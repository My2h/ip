package ff15;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ff15.task.Deadline;
import ff15.task.Event;
import ff15.task.Task;
import ff15.task.TaskList;
import ff15.task.TaskTime;
import ff15.task.Todo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests that {@link Storage} writes the task list somewhere it can read it back
 * from, and that a save file which has been damaged is reported rather than
 * silently misread.
 *
 * <p>Every test writes inside a temporary folder supplied by JUnit, so none of
 * them can touch the real {@code data/ff15.txt}.
 */
public class StorageTest {

    @TempDir
    Path tempDir;

    private Storage storageAt(String... pathParts) {
        Path path = tempDir;
        for (String part : pathParts) {
            path = path.resolve(part);
        }
        return new Storage(path.toString());
    }

    private Path fileAt(String... pathParts) {
        Path path = tempDir;
        for (String part : pathParts) {
            path = path.resolve(part);
        }
        return path;
    }

    /** Writes {@code lines} straight into a save file, standing in for an earlier run. */
    private Storage givenSaveFile(String... lines) throws IOException {
        Files.write(fileAt("ff15.txt"), List.of(lines));
        return storageAt("ff15.txt");
    }

    // --- writing and reading back --------------------------------------------

    @Test
    public void load_fileDoesNotExist_returnsEmptyList() throws Exception {
        assertTrue(storageAt("never-written.txt").load().isEmpty());
    }

    @Test
    public void load_emptyFile_returnsEmptyList() throws Exception {
        assertTrue(givenSaveFile().load().isEmpty());
    }

    @Test
    public void save_parentFolderMissing_createsIt() throws Exception {
        Storage storage = storageAt("data", "ff15.txt");
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        storage.save(tasks);

        assertTrue(Files.exists(fileAt("data", "ff15.txt")));
        assertEquals(List.of("T | 0 | read book"), Files.readAllLines(fileAt("data", "ff15.txt")));
    }

    @Test
    public void saveThenLoad_everyTaskType_roundTripsUnchanged() throws Exception {
        Storage storage = storageAt("ff15.txt");
        TaskList original = new TaskList();
        original.add(new Todo("read book"));
        original.add(new Deadline("return book", TaskTime.parse("2019-12-02")));
        original.add(new Deadline("submit report", TaskTime.parse("2019-12-02 1800")));
        original.add(new Event("holiday", TaskTime.parse("2019-12-20"), TaskTime.parse("2019-12-26")));
        original.add(new Event("meeting", TaskTime.parse("2019-12-05 1400"), TaskTime.parse("2019-12-05 1600")));

        storage.save(original);
        TaskList reloaded = new TaskList(storage.load());

        assertEquals(original.size(), reloaded.size());
        for (int number = 1; number <= original.size(); number++) {
            assertEquals(original.get(number).toString(), reloaded.get(number).toString());
        }
    }

    @Test
    public void saveThenLoad_dateOnlyTask_doesNotGainAMidnightTime() throws Exception {
        Storage storage = storageAt("ff15.txt");
        TaskList tasks = new TaskList();
        tasks.add(new Deadline("return book", TaskTime.parse("2019-12-02")));

        storage.save(tasks);

        assertEquals("[D][ ] return book (by: Dec 02 2019)", storage.load().get(0).toString());
    }

    @Test
    public void saveThenLoad_doneTasks_keepTheirStatus() throws Exception {
        Storage storage = storageAt("ff15.txt");
        TaskList tasks = new TaskList();
        Task done = new Todo("read book");
        done.markAsDone();
        tasks.add(done);
        tasks.add(new Todo("not done yet"));

        storage.save(tasks);
        ArrayList<Task> reloaded = storage.load();

        assertEquals("[T][X] read book", reloaded.get(0).toString());
        assertEquals("[T][ ] not done yet", reloaded.get(1).toString());
    }

    @Test
    public void save_emptyList_leavesAnEmptyFile() throws Exception {
        Storage storage = storageAt("ff15.txt");
        storage.save(new TaskList());
        assertTrue(Files.exists(fileAt("ff15.txt")));
        assertTrue(Files.readAllLines(fileAt("ff15.txt")).isEmpty());
    }

    @Test
    public void save_calledAgain_replacesTheEarlierContents() throws Exception {
        Storage storage = storageAt("ff15.txt");
        TaskList tasks = new TaskList();
        tasks.add(new Todo("first"));
        storage.save(tasks);

        tasks.delete(1);
        tasks.add(new Todo("second"));
        storage.save(tasks);

        assertEquals(List.of("T | 0 | second"), Files.readAllLines(fileAt("ff15.txt")));
    }

    @Test
    public void save_descriptionContainingTheSeparator_losesTheTextAfterIt() throws Exception {
        // Documents today's behaviour: a description holding " | " gains a field when
        // it is read back, so the extra text lands in the wrong place rather than
        // being kept as part of the description.
        Storage storage = storageAt("ff15.txt");
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book | and take notes"));

        storage.save(tasks);

        assertEquals("read book", storage.load().get(0).toString().substring("[T][ ] ".length()));
    }

    // --- reading a file that is not quite right ------------------------------

    @Test
    public void load_blankLines_areSkipped() throws Exception {
        Storage storage = givenSaveFile("T | 0 | read book", "", "   ", "T | 1 | return book");
        ArrayList<Task> tasks = storage.load();
        assertEquals(2, tasks.size());
        assertEquals("[T][ ] read book", tasks.get(0).toString());
        assertEquals("[T][X] return book", tasks.get(1).toString());
    }

    @Test
    public void load_unknownTaskType_throwsException() throws Exception {
        Storage storage = givenSaveFile("X | 0 | mystery");
        FF15Exception thrown = assertThrows(FF15Exception.class, storage::load);
        assertTrue(thrown.getMessage().contains("X"), thrown.getMessage());
    }

    @Test
    public void load_lineWithTooFewFields_throwsException() throws Exception {
        assertThrows(FF15Exception.class, () -> givenSaveFile("T | 0").load());
        assertThrows(FF15Exception.class, () -> givenSaveFile("just some text").load());
    }

    @Test
    public void load_deadlineMissingItsDate_throwsException() throws Exception {
        assertThrows(FF15Exception.class, () -> givenSaveFile("D | 0 | return book").load());
    }

    @Test
    public void load_eventMissingItsEndDate_throwsException() throws Exception {
        assertThrows(FF15Exception.class, () -> givenSaveFile("E | 0 | meeting | 2019-12-05").load());
    }

    @Test
    public void load_unreadableDate_throwsException() throws Exception {
        assertThrows(FF15Exception.class, () -> givenSaveFile("D | 0 | return book | sunday").load());
    }

    @Test
    public void load_doneFlagThatIsNotOne_readsAsNotDone() throws Exception {
        assertEquals("[T][ ] read book", givenSaveFile("T | 0 | read book").load().get(0).toString());
        assertEquals("[T][ ] read book", givenSaveFile("T | 2 | read book").load().get(0).toString());
    }

    @Test
    public void load_damagedLineAfterAGoodOne_throwsRatherThanKeepingHalfTheFile() throws Exception {
        Storage storage = givenSaveFile("T | 0 | read book", "D | 0 | broken");
        assertThrows(FF15Exception.class, storage::load);
    }
}
