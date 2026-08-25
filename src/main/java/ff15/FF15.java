package ff15;

import java.io.IOException;
import java.util.ArrayList;

public class FF15 {
    public static void main(String[] args) {
        Ui ui = new Ui();

        // Load whatever was saved by the previous session before greeting the user, so
        // that the tasks are already in memory by the time the first command arrives.
        ArrayList<Task> list = new ArrayList<>(); // Container To-Do List
        String loadWarning = null;
        try {
            list = Storage.load();
        } catch (IOException | FF15Exception e) {
            // A missing file is normal and loads as an empty list; anything else
            // (unreadable or corrupted file) is reported and we start fresh.
            loadWarning = "Couldn't read your saved tasks: " + e.getMessage();
        }

        ui.showWelcome(loadWarning);

        String input = ui.readCommand();
        Command command = Command.match(input);

        while (command != Command.BYE) {
            ui.startBlock();
            try {
                switch (command) {
                    case LIST -> ui.showTaskList("Here are the tasks in your list:", list);
                    case ON -> {
                        String query = argumentAfter(input, "on");
                        if (query.isEmpty()) {
                            throw new FF15Exception(
                                    "Tell me when, e.g.: on 2019-12-02, on 2019-12, or on 2019");
                        }
                        DateRange range = DateRange.parse(query);
                        ArrayList<Task> matches = tasksIn(list, range);
                        if (matches.isEmpty()) {
                            ui.showMessage("You've got nothing on " + range.getLabel() + ", bro.");
                        } else {
                            ui.showTaskList("Here are the tasks on " + range.getLabel() + ":", matches);
                        }
                    }
                    case MARK -> {
                        int number = parseTaskNumber(argumentAfter(input, "mark"), list.size());
                        Task task = list.get(number - 1);
                        task.markAsDone();
                        Storage.save(list);
                        ui.showTask("You are cooking! I've marked this task as done:", task);
                    }
                    case UNMARK -> {
                        int number = parseTaskNumber(argumentAfter(input, "unmark"), list.size());
                        Task task = list.get(number - 1);
                        task.markAsNotDone();
                        Storage.save(list);
                        ui.showTask("OK, I've marked this task as not done yet:", task);
                    }
                    case DELETE -> {
                        int number = parseTaskNumber(argumentAfter(input, "delete"), list.size());
                        Task task = list.remove(number - 1);
                        Storage.save(list);
                        ui.showTaskRemoved(task, list.size());
                    }
                    case TODO -> {
                        String description = argumentAfter(input, "todo");
                        if (description.isEmpty()) {                                                           // handle empty description for todo
                            throw new FF15Exception("The description of a todo can't be empty, bro.");
                        }
                        Task task = new Todo(description);
                        list.add(task);
                        Storage.save(list);
                        ui.showTaskAdded(task, list.size());
                    }
                    case DEADLINE -> {
                        String details = argumentAfter(input, "deadline");
                        int byIndex = details.indexOf(" /by");
                        if (byIndex == -1) {                                                                     // handle invalid date input for deadlines
                            throw new FF15Exception("A deadline needs a /by, e.g.: deadline return book /by 2019-12-02 1800");
                        }
                        String description = details.substring(0, byIndex).trim();
                        String by = details.substring(byIndex + " /by".length()).trim();
                        if (description.isEmpty()) {                                                            // handle empty description input for deadlines
                            throw new FF15Exception("The description of a deadline can't be empty, bro.");
                        }
                        if (by.isEmpty()) {                                                                     // handle empty date input for deadlines
                            throw new FF15Exception("The /by date/time of a deadline can't be empty, bro.");
                        }
                        Task task = new Deadline(description, TaskTime.parse(by));
                        list.add(task);
                        Storage.save(list);
                        ui.showTaskAdded(task, list.size());
                    }
                    case EVENT -> {
                        String details = argumentAfter(input, "event");
                        int fromIndex = details.indexOf(" /from");
                        int toIndex = details.indexOf(" /to");
                        if (fromIndex == -1 || toIndex == -1 || toIndex < fromIndex) {                    // handle invalid date input for events
                            throw new FF15Exception(
                                    "An event needs /from and /to, e.g.: event project meeting /from 2019-12-05 1400 /to 2019-12-05 1600");
                        }
                        String description = details.substring(0, fromIndex).trim();
                        String from = details.substring(fromIndex + " /from".length(), toIndex).trim();
                        String to = details.substring(toIndex + " /to".length()).trim();
                        if (description.isEmpty()) {                                                      // handle empty description input for events
                            throw new FF15Exception("The description of an event can't be empty bro.");
                        }
                        if (from.isEmpty() || to.isEmpty()) {                                             // handle empty dates input for events
                            throw new FF15Exception("The /from and /to date/times of an event can't be empty, bro.");
                        }
                        TaskTime fromDate = TaskTime.parse(from);
                        TaskTime toDate = TaskTime.parse(to);
                        if (toDate.isBefore(fromDate)) {                          // an event can't finish before it begins
                            throw new FF15Exception("An event can't end before it starts, bro.");
                        }
                        Task task = new Event(description, fromDate, toDate);
                        list.add(task);
                        Storage.save(list);
                        ui.showTaskAdded(task, list.size());
                    }
                    default -> throw new FF15Exception("I'm sorry big man, I don't know what that means :-(");
                }
            } catch (FF15Exception e) {
                ui.showError(e.getMessage());
            } catch (IOException e) {
                ui.showError("Couldn't save your tasks: " + e.getMessage());
            }
            ui.endBlock();
            input = ui.readCommand();
            command = Command.match(input);
        }

        ui.showGoodbye();
    }

    /**
     * Returns whatever follows the command word in {@code input} (trimmed), or an
     * empty string if the command word was typed with nothing after it.
     * remove command and obtain string
     */
    private static String argumentAfter(String input, String commandWord) {
        if (input.length() <= commandWord.length()) {
            return "";
        }
        return input.substring(commandWord.length() + 1).trim();
    }

    /**
     * Parses a 1-based task number typed as an argument to mark/unmark, checking that
     * it is present, numeric, and within range of the current list.
     * for mark and unmark commands
     */
    private static int parseTaskNumber(String arg, int listSize) throws FF15Exception {
        if (arg.isEmpty()) {
            throw new FF15Exception("Bro Tell me which task number, e.g. mark 2.");
        }
        int number;
        try {
            number = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new FF15Exception("'" + arg + "' aint looking like a task number.");
        }
        if (number < 1 || number > listSize) {
            throw new FF15Exception("I don't have task number " + number + ". You've got " + listSize + " task(s).");
        }
        return number;
    }

    /**
     * Returns the tasks from {@code list} that fall within {@code range}, kept in
     * list order. Todos never match, since they have no date attached.
     */
    private static ArrayList<Task> tasksIn(ArrayList<Task> list, DateRange range) {
        ArrayList<Task> matches = new ArrayList<>();
        for (Task task : list) {
            if (task.occursIn(range)) {
                matches.add(task);
            }
        }
        return matches;
    }
}
