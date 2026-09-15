package ff15;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ff15.command.AddCommand;
import ff15.command.Command;
import ff15.command.ContactAddCommand;
import ff15.command.ContactDeleteCommand;
import ff15.command.ContactFindCommand;
import ff15.command.ContactListCommand;
import ff15.command.DeleteCommand;
import ff15.command.ExitCommand;
import ff15.command.FindCommand;
import ff15.command.ListCommand;
import ff15.command.MarkCommand;
import ff15.command.OnCommand;
import ff15.command.UnmarkCommand;
import ff15.contact.Contact;
import ff15.contact.ContactList;
import ff15.task.Task;
import ff15.task.TaskList;
import ff15.task.Todo;

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

    /** Returns a Storage writing both save files into the test's temporary folder. */
    private Storage storage() {
        return new Storage(tempDir.resolve("tasks.txt").toString(),
                tempDir.resolve("contacts.txt").toString());
    }

    /** Parses and runs {@code input} against {@code tasks} and an empty contact list. */
    private void run(TaskList tasks, String input) throws Exception {
        run(tasks, new ContactList(), input);
    }

    /**
     * Parses {@code input} and runs it against both lists, saving into the test's
     * temporary folder and throwing away whatever the Ui prints.
     */
    private void run(TaskList tasks, ContactList contacts, String input) throws Exception {
        PrintStream realOut = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        try {
            Command command = Parser.parse(input);
            command.execute(tasks, contacts, new Ui(), storage());
        } finally {
            System.setOut(realOut);
        }
    }

    /** Parses and runs {@code input} against {@code tasks}, returning what the Ui printed. */
    private String runCapturing(TaskList tasks, String input) throws Exception {
        return runCapturing(tasks, new ContactList(), input);
    }

    /**
     * Parses and runs {@code input} against both lists, returning everything the
     * Ui printed while doing so.
     */
    private String runCapturing(TaskList tasks, ContactList contacts, String input) throws Exception {
        PrintStream realOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            Command command = Parser.parse(input);
            command.execute(tasks, contacts, new Ui(), storage());
        } finally {
            System.setOut(realOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
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
    public void parse_find_returnsFindCommand() throws FF15Exception {
        assertInstanceOf(FindCommand.class, Parser.parse("find book"));
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
        tasks.add(new Todo("a"), new Todo("b"));
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
        tasks.add(new Todo("a"), new Todo("b"));
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
        assertThrows(FF15Exception.class, () ->
                Parser.parse("event meeting /to 2019-12-06 /from 2019-12-05"));
    }

    @Test
    public void parse_eventWithEmptyEnds_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("event meeting /from /to 2019-12-06"));
        assertThrows(FF15Exception.class, () -> Parser.parse("event meeting /from 2019-12-05 /to"));
    }

    @Test
    public void parse_eventEndingBeforeItStarts_throwsException() {
        assertThrows(FF15Exception.class, () ->
                Parser.parse("event meeting /from 2019-12-06 /to 2019-12-05"));
        assertThrows(FF15Exception.class, () ->
                Parser.parse("event meeting /from 2019-12-05 1600 /to 2019-12-05 1400"));
    }

    @Test
    public void parse_eventStartingAndEndingAtTheSameMoment_isAccepted() throws Exception {
        assertEquals("[E][ ] meeting (from: Dec 05 2019, 2:00pm to: Dec 05 2019, 2:00pm)",
                addedBy("event meeting /from 2019-12-05 1400 /to 2019-12-05 1400").toString());
    }

    @Test
    public void parse_find_reportsTheMatchingTasks() throws Exception {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"), new Todo("buy milk"));

        String printed = runCapturing(tasks, "find book");

        assertTrue(printed.contains("Michael Scarn:"), printed);
        assertTrue(printed.contains("1.[T][ ] read book"), printed);
        assertFalse(printed.contains("buy milk"), printed);
    }

    @Test
    public void parse_findWithNothingMatching_saysSo() throws Exception {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        String printed = runCapturing(tasks, "find homework");

        assertTrue(printed.contains("Nothing matching 'homework'. I looked."), printed);
    }

    @Test
    public void parse_findWithSurroundingSpaces_trimsTheKeyword() throws Exception {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        String printed = runCapturing(tasks, "find    book   ");

        assertTrue(printed.contains("1.[T][ ] read book"), printed);
    }

    @Test
    public void parse_findLeavesTheListAlone_doesNotChangeAnyTask() throws Exception {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));
        runCapturing(tasks, "find book");
        assertEquals(1, tasks.size());
        assertEquals("[T][ ] read book", tasks.get(1).toString());
    }

    @Test
    public void parse_findWithoutAKeyword_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("find"));
        assertThrows(FF15Exception.class, () -> Parser.parse("find   "));
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

    // --- contacts -------------------------------------------------------------

    /** Runs {@code input} against a fresh contact list and returns the single contact it added. */
    private Contact contactAddedBy(String input) throws Exception {
        ContactList contacts = new ContactList();
        run(new TaskList(), contacts, input);
        assertEquals(1, contacts.size(), "the command should have added exactly one contact");
        return contacts.get(1);
    }

    @Test
    public void parse_contactSubCommands_returnTheMatchingCommand() throws FF15Exception {
        assertInstanceOf(ContactAddCommand.class, Parser.parse("contact add John"));
        assertInstanceOf(ContactListCommand.class, Parser.parse("contact list"));
        assertInstanceOf(ContactDeleteCommand.class, Parser.parse("contact delete 1"));
        assertInstanceOf(ContactFindCommand.class, Parser.parse("contact find john"));
    }

    @Test
    public void parse_contactAddWithAllFields_keepsAllThree() throws Exception {
        Contact contact = contactAddedBy("contact add John /phone 91234567 /email john@example.com");
        assertEquals("John", contact.getName());
        assertEquals("91234567", contact.getPhone());
        assertEquals("john@example.com", contact.getEmail());
    }

    @Test
    public void parse_contactAddWithMarkersReversed_stillReadsBothFields() throws Exception {
        Contact contact = contactAddedBy("contact add John /email john@example.com /phone 91234567");
        assertEquals("John", contact.getName());
        assertEquals("91234567", contact.getPhone());
        assertEquals("john@example.com", contact.getEmail());
    }

    @Test
    public void parse_contactAddWithOneField_leavesTheOtherEmpty() throws Exception {
        Contact withPhone = contactAddedBy("contact add Mary /phone 98765432");
        assertEquals("98765432", withPhone.getPhone());
        assertEquals("", withPhone.getEmail());

        Contact withEmail = contactAddedBy("contact add Sam /email sam@example.com");
        assertEquals("", withEmail.getPhone());
        assertEquals("sam@example.com", withEmail.getEmail());
    }

    @Test
    public void parse_contactAddNameOnly_keepsTheWholeName() throws Exception {
        assertEquals("Alex Tan", contactAddedBy("contact add Alex Tan").getName());
    }

    @Test
    public void parse_contactAddWithSurroundingSpaces_trimsEachField() throws Exception {
        Contact contact = contactAddedBy("contact add   John   /phone   91234567   /email   john@example.com  ");
        assertEquals("John", contact.getName());
        assertEquals("91234567", contact.getPhone());
        assertEquals("john@example.com", contact.getEmail());
    }

    @Test
    public void parse_contactAddWithNoName_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("contact add"));
        assertThrows(FF15Exception.class, () -> Parser.parse("contact add /phone 91234567"));
    }

    @Test
    public void parse_contactAddWithEmptyMarkerValue_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("contact add John /phone"));
        assertThrows(FF15Exception.class, () -> Parser.parse("contact add John /email"));
    }

    @Test
    public void parse_contactAddWithUnusablePhone_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("contact add John /phone hello"));
    }

    @Test
    public void parse_contactAddWithOddButValidPhone_isAccepted() throws Exception {
        assertEquals("+65 (912) 345-67", contactAddedBy("contact add John /phone +65 (912) 345-67").getPhone());
    }

    @Test
    public void parse_contactAddWithUnusableEmail_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("contact add John /email nope"));
        assertThrows(FF15Exception.class, () -> Parser.parse("contact add John /email @example.com"));
        assertThrows(FF15Exception.class, () -> Parser.parse("contact add John /email john@"));
        assertThrows(FF15Exception.class, () -> Parser.parse("contact add John /email a@b@c"));
    }

    @Test
    public void parse_contactWithNoSubCommand_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("contact"));
        assertThrows(FF15Exception.class, () -> Parser.parse("contact   "));
    }

    @Test
    public void parse_contactWithUnknownSubCommand_throwsException() {
        FF15Exception thrown = assertThrows(FF15Exception.class, () -> Parser.parse("contact mark 1"));
        assertTrue(thrown.getMessage().contains("contact add"), thrown.getMessage());
    }

    @Test
    public void parse_wordThatMerelyStartsWithContact_isNotAContactCommand() {
        assertThrows(FF15Exception.class, () -> Parser.parse("contacts"));
    }

    @Test
    public void parse_contactListWithAnArgument_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("contact list everything"));
    }

    @Test
    public void parse_contactDeleteWithoutOrWithABadNumber_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("contact delete"));
        assertThrows(FF15Exception.class, () -> Parser.parse("contact delete two"));
    }

    @Test
    public void parse_contactFindWithoutAKeyword_throwsException() {
        assertThrows(FF15Exception.class, () -> Parser.parse("contact find"));
    }

    @Test
    public void parse_contactDelete_removesTheContactTheUserNumbered() throws Exception {
        ContactList contacts = new ContactList();
        contacts.add(new Contact("a", "", ""), new Contact("b", "", ""));
        run(new TaskList(), contacts, "contact delete 1");
        assertEquals(1, contacts.size());
        assertEquals("b", contacts.get(1).getName());
    }

    @Test
    public void parse_contactFind_reportsTheMatchingContacts() throws Exception {
        ContactList contacts = new ContactList();
        contacts.add(new Contact("John", "", ""), new Contact("Mary", "", ""));

        String printed = runCapturing(new TaskList(), contacts, "contact find john");

        assertTrue(printed.contains("John"), printed);
        assertFalse(printed.contains("Mary"), printed);
    }

    @Test
    public void parse_contactFindWithNoMatch_saysSo() throws Exception {
        ContactList contacts = new ContactList();
        contacts.add(new Contact("John", "", ""));

        String printed = runCapturing(new TaskList(), contacts, "contact find zzz");

        assertTrue(printed.contains("no contacts matching"), printed);
    }
}
