package ff15;

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
    private Parser() {   // a private constructor stops anyone writing "new Parser()"
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
     * that it is present, numeric, and within range of the current list.
     *
     * @param listSize how many tasks exist, used for the range check
     */
    public static int parseTaskNumber(String input, CommandWord command, int listSize) throws FF15Exception {
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
        if (number < 1 || number > listSize) {
            throw new FF15Exception("I don't have task number " + number + ". You've got " + listSize + " task(s).");
        }
        return number;
    }

    /** Builds the Todo described by {@code input}, which needs only a description. */
    public static Todo parseTodo(String input) throws FF15Exception {
        String description = argumentAfter(input, CommandWord.TODO);
        if (description.isEmpty()) {
            throw new FF15Exception("The description of a todo can't be empty, bro.");
        }
        return new Todo(description);
    }

    /** Builds the Deadline described by {@code input}, which must carry a /by. */
    public static Deadline parseDeadline(String input) throws FF15Exception {
        String details = argumentAfter(input, CommandWord.DEADLINE);
        int byIndex = details.indexOf(" /by");
        if (byIndex == -1) {
            throw new FF15Exception("A deadline needs a /by, e.g.: deadline return book /by 2019-12-02 1800");
        }
        String description = details.substring(0, byIndex).trim();
        String by = details.substring(byIndex + " /by".length()).trim();
        if (description.isEmpty()) {
            throw new FF15Exception("The description of a deadline can't be empty, bro.");
        }
        if (by.isEmpty()) {
            throw new FF15Exception("The /by date/time of a deadline can't be empty, bro.");
        }
        return new Deadline(description, TaskTime.parse(by));
    }

    /** Builds the Event described by {@code input}, which must carry a /from followed by a /to. */
    public static Event parseEvent(String input) throws FF15Exception {
        String details = argumentAfter(input, CommandWord.EVENT);
        int fromIndex = details.indexOf(" /from");
        int toIndex = details.indexOf(" /to");
        if (fromIndex == -1 || toIndex == -1 || toIndex < fromIndex) {
            throw new FF15Exception(
                    "An event needs /from and /to, e.g.: event project meeting /from 2019-12-05 1400 /to 2019-12-05 1600");
        }
        String description = details.substring(0, fromIndex).trim();
        String from = details.substring(fromIndex + " /from".length(), toIndex).trim();
        String to = details.substring(toIndex + " /to".length()).trim();
        if (description.isEmpty()) {
            throw new FF15Exception("The description of an event can't be empty bro.");
        }
        if (from.isEmpty() || to.isEmpty()) {
            throw new FF15Exception("The /from and /to date/times of an event can't be empty, bro.");
        }
        TaskTime fromTime = TaskTime.parse(from);
        TaskTime toTime = TaskTime.parse(to);
        if (toTime.isBefore(fromTime)) {                          // an event can't finish before it begins
            throw new FF15Exception("An event can't end before it starts, bro.");
        }
        return new Event(description, fromTime, toTime);
    }

    /** Builds the span of dates asked about by an {@code on} command. */
    public static DateRange parseDateQuery(String input) throws FF15Exception {
        String query = argumentAfter(input, CommandWord.ON);
        if (query.isEmpty()) {
            throw new FF15Exception("Tell me when, e.g.: on 2019-12-02, on 2019-12, or on 2019");
        }
        return DateRange.parse(query);
    }
}
