package ff15.command;

import ff15.Storage;
import ff15.Ui;
import ff15.task.TaskList;
/** Says goodbye and ends the session. */
public class ExitCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showFarewell();
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
