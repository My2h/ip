package ff15;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ff15.contact.Contact;
import ff15.contact.ContactList;
import ff15.task.Deadline;
import ff15.task.Event;
import ff15.task.Task;
import ff15.task.TaskList;
import ff15.task.TaskTime;
import ff15.task.Todo;

/**
 * Saves the task list and the contact list to files on disk, and loads them back
 * again. The locations are given when the Storage is created, so the class holds
 * no opinion about where the data lives.
 *
 * <p>The two lists are kept in separate files, so the format of one is free to
 * change without disturbing the other, and a save file written before contacts
 * existed still loads unchanged.
 */
public class Storage {
    /** Separator between the fields of one saved task, as written by {@link Task#toFileFormat()}. */
    private static final String FIELD_SEPARATOR = " | ";

    /** Fields every saved task carries: type letter, done flag, and description. */
    private static final int COMMON_FIELD_COUNT = 3;

    /** Fields a saved deadline carries: the common ones, plus its /by date. */
    private static final int DEADLINE_FIELD_COUNT = 4;

    /** Fields a saved event carries: the common ones, plus its /from and /to date/times. */
    private static final int EVENT_FIELD_COUNT = 5;

    /** Fields every saved contact carries: name, phone, and email. */
    private static final int CONTACT_FIELD_COUNT = 3;

    private final Path filePath;
    private final Path contactFilePath;

    /**
     * Creates a Storage reading and writing tasks at {@code filePath} and contacts
     * at {@code contactFilePath}, e.g. {@code data/ff15.txt} and
     * {@code data/contacts.txt}.
     */
    public Storage(String filePath, String contactFilePath) {
        this.filePath = Paths.get(filePath);
        this.contactFilePath = Paths.get(contactFilePath);
    }

    /**
     * Writes {@code tasks} to the data file, one task per line, creating the
     * containing folder first if it does not already exist.
     */
    public void save(TaskList tasks) throws IOException {
        writeLines(filePath, tasks.asList().stream()
                .map(Task::toFileFormat)
                .toList());
    }

    /**
     * Writes {@code contacts} to the contacts file, one contact per line, creating
     * the containing folder first if it does not already exist.
     */
    public void saveContacts(ContactList contacts) throws IOException {
        writeLines(contactFilePath, contacts.asList().stream()
                .map(Contact::toFileFormat)
                .toList());
    }

    /** Writes {@code lines} to {@code path}, creating the containing folder first if needed. */
    private static void writeLines(Path path, List<String> lines) throws IOException {
        Path folder = path.getParent();
        if (folder != null) { // null when the file sits in the working directory
            Files.createDirectories(folder);
        }
        Files.write(path, lines);
    }

    /**
     * Reads the tasks saved by {@link #save(TaskList)} back into a list.
     * Returns an empty list if the data file does not exist yet, which is the
     * normal situation on the very first run.
     *
     * @throws IOException if the file exists but cannot be read.
     * @throws FF15Exception if a line in the file is not in the expected save format.
     */
    public ArrayList<Task> load() throws IOException, FF15Exception {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return tasks;
        }
        for (String line : Files.readAllLines(filePath)) {
            if (line.isBlank()) { // ignore stray empty lines rather than failing on them
                continue;
            }
            tasks.add(parseTask(line));
        }
        return tasks;
    }

    /**
     * Turns one saved line back into the Task it came from, e.g.
     * {@code "D | 1 | return book | Sunday"} becomes a done {@link Deadline}.
     * This is the reverse of {@link Task#toFileFormat()}.
     */
    private static Task parseTask(String line) throws FF15Exception {
        // Pattern.quote treats the separator as plain text, since "|" means "or" in a regex.
        String[] fields = line.split(Pattern.quote(FIELD_SEPARATOR));
        requireFieldCount(fields, COMMON_FIELD_COUNT, line);
        assert fields.length >= COMMON_FIELD_COUNT
                : "requireFieldCount let through a short saved line: " + line;
        String type = fields[0];
        boolean isDone = fields[1].equals("1");
        String description = fields[2];

        Task task;
        switch (type) {
            case "T" -> task = new Todo(description);
            case "D" -> {
                requireFieldCount(fields, DEADLINE_FIELD_COUNT, line);
                task = new Deadline(description, TaskTime.parse(fields[3]));
            }
            case "E" -> {
                requireFieldCount(fields, EVENT_FIELD_COUNT, line);
                task = new Event(description, TaskTime.parse(fields[3]), TaskTime.parse(fields[4]));
            }
            default -> throw new FF15Exception("I don't recognise the task type '" + type
                    + "' on this saved line: " + line);
        }
        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Reads the contacts saved by {@link #saveContacts(ContactList)} back into a
     * list. Returns an empty list if the contacts file does not exist yet, which
     * is the normal situation until the first contact is added.
     *
     * @throws IOException if the file exists but cannot be read.
     * @throws FF15Exception if a line in the file is not in the expected save format.
     */
    public ArrayList<Contact> loadContacts() throws IOException, FF15Exception {
        ArrayList<Contact> contacts = new ArrayList<>();
        if (!Files.exists(contactFilePath)) {
            return contacts;
        }
        for (String line : Files.readAllLines(contactFilePath)) {
            if (line.isBlank()) { // ignore stray empty lines rather than failing on them
                continue;
            }
            contacts.add(parseContact(line));
        }
        return contacts;
    }

    /**
     * Turns one saved line back into the Contact it came from, e.g.
     * {@code "John | 91234567 | john@example.com"} becomes a {@link Contact} with
     * all three fields. This is the reverse of {@link Contact#toFileFormat()}.
     */
    private static Contact parseContact(String line) throws FF15Exception {
        // The -1 limit keeps trailing empty fields, which a contact saved without an
        // email has. Without it, "Alex |  | " would split into a single field.
        String[] fields = line.split(Pattern.quote(FIELD_SEPARATOR), -1);
        requireFieldCount(fields, CONTACT_FIELD_COUNT, line);
        return new Contact(fields[0].trim(), fields[1].trim(), fields[2].trim());
    }

    /**
     * Checks that a saved line was split into at least {@code expected} fields,
     * so that reading a truncated line reports a clear error instead of throwing
     * an ArrayIndexOutOfBoundsException.
     */
    private static void requireFieldCount(String[] fields, int expected, String line) throws FF15Exception {
        if (fields.length < expected) {
            throw new FF15Exception("This saved line is missing some fields: " + line);
        }
    }
}
