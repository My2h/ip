package ff15.command;

import java.io.IOException;

import ff15.Storage;
import ff15.Ui;
import ff15.task.Task;
import ff15.task.TaskList;

/**
 * Adds a task to the list. Serves the todo, deadline, and event commands alike:
 * they differ in how the task was parsed, not in what happens to it afterwards.
 */
public class AddCommand extends Command {
    private final Task task;

    /**
     * Creates a command that will add {@code task} to the list.
     *
     * @param task the task the parser built from the user input
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    /**
     * Adds the task, saves the updated list, and reports what was added.
     *
     * @throws IOException if the updated list could not be saved
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws IOException {
        tasks.add(task);
        storage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }
}
