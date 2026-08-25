package ff15;

import java.time.LocalDate;

/**
 * Represents a task that starts on a specific date and ends on a specific date.
 * Both dates are held as {@link LocalDate}s so that the event can be compared
 * against a {@link DateRange}.
 */
public class Event extends Task {
    protected LocalDate from;
    protected LocalDate to;

    public Event(String description, LocalDate from, LocalDate to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * An event falls in a span when it is running for at least one day of it —
     * an event lasting a whole week shows up in a query for any day of that week,
     * not only its first or last day.
     */
    @Override
    public boolean occursIn(DateRange range) {
        return range.overlaps(from, to);
    }

    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + Dates.format(from) + " to: " + Dates.format(to) + ")";
    }

    @Override
    public String toFileFormat() {
        return "E | " + super.toFileFormat()
                + " | " + Dates.toFileFormat(from) + " | " + Dates.toFileFormat(to);
    }
}
