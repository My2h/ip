package ff15;

import java.io.IOException;

/** Marks a task as not done again. */
public class UnmarkCommand extends Command {
    private final int number;

    public UnmarkCommand(int number) {
        this.number = number;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws FF15Exception, IOException {
        Task task = tasks.get(number);
        task.markAsNotDone();
        storage.save(tasks);
        ui.showTask("OK, I've marked this task as not done yet:", task);
    }
}
