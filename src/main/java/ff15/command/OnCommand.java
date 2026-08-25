package ff15.command;

import ff15.Storage;
import ff15.Ui;
import ff15.task.DateRange;
import ff15.task.Task;
import ff15.task.TaskList;
import java.util.List;

/** Shows the tasks falling within a day, month, or year the user asked about. */
public class OnCommand extends Command {
    private final DateRange range;

    public OnCommand(DateRange range) {
        this.range = range;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        List<Task> matches = tasks.tasksIn(range);
        if (matches.isEmpty()) {
            ui.showMessage("You've got nothing on " + range.getLabel() + ", bro.");
        } else {
            ui.showTaskList("Here are the tasks on " + range.getLabel() + ":", matches);
        }
    }
}
