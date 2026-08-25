package ff15;

/**
 * Represents a task that needs to be done before a specific date, optionally at
 * a specific time of day. The moment is held as a {@link TaskTime} rather than
 * plain text, so that it is validated when it is typed and can be compared
 * against a {@link DateRange}.
 */
public class Deadline extends Task {
    protected TaskTime date;

    public Deadline(String description, TaskTime by) {
        super(description);
        this.date = by;
    }

    /** A deadline falls in a span when the day it is due does. */
    @Override
    public boolean occursIn(DateRange range) {
        return range.includes(date.getDate());
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + date + ")";
    }

    @Override
    public String toFileFormat() {
        return "D | " + super.toFileFormat() + " | " + date.toFileFormat();
    }
}
