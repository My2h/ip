/**
 * Represents a task that needs to be done before a specific date/time.
 */
public class Deadline extends Task {
    protected String date;

    public Deadline(String description, String by) {
        super(description);
        this.date = by;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + date + ")";
    }
}
