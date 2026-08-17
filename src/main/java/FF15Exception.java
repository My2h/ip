/**
 * Represents an error caused by invalid user input (e.g. an unknown command,
 * or a command missing a required part such as a description or date/time).
 */
public class FF15Exception extends Exception {
    public FF15Exception(String message) {
        super(message);
    }
}
