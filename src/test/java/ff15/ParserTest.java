package ff15;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import ff15.command.AddCommand;
import ff15.command.Command;
import ff15.command.DeleteCommand;
import ff15.command.ExitCommand;
import ff15.command.ListCommand;
import ff15.command.MarkCommand;
import ff15.command.OnCommand;
import ff15.command.UnmarkCommand;
import ff15.task.Task;
import ff15.task.TaskList;
import ff15.task.Todo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link Parser#parse(String)}: which command a line of input turns into,
 * what that command was given, and which badly typed lines are rejected.
 *
 * <p>The commands the parser builds keep their contents to themselves, so a
 * successfully parsed line is checked by running it against a real
 * {@link TaskList} and looking at what ended up there. That also covers the
 * substring arithmetic used to pull a description and a date out of one line,
 * which is where an off-by-one would hide.
 */
public class ParserTest {

    @TempDir
    Path tempDir;

    /**
     * Parses {@code input} and runs it against {@code tasks}, saving into the test's
     * temporary folder and throwing away whatever the Ui prints.
     */
    private void run(TaskList tasks, String input) throws Exception {
        PrintStream realOut = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        try {
            Command command = Parser.parse(input);
            command.execute(tasks, new Ui(), new Storage(tempDir.resolve("tasks.txt").toString()));
        } finally {
            System.setOut(realOut);
        }
    }

    /** Parses and runs {@code input} against an empty list, returning the single task it added. */
    private Task addedBy(String input) throws Exception {
        TaskList tasks = new TaskList();
        run(tasks, input);
        assertEquals(1, tasks.size(), "the command should have added exactly one task");
        return tasks.get(1);
    }

    // --- which command each line becomes -------------------------------------

    @Test
    public void parse_list_returnsListCommand() throws FF15Exception {
        assertInstanceOf(ListCommand.class, Parser.parse("list"));
    }

    @Test
    public void parse_bye_returnsExitCommandThatEndsTheSession() throws FF15Exception {
        Command command = Parser.parse("bye");
        assertInstanceOf(ExitCommand.class, command);
        assertTrue(command.isExit());
    }

    @Test
    public void parse_anythingButBye_returnsCommandThatDoesNotEndTheSession() throws FF15Exception {
        assertFalse(Parser.parse("list").isExit());
        assertFalse(Parser.parse("todo read book").isExit());
        assertFalse(Parser.parse("mark 1").isExit());
        assertFalse(Parser.parse("on 2019-12-02").isExit());
    }

    @Test
    public void parse_addingCommands_returnAddCommand() throws FF15Exception {
        assertInstanceOf(AddCommand.class, Parser.parse("todo read book"));
        assertInstanceOf(AddCommand.class, Parser.parse("deadline return book /by 2019-12-02"));
        assertInstanceOf(AddCommand.class,
                Parser.parse("event meeting /from 2019-12-05 1400 /to 2019-12-05 1600"));
    }

    @Test
    public void parse_taskNumberCommands_returnTheMatchingCommand() throws FF15Exception {
        assertInstanceOf(MarkCommand.class, Parser.parse("mark 1"));
        assertInstanceOf(UnmarkCommand.class, Parser.parse("unmark 1"));
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 1"));
    }

    @Test
    public void parse_on_returnsOnCommand() throws FF15Exception {
        assertInstanceOf(OnCommand.class, Parser.parse("on 2019-12-02"));
        assertInstanceOf(OnCommand.class, Parser.parse("on 2019-12"));
        assertInstanceOf(OnCommand.class, Parser.parse("on 2019"));
    }

    // --- what the parsed command was given -----------------------------------

    @Test
    public void parse_todo_buildsTodoWithTheDescription() throws Exception {
        assertEquals("[T][ ] read book", addedBy("todo read book").toString());
    }

    @Test
    public void parse_todoWithSurroundingSpaces_trimsTheDescription() throws Exception {
        assertEquals("[T][ ] read book", addedBy("todo    read book   ").toString());
    }

    @Test
    public void parse_deadlineWithDateOnly_buildsDeadlineWithThatDate() throws Exception {
        assertEquals("[D][ ] return book (by: Dec 02 2019)",
                addedBy("deadline return book /by 2019-12-02").toString());
    }

    @Test
    public void parse_deadlineWithTime_keepsTheTime() throws Exception {
        assertEquals("[D][ ] return book (by: Dec 02 2019, 6:00pm)",
                addedBy("deadline return book /by 2019-12-02 1800").toString());
    }

    @Test
    public void parse_deadlineWithSpacesAroundTheBy_trimsBothSides() throws Exception {
        assertEquals("[D][ ] return book (by: Dec 02 2019)",
                addedBy("deadline return book   /by    2019-12-02  ").toString());
    }

    @Test
    public void parse_event_buildsEventWithBothEnds() throws Exception {
        assertEquals("[E][ ] project meeting (from: Dec 05 2019, 2:00pm to: Dec 05 2019, 4:00pm)",
                addedBy("event project meeting /from 2019-12-05 1400 /to 2019-12-05 1600").toString());
    }

    @Test
    public void parse_multiWordDescriptions_keepInnerSpacing() throws Exception {
        assertEquals("[T][ ] borrow a book from the library",
                addedBy("todo borrow a book from the library").toString());
    }

    @Test
    public void parse_mark_marksTheTaskTheUserNumbered() throws Exception {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("a"));
        tasks.add(new Todo("b"));
        run(tasks, "mark 2");
        assertEquals("[T][ ] a", tasks.get(1).toString());
        assertEquals("[T][X] b", tasks.get(2).toString());
    }

    @Test
    public void parse_unmark_clearsTheTaskTheUserNumbered() throws Exception {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("a"));
        run(tasks, "mark 1");
        run(tasks, "unmark 1");
        assertEquals("[T][ ] a", tasks.get(1).toString());
    }

    @Test
    public void parse_delete_removesTheTaskTheUserNumbered() throws Exception {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("a"));
        tasks.add(new Todo("b"));
        run(tasks, "delete 1");
        assertEquals(1, tasks.size());
        assertEquals("[T][ ] b", tasks.get(1).toString());
    }

    // --- lines that should be rejected ---------------------------------------

    @Test
    public void parse_unknownCommand_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("blah"));
        assertThrows(FF15Exception.class, () -> Parser.parse(""));
        assertThrows(FF15Exception.class, () -> Parser.parse("Todo read book"));
        assertThrows(FF15Exception.class, () -> Parser.parse("mark2"));
    }

    @Test
    public void parse_todoWithoutDescription_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("todo"));
        assertThrows(FF15Exception.class, () -> Parser.parse("todo   "));
    }

    @Test
    public void parse_taskNumberMissing_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("mark"));
        assertThrows(FF15Exception.class, () -> Parser.parse("unmark   "));
        assertThrows(FF15Exception.class, () -> Parser.parse("delete"));
    }

    @Test
    public void parse_taskNumberNotANumber_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("mark two"));
        assertThrows(FF15Exception.class, () -> Parser.parse("delete 1.5"));
        assertThrows(FF15Exception.class, () -> Parser.parse("unmark 1 2"));
    }

    @Test
    public void parse_taskNumberOutOfRange_isLeftForTheTaskListToReject() throws FF15Exception {
        // Parsing only checks that a number was typed; whether it names a task is
        // something only the list knows, so the complaint comes at execute time.
        Command command = Parser.parse("mark 99");
        TaskList empty = new TaskList();
        assertThrows(FF15Exception.class, () -> run(empty, "mark 99"));
        assertInstanceOf(MarkCommand.class, command);
    }

    @Test
    public void parse_negativeTaskNumber_parsesButIsRejectedOnExecute() throws FF15Exception {
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete -1"));
        TaskList tasks = new TaskList();
        tasks.add(new Todo("a"));
        assertThrows(FF15Exception.class, () -> run(tasks, "delete -1"));
        assertEquals(1, tasks.size());
    }

    @Test
    public void parse_deadlineWithoutBy_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("deadline return book"));
        assertThrows(FF15Exception.class, () -> Parser.parse("deadline"));
    }

    @Test
    public void parse_deadlineWithEmptyDate_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("deadline return book /by"));
        assertThrows(FF15Exception.class, () -> Parser.parse("deadline return book /by   "));
    }

    @Test
    public void parse_deadlineWithUnreadableDate_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("deadline return book /by sunday"));
        assertThrows(FF15Exception.class, () -> Parser.parse("deadline return book /by 2019-13-02"));
        assertThrows(FF15Exception.class, () -> Parser.parse("deadline return book /by 2019-12-02 2500"));
    }

    @Test
    public void parse_eventMissingFromOrTo_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("event meeting"));
        assertThrows(FF15Exception.class, () -> Parser.parse("event meeting /from 2019-12-05"));
        assertThrows(FF15Exception.class, () -> Parser.parse("event meeting /to 2019-12-05"));
        assertThrows(FF15Exception.class, () -> Parser.parse("event"));
    }

    @Test
    public void parse_eventWithToBeforeFrom_throwsException() {
        assertThrows(FF15Exception.class,
                () -> Parser.parse("event meeting /to 2019-12-06 /from 2019-12-05"));
    }

    @Test
    public void parse_eventWithEmptyEnds_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("event meeting /from /to 2019-12-06"));
        assertThrows(FF15Exception.class, () -> Parser.parse("event meeting /from 2019-12-05 /to"));
    }

    @Test
    public void parse_eventEndingBeforeItStarts_throwsException() {
        assertThrows(FF15Exception.class,
                () -> Parser.parse("event meeting /from 2019-12-06 /to 2019-12-05"));
        assertThrows(FF15Exception.class,
                () -> Parser.parse("event meeting /from 2019-12-05 1600 /to 2019-12-05 1400"));
    }

    @Test
    public void parse_eventStartingAndEndingAtTheSameMoment_isAccepted() throws Exception {
        assertEquals("[E][ ] meeting (from: Dec 05 2019, 2:00pm to: Dec 05 2019, 2:00pm)",
                addedBy("event meeting /from 2019-12-05 1400 /to 2019-12-05 1400").toString());
    }

    @Test
    public void parse_onWithoutADate_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("on"));
        assertThrows(FF15Exception.class, () -> Parser.parse("on   "));
    }

    @Test
    public void parse_onWithUnreadableDate_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("on someday"));
        assertThrows(FF15Exception.class, () -> Parser.parse("on 2019-12-02-03"));
    }

    @Test
    public void parse_unknownCommand_messageExplainsItWasNotUnderstood() {
        FF15Exception thrown = assertThrows(FF15Exception.class, () -> Parser.parse("blah"));
        assertTrue(thrown.getMessage().contains("don't know what that means"), thrown.getMessage());
    }
}
