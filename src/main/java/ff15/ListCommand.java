package ff15;

/** Shows every task in the list, in order. */
public class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTaskList("Here are the tasks in your list:", tasks.asList());
    }
}
