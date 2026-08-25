package ff15;

import java.io.IOException;

/**
 * One thing the user asked the chatbot to do.
 *
 * <p>{@link Parser} turns an input line into the matching subclass, carrying
 * whatever that command needs (a task to add, a number to delete). The subclass
 * then carries it out in {@link #execute}, so adding a new command means adding
 * a class rather than another branch to a switch.
 */
public abstract class Command {
    /**
     * Carries out this command, reporting the result through {@code ui}.
     *
     * @throws FF15Exception if the command cannot be carried out as asked
     * @throws IOException if the tasks could not be saved afterwards
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws FF15Exception, IOException;

    /** Returns whether the chatbot should stop after this command. Only exit says yes. */
    public boolean isExit() {
        return false;
    }
}
