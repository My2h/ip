package ff15;

import ff15.command.AddCommand;
import ff15.command.Command;
import ff15.command.DeleteCommand;
import ff15.command.ExitCommand;
import ff15.command.FindCommand;
import ff15.command.ListCommand;
import ff15.command.MarkCommand;
import ff15.command.OnCommand;
import ff15.command.UnmarkCommand;
import ff15.task.DateRange;
import ff15.task.Deadline;
import ff15.task.Event;
import ff15.task.TaskTime;
import ff15.task.Todo;

/**
 * Deals with making sense of what the user typed: separating the arguments from
 * the command word, checking that the required parts are all there, and turning
 * them into the objects the rest of the program works with.
 *
 * <p>Every method throws {@link FF15Exception} carrying a message aimed at the
 * user, so a badly typed command is reported rather than reaching the task list
 * half-understood. Holds only static helpers and is never instantiated.
 */
public class Parser {
    /** Marks off the date a deadline is due, e.g. {@code deadline return book /by 2019-12-02}. */
    private static final String BY_MARKER = " /by";

    /** Marks off the date an event starts. */
    private static final String FROM_MARKER = " /from";

    /** Marks off the date an event ends. */
    private static final String TO_MARKER = " /to";

    private Parser() { // a private constructor stops anyone writing "new Parser()"
    }

    /**
     * Works out which command {@code input} asks for, and builds it ready to run.
     *
     * @throws FF15Exception if the command word is not recognised, or the rest of
     *     the line does not give the command what it needs.
     */
    public static Command parse(String input) throws FF15Exception {
        CommandWord word = CommandWord.match(input);
        return switch (word) {
            case LIST -> new ListCommand();
            case ON -> new OnCommand(parseDateQuery(input));
            case MARK -> new MarkCommand(parseTaskNumber(input, CommandWord.MARK));
            case UNMARK -> new UnmarkCommand(parseTaskNumber(input, CommandWord.UNMARK));
            case DELETE -> new DeleteCommand(parseTaskNumber(input, CommandWord.DELETE));
            case TODO -> new AddCommand(parseTodo(input));
            case DEADLINE -> new AddCommand(parseDeadline(input));
            case EVENT -> new AddCommand(parseEvent(input));
            case FIND -> new FindCommand(parseKeyword(input));
            case BYE -> new ExitCommand();
            case UNKNOWN -> throw new FF15Exception("I'm sorry big man, I don't know what that means :-(");
        };
    }

    /**
     * Returns whatever follows the command word in {@code input} (trimmed), or an
     * empty string if the command word was typed with nothing after it.
     */
    private static String argumentAfter(String input, CommandWord command) {
        String word = command.getWord();
        if (input.length() <= word.length()) {
            return "";
        }
        return input.substring(word.length() + 1).trim();
    }

    /**
     * Parses the 1-based task number given to mark, unmark, or delete, checking
     * that it is present and numeric. Whether it is in range is checked later by
     * {@link TaskList}, which is the thing that knows how many tasks there are.
     */
    private static int parseTaskNumber(String input, CommandWord command) throws FF15Exception {
        String arg = argumentAfter(input, command);
        if (arg.isEmpty()) {
            throw new FF15Exception("Bro Tell me which task number, e.g. mark 2.");
        }
        int number;
        try {
            number = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new FF15Exception("'" + arg + "' aint looking like a task number.");
        }
        return number;
    }

    /** Builds the Todo described by {@code input}, which needs only a description. */
    private static Todo parseTodo(String input) throws FF15Exception {
        String description = argumentAfter(input, CommandWord.TODO);
        if (description.isEmpty()) {
            throw new FF15Exception("The description of a todo can't be empty, bro.");
        }
        return new Todo(description);
    }

    /** Builds the Deadline described by {@code input}, which must carry a /by. */
    private static Deadline parseDeadline(String input) throws FF15Exception {
        String details = argumentAfter(input, CommandWord.DEADLINE);
        int byIndex = details.indexOf(BY_MARKER);
        if (byIndex == -1) {
            throw new FF15Exception("A deadline needs a /by, e.g.: deadline return book /by 2019-12-02 1800");
        }
        String description = details.substring(0, byIndex).trim();
        String by = details.substring(byIndex + BY_MARKER.length()).trim();
        if (description.isEmpty()) {
            throw new FF15Exception("The description of a deadline can't be empty, bro.");
        }
        if (by.isEmpty()) {
            throw new FF15Exception("The /by date/time of a deadline can't be empty, bro.");
        }
        return new Deadline(description, TaskTime.parse(by));
    }

    /** Builds the Event described by {@code input}, which must carry a /from followed by a /to. */
    private static Event parseEvent(String input) throws FF15Exception {
        String details = argumentAfter(input, CommandWord.EVENT);
        int fromIndex = details.indexOf(FROM_MARKER);
        int toIndex = details.indexOf(TO_MARKER);
        boolean isFromMissing = fromIndex == -1;
        boolean isToMissing = toIndex == -1;
        boolean isToBeforeFrom = toIndex < fromIndex;
        if (isFromMissing || isToMissing || isToBeforeFrom) {
            throw new FF15Exception("An event needs /from and /to, e.g.: "
                    + "event project meeting /from 2019-12-05 1400 /to 2019-12-05 1600");
        }
        String description = details.substring(0, fromIndex).trim();
        String from = details.substring(fromIndex + FROM_MARKER.length(), toIndex).trim();
        String to = details.substring(toIndex + TO_MARKER.length()).trim();
        if (description.isEmpty()) {
            throw new FF15Exception("The description of an event can't be empty bro.");
        }
        if (from.isEmpty() || to.isEmpty()) {
            throw new FF15Exception("The /from and /to date/times of an event can't be empty, bro.");
        }
        TaskTime fromTime = TaskTime.parse(from);
        TaskTime toTime = TaskTime.parse(to);
        if (toTime.isBefore(fromTime)) { // an event can't finish before it begins
            throw new FF15Exception("An event can't end before it starts, bro.");
        }
        return new Event(description, fromTime, toTime);
    }

    /** Returns the keyword a {@code find} command should search for. */
    private static String parseKeyword(String input) throws FF15Exception {
        String keyword = argumentAfter(input, CommandWord.FIND);
        if (keyword.isEmpty()) {
            throw new FF15Exception("Tell me what to look for, e.g.: find book");
        }
        return keyword;
    }

    /** Builds the span of dates asked about by an {@code on} command. */
    private static DateRange parseDateQuery(String input) throws FF15Exception {
        String query = argumentAfter(input, CommandWord.ON);
        if (query.isEmpty()) {
            throw new FF15Exception("Tell me when, e.g.: on 2019-12-02, on 2019-12, or on 2019");
        }
        return DateRange.parse(query);
    }
}
