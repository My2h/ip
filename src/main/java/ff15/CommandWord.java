package ff15;

/**
 * Represents the fixed set of command words FF15 understands. Recognising which
 * word was typed is a parsing job; what each one then does lives elsewhere.
 */
public enum CommandWord {
    /** Shows every task in the list. */
    LIST("list", false),
    /** Shows the tasks falling on a given day, month, or year. */
    ON("on", true),
    /** Marks a task as done. */
    MARK("mark", true),
    /** Marks a task as not done again. */
    UNMARK("unmark", true),
    /** Removes a task from the list. */
    DELETE("delete", true),
    /** Adds a task with no date attached to it. */
    TODO("todo", true),
    /** Adds a task due by a given date, optionally at a given time. */
    DEADLINE("deadline", true),
    /** Adds a task running between two given dates or date/times. */
    EVENT("event", true),
    /** Shows the tasks whose description contains a given keyword. */
    FIND("find", true),
    /** Ends the session. */
    BYE("bye", false),
    /** Anything the chatbot does not recognise. Has no word of its own. */
    UNKNOWN("", false);

    private final String word;
    private final boolean acceptsArguments;

    /**
     * Records the word the user types for this command, and whether anything
     * is allowed to follow it on the same line.
     *
     * @param word what the user types to invoke this command
     * @param acceptsArguments whether arguments may follow the word
     */
    CommandWord(String word, boolean acceptsArguments) {
        this.word = word;
        this.acceptsArguments = acceptsArguments;
    }

    /** Returns the word the user types to invoke this command. */
    public String getWord() {
        return word;
    }

    /**
     * Matches {@code input} against a known command word. Commands that accept
     * arguments also match when {@code input} starts with "{@code word} " (a
     * trailing space); the rest is left for the caller to parse as arguments.
     * Returns {@link #UNKNOWN} if nothing matches.
     */
    public static CommandWord match(String input) {
        for (CommandWord command : values()) {  // static method values() return all COMMAND enum types
            if (command == UNKNOWN) {
                continue;
            }
            // Matches the word on its own, or the word followed by a space and
            // arguments. Requiring that space keeps "marker" from being read as
            // a "mark", while still letting a bare "mark" through so the parser
            // can report the missing task number.
            if (input.equals(command.word)
                    || (command.acceptsArguments && input.startsWith(command.word + " "))) {
                return command;
            }
        }
        return UNKNOWN;
    }
}
