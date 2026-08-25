package ff15;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Saves the task list to a fixed location on disk, and loads it back again.
 */
public class Storage {
    private static final Path FILE_PATH = Paths.get("data", "ff15.txt");

    /** Separator between the fields of one saved task, as written by {@link Task#toFileFormat()}. */
    private static final String FIELD_SEPARATOR = " | ";

    /**
     * Writes {@code tasks} to the data file, one task per line, creating the
     * containing folder first if it does not already exist.
     */
    public static void save(List<Task> tasks) throws IOException {
        Files.createDirectories(FILE_PATH.getParent());
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(task.toFileFormat());
        }
        Files.write(FILE_PATH, lines);
    }

    /**
     * Reads the tasks saved by {@link #save(List)} back into a list.
     * Returns an empty list if the data file does not exist yet, which is the
     * normal situation on the very first run.
     *
     * @throws IOException if the file exists but cannot be read
     * @throws FF15Exception if a line in the file is not in the expected save format
     */
    public static ArrayList<Task> load() throws IOException, FF15Exception {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.exists(FILE_PATH)) {
            return tasks;
        }
        for (String line : Files.readAllLines(FILE_PATH)) {
            if (line.isBlank()) {   // ignore stray empty lines rather than failing on them
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
        requireFieldCount(fields, 3, line);      // every task saves at least: type, done flag, description
        String type = fields[0];
        boolean isDone = fields[1].equals("1");
        String description = fields[2];

        Task task;
        switch (type) {
            case "T" -> task = new Todo(description);
            case "D" -> {
                requireFieldCount(fields, 4, line);      // plus the /by date
                task = new Deadline(description, TaskTime.parse(fields[3]));
            }
            case "E" -> {
                requireFieldCount(fields, 5, line);      // plus the /from and /to date/times
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
