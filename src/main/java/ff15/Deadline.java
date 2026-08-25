package ff15;

import java.time.LocalDate;

/**
 * Represents a task that needs to be done before a specific date.
 * The date is held as a {@link LocalDate} rather than plain text, so that it is
 * validated when it is typed and can be compared against a {@link DateRange}.
 */
public class Deadline extends Task {
    protected LocalDate date;

    public Deadline(String description, LocalDate by) {
        super(description);
        this.date = by;
    }

    /** A deadline falls in a span when the day it is due does. */
    @Override
    public boolean occursIn(DateRange range) {
        return range.includes(date);
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + Dates.format(date) + ")";
    }

    @Override
    public String toFileFormat() {
        return "D | " + super.toFileFormat() + " | " + Dates.toFileFormat(date);
    }
}
