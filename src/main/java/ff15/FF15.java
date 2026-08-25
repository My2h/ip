package ff15;

import java.io.IOException;
import java.util.List;

public class FF15 {
    public static void main(String[] args) {
        Ui ui = new Ui();

        // Load whatever was saved by the previous session before greeting the user, so
        // that the tasks are already in memory by the time the first command arrives.
        TaskList tasks = new TaskList();
        String loadWarning = null;
        try {
            tasks = new TaskList(Storage.load());
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
                    case LIST -> ui.showTaskList("Here are the tasks in your list:", tasks.asList());
                    case ON -> {
                        DateRange range = Parser.parseDateQuery(input);
                        List<Task> matches = tasks.tasksIn(range);
                        if (matches.isEmpty()) {
                            ui.showMessage("You've got nothing on " + range.getLabel() + ", bro.");
                        } else {
                            ui.showTaskList("Here are the tasks on " + range.getLabel() + ":", matches);
                        }
                    }
                    case MARK -> {
                        int number = Parser.parseTaskNumber(input, Command.MARK, tasks.size());
                        Task task = tasks.get(number);
                        task.markAsDone();
                        Storage.save(tasks);
                        ui.showTask("You are cooking! I've marked this task as done:", task);
                    }
                    case UNMARK -> {
                        int number = Parser.parseTaskNumber(input, Command.UNMARK, tasks.size());
                        Task task = tasks.get(number);
                        task.markAsNotDone();
                        Storage.save(tasks);
                        ui.showTask("OK, I've marked this task as not done yet:", task);
                    }
                    case DELETE -> {
                        int number = Parser.parseTaskNumber(input, Command.DELETE, tasks.size());
                        Task task = tasks.delete(number);
                        Storage.save(tasks);
                        ui.showTaskRemoved(task, tasks.size());
                    }
                    case TODO -> addTask(Parser.parseTodo(input), tasks, ui);
                    case DEADLINE -> addTask(Parser.parseDeadline(input), tasks, ui);
                    case EVENT -> addTask(Parser.parseEvent(input), tasks, ui);
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
    private static void addTask(Task task, TaskList tasks, Ui ui) throws IOException {
        tasks.add(task);
        Storage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }

}
