package ff15.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ff15.FF15Exception;

/**
 * Holds the tasks the user is keeping track of, and the operations that add to,
 * remove from, and search that collection.
 *
 * <p>Task numbers are 1-based here, matching what the user types and what the
 * list prints, so the conversion to the ArrayList's 0-based index happens inside
 * this class instead of at every call site.
 */
public class TaskList {
    private final ArrayList<Task> tasks;

    /** Creates an empty list, used when there is nothing saved to start from. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /** Creates a list holding {@code tasks}, typically the ones just read from disk. */
    public TaskList(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /** Returns how many tasks the list holds. */
    public int size() {
        return tasks.size();
    }

    /** Adds {@code task} to the end of the list. */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Returns the task the user knows as {@code number}, counting from 1.
     *
     * @throws FF15Exception if there is no such task
     */
    public Task get(int number) throws FF15Exception {
        checkNumber(number);
        return tasks.get(number - 1);
    }

    /**
     * Removes and returns the task the user knows as {@code number}, counting from 1.
     *
     * @throws FF15Exception if there is no such task
     */
    public Task delete(int number) throws FF15Exception {
        checkNumber(number);
        return tasks.remove(number - 1);
    }

    /** Rejects a task number that does not name a task in this list. */
    private void checkNumber(int number) throws FF15Exception {
        if (number < 1 || number > tasks.size()) {
            throw new FF15Exception("I don't have task number " + number
                    + ". You've got " + tasks.size() + " task(s).");
        }
    }

    /**
     * Returns the tasks falling within {@code range}, kept in list order.
     * Todos never match, since they have no date attached.
     */
    public List<Task> tasksIn(DateRange range) {
        List<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task.occursIn(range)) {
                matches.add(task);
            }
        }
        return matches;
    }

    /**
     * Returns the tasks for reading only, e.g. to print them or write them to
     * disk. The view cannot be modified, so nothing outside this class can change
     * the list behind its back.
     */
    public List<Task> asList() {
        return Collections.unmodifiableList(tasks);
    }
}
