package ff15.command;

import ff15.Storage;
import ff15.Ui;
import ff15.contact.ContactList;
import ff15.task.TaskList;

/** Shows every task in the list, in order. */
public class ListCommand extends Command {
    /** Prints every task in the list, numbered from 1. */
    @Override
    public void execute(TaskList tasks, ContactList contacts, Ui ui, Storage storage) {
        ui.showTaskList("Here are the tasks in your list:", tasks.asList());
    }
}
