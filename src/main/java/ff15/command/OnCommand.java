package ff15.command;

import java.util.List;

import ff15.Storage;
import ff15.Ui;
import ff15.task.DateRange;
import ff15.task.Task;
import ff15.task.TaskList;

/** Shows the tasks falling within a day, month, or year the user asked about. */
public class OnCommand extends Command {
    private final DateRange range;

    /**
     * Creates a command that will report the tasks falling in one span of dates.
     *
     * @param range the day, month, or year the user asked about
     */
    public OnCommand(DateRange range) {
        this.range = range;
    }

    /** Shows the tasks falling within the range, or says there are none. */
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
