package ff15.task;

import ff15.FF15Exception;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * The date, and optionally the time of day, attached to a task.
 * The user may type either form: {@code 2019-12-02} for a whole day, or
 * {@code 2019-12-02 1800} to pin it to 6pm. Whether a time was given is
 * remembered, so a task typed without one is never shown with a made-up
 * midnight attached to it.
 *
 * <p>This class replaces the earlier {@code Dates} helper: it owns the same
 * parsing and formatting, but now has something to store, so it holds a value
 * instead of being a bag of static methods.
 */
public class TaskTime {
    /** A date on its own, as typed and as saved, e.g. {@code 2019-12-02}. */
    private static final DateTimeFormatter DATE_INPUT = DateTimeFormatter.ISO_LOCAL_DATE;

    /** A date with a time, as typed and as saved, e.g. {@code 2019-12-02 1800}. */
    private static final DateTimeFormatter DATE_TIME_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /** How the date is shown back to the user, e.g. {@code Dec 02 2019}. */
    private static final DateTimeFormatter DATE_DISPLAY = DateTimeFormatter.ofPattern("MMM dd yyyy");

    /** How the time is shown back to the user, e.g. {@code 6:00pm}. */
    private static final DateTimeFormatter TIME_DISPLAY = DateTimeFormatter.ofPattern("h:mma");

    private final LocalDateTime moment;
    private final boolean hasTime;   // false when the user typed a date only

    private TaskTime(LocalDateTime moment, boolean hasTime) {
        this.moment = moment;
        this.hasTime = hasTime;
    }

    /**
     * Turns text typed by the user, or read back from the save file, into a
     * date and an optional time. A space in {@code text} means a time follows.
     *
     * @throws FF15Exception if {@code text} is not a real date, or a real date and time
     */
    public static TaskTime parse(String text) throws FF15Exception {
        try {
            if (text.contains(" ")) {
                return new TaskTime(LocalDateTime.parse(text, DATE_TIME_INPUT), true);
            }
            // No time given: keep the date and remember that the time is unknown.
            return new TaskTime(LocalDate.parse(text, DATE_INPUT).atStartOfDay(), false);
        } catch (DateTimeParseException e) {
            throw new FF15Exception("'" + text + "' isn't a date I understand. "
                    + "Write it as yyyy-mm-dd, or yyyy-mm-dd HHmm to add a time, "
                    + "e.g. 2019-12-02 or 2019-12-02 1800");
        }
    }

    /** Returns just the day this falls on, which is what a {@link DateRange} query matches against. */
    public LocalDate getDate() {
        return moment.toLocalDate();
    }

    /** Returns whether this moment comes before {@code other}. */
    public boolean isBefore(TaskTime other) {
        return moment.isBefore(other.moment);
    }

    /** Returns a bare date in the form used throughout the output, e.g. {@code Dec 02 2019}. */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_DISPLAY);
    }

    /** Returns this in the form written to the save file, which is the same form the user types. */
    public String toFileFormat() {
        return hasTime ? moment.format(DATE_TIME_INPUT) : moment.format(DATE_INPUT);
    }

    /** Returns this as shown to the user, e.g. {@code Dec 02 2019} or {@code Dec 02 2019, 6:00pm}. */
    @Override
    public String toString() {
        if (!hasTime) {
            return moment.format(DATE_DISPLAY);
        }
        // toLowerCase turns the formatter's "PM" into the friendlier "pm".
        return moment.format(DATE_DISPLAY) + ", " + moment.format(TIME_DISPLAY).toLowerCase();
    }
}
