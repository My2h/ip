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
                        DateRange range = Parser.parseDateQuery(input);
                        ArrayList<Task> matches = tasksIn(list, range);
                        if (matches.isEmpty()) {
                            ui.showMessage("You've got nothing on " + range.getLabel() + ", bro.");
                        } else {
                            ui.showTaskList("Here are the tasks on " + range.getLabel() + ":", matches);
                        }
                    }
                    case MARK -> {
                        int number = Parser.parseTaskNumber(input, Command.MARK, list.size());
                        Task task = list.get(number - 1);
                        task.markAsDone();
                        Storage.save(list);
                        ui.showTask("You are cooking! I've marked this task as done:", task);
                    }
                    case UNMARK -> {
                        int number = Parser.parseTaskNumber(input, Command.UNMARK, list.size());
                        Task task = list.get(number - 1);
                        task.markAsNotDone();
                        Storage.save(list);
                        ui.showTask("OK, I've marked this task as not done yet:", task);
                    }
                    case DELETE -> {
                        int number = Parser.parseTaskNumber(input, Command.DELETE, list.size());
                        Task task = list.remove(number - 1);
                        Storage.save(list);
                        ui.showTaskRemoved(task, list.size());
                    }
                    case TODO -> addTask(Parser.parseTodo(input), list, ui);
                    case DEADLINE -> addTask(Parser.parseDeadline(input), list, ui);
                    case EVENT -> addTask(Parser.parseEvent(input), list, ui);
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
     * Adds a freshly parsed task to the list, saves the list, and confirms it.
     * Shared by the todo, deadline, and event commands, which differ only in how
     * the task was parsed, not in what happens to it afterwards.
     */
    private static void addTask(Task task, ArrayList<Task> list, Ui ui) throws IOException {
        list.add(task);
        Storage.save(list);
        ui.showTaskAdded(task, list.size());
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
