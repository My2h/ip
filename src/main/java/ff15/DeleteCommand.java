package ff15;

import java.io.IOException;

/** Removes a task from the list. */
public class DeleteCommand extends Command {
    private final int number;

    public DeleteCommand(int number) {
        this.number = number;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws FF15Exception, IOException {
        Task task = tasks.delete(number);
        storage.save(tasks);
        ui.showTaskRemoved(task, tasks.size());
    }
}
