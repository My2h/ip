package ff15.task;

/**
 * Represents a task that a user wants to keep track of.
 * Each task has a description and a done/not-done status.
 */
public class Task {
    protected String description;
    protected boolean isDone;

    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the icon used to display this task's status: "X" if done, otherwise a blank space.
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " ");
    }

    public void markAsDone() {
        isDone = true;
    }

    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Returns whether this task falls within {@code range}. A plain task has no
     * date attached, so it never does; {@link Deadline} and {@link Event} override
     * this with their own answer.
     */
    public boolean occursIn(DateRange range) {
        return false;
    }

    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }

    /**
     * Returns this task's representation for the save file, e.g. {@code "1 | read book"}.
     * Subclasses prepend a type letter and append any extra fields of their own.
     */
    public String toFileFormat() {
        return (isDone ? "1" : "0") + " | " + description;
    }
}
