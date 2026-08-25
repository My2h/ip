package ff15;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     * The number is expected to have been range-checked already by
     * {@link Parser#parseTaskNumber(String, CommandWord, int)}.
     */
    public Task get(int number) {
        return tasks.get(number - 1);
    }

    /** Removes and returns the task the user knows as {@code number}, counting from 1. */
    public Task delete(int number) {
        return tasks.remove(number - 1);
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
