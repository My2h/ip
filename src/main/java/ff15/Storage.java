package ff15;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves the task list to a fixed location on disk.
 */
public class Storage {
    private static final Path FILE_PATH = Paths.get("data", "ff15.txt");

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
}
