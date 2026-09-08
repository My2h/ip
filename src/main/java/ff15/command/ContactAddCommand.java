package ff15.command;

import java.io.IOException;

import ff15.Storage;
import ff15.Ui;
import ff15.contact.Contact;
import ff15.contact.ContactList;
import ff15.task.TaskList;

/** Adds a contact to the contact list. */
public class ContactAddCommand extends Command {
    private final Contact contact;

    /**
     * Creates a command that will add one contact.
     *
     * @param contact the contact the parser built from the user input.
     */
    public ContactAddCommand(Contact contact) {
        this.contact = contact;
    }

    /**
     * Adds the contact, saves the list, and reports what was added along with how
     * many contacts there are now.
     *
     * @throws IOException if the updated list could not be saved.
     */
    @Override
    public void execute(TaskList tasks, ContactList contacts, Ui ui, Storage storage) throws IOException {
        contacts.add(contact);
        storage.saveContacts(contacts);
        ui.showContactAdded(contact, contacts.size());
    }
}
