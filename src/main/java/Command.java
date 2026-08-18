/**
 * Represents the fixed set of command words FF15 understands.
 */
public enum Command {
    LIST("list", false),
    MARK("mark", true),
    UNMARK("unmark", true),
    DELETE("delete", true),
    TODO("todo", true),
    DEADLINE("deadline", true),
    EVENT("event", true),
    BYE("bye", false),
    UNKNOWN("", false);

    private final String word;
    private final boolean acceptsArguments;

    Command(String word, boolean acceptsArguments) {
        this.word = word;
        this.acceptsArguments = acceptsArguments;
    }

    public String getWord() {
        return word;
    }

    /**
     * Matches {@code input} against a known command word. Commands that accept
     * arguments also match when {@code input} starts with "{@code word} " (a
     * trailing space); the rest is left for the caller to parse as arguments.
     * Returns {@link #UNKNOWN} if nothing matches.
     */
    public static Command match(String input) {
        for (Command command : values()) {  // static method values() return all COMMAND enum types
            if (command == UNKNOWN) {
                continue;
            }
            if (input.equals(command.word)
                    || (command.acceptsArguments && input.startsWith(command.word + " "))) {  // to check for empty mark cases, so empty mark != empty input
                return command;
            }
        }
        return UNKNOWN;
    }
}
