package ff15;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Represents a task that needs to be done before a specific date.
 * The date is held as a {@link LocalDate} rather than plain text, so that it is
 * validated when it is typed and can later be compared or sorted.
 */
public class Deadline extends Task {
    /** How a date is written by the user and in the save file, e.g. {@code 2019-12-02}. */
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /** How a date is shown back to the user, e.g. {@code Dec 02 2019}. */
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd yyyy");

    protected LocalDate date;

    public Deadline(String description, LocalDate by) {
        super(description);
        this.date = by;
    }

    /**
     * Turns the text typed after {@code /by} into a date.
     *
     * @param text a date in yyyy-mm-dd form, e.g. {@code 2019-12-02}
     * @throws FF15Exception if {@code text} is not a real date in that format
     */
    public static LocalDate parseDate(String text) throws FF15Exception {
        try {
            return LocalDate.parse(text, INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            throw new FF15Exception("'" + text + "' isn't a date I understand. "
                    + "Write it as yyyy-mm-dd, e.g.: deadline return book /by 2019-12-02");
        }
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + date.format(DISPLAY_FORMAT) + ")";
    }

    @Override
    public String toFileFormat() {
        // Saved in the input format so that load() can read it back with parseDate().
        return "D | " + super.toFileFormat() + " | " + date.format(INPUT_FORMAT);
    }
}
