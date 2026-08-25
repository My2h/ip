package ff15.command;

import ff15.FF15Exception;
import ff15.Storage;
import ff15.Ui;
import ff15.task.Task;
import ff15.task.TaskList;
import java.io.IOException;

/** Marks a task as done. */
public class MarkCommand extends Command {
    private final int number;

    public MarkCommand(int number) {
        this.number = number;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws FF15Exception, IOException {
        Task task = tasks.get(number);
        task.markAsDone();
        storage.save(tasks);
        ui.showTask("You are cooking! I've marked this task as done:", task);
    }
}
