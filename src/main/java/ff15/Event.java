package ff15;

/**
 * Represents a task that starts on a specific date and ends on a specific date,
 * either of which may also carry a time of day. Both are held as
 * {@link TaskTime}s so that the event can be compared against a {@link DateRange}.
 */
public class Event extends Task {
    protected TaskTime from;
    protected TaskTime to;

    public Event(String description, TaskTime from, TaskTime to) {
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
        return range.overlaps(from.getDate(), to.getDate());
    }

    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + from + " to: " + to + ")";
    }

    @Override
    public String toFileFormat() {
        return "E | " + super.toFileFormat()
                + " | " + from.toFileFormat() + " | " + to.toFileFormat();
    }
}
