package ff15;

import java.io.IOException;

/**
 * Adds a task to the list. Serves the todo, deadline, and event commands alike:
 * they differ in how the task was parsed, not in what happens to it afterwards.
 */
public class AddCommand extends Command {
    private final Task task;

    public AddCommand(Task task) {
        this.task = task;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws IOException {
        tasks.add(task);
        storage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }
}
