package ff15;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Shared helpers for reading and showing the dates attached to tasks, so that
 * every task type accepts the same input format and prints dates the same way.
 * Holds only static helpers and is never instantiated.
 */
public class Dates {
    /** How a date is typed by the user and written to the save file, e.g. {@code 2019-12-02}. */
    public static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /** How a single date is shown back to the user, e.g. {@code Dec 02 2019}. */
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd yyyy");

    private Dates() {   // a private constructor stops anyone writing "new Dates()"
    }

    /**
     * Turns text typed by the user, or read back from the save file, into a date.
     *
     * @param text a date in yyyy-mm-dd form, e.g. {@code 2019-12-02}
     * @throws FF15Exception if {@code text} is not a real date in that format
     */
    public static LocalDate parse(String text) throws FF15Exception {
        try {
            return LocalDate.parse(text, INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            throw new FF15Exception("'" + text + "' isn't a date I understand. "
                    + "Write dates as yyyy-mm-dd, e.g. 2019-12-02");
        }
    }

    /** Returns {@code date} in the form shown to the user, e.g. {@code Dec 02 2019}. */
    public static String format(LocalDate date) {
        return date.format(DISPLAY_FORMAT);
    }

    /** Returns {@code date} in the form written to the save file, e.g. {@code 2019-12-02}. */
    public static String toFileFormat(LocalDate date) {
        return date.format(INPUT_FORMAT);
    }
}
