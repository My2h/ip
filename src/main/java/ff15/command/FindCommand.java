package ff15.command;

import java.util.List;

import ff15.Storage;
import ff15.Ui;
import ff15.task.Task;
import ff15.task.TaskList;

/** Shows the tasks whose description contains a keyword the user asked about. */
public class FindCommand extends Command {
    private final String keyword;

    /**
     * Creates a command that will report the tasks matching one keyword.
     *
     * @param keyword the text to look for in each task description.
     */
    public FindCommand(String keyword) {
        this.keyword = keyword;
    }

    /** Shows the tasks matching the keyword, or says there are none. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        List<Task> matches = tasks.find(keyword);
        if (matches.isEmpty()) {
            ui.showMessage("You've got nothing matching '" + keyword + "', bro.");
        } else {
            ui.showTaskList("Here are the matching tasks in your list:", matches);
        }
    }
}
