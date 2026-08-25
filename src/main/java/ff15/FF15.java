package ff15;

import java.io.IOException;
import java.util.List;

/**
 * The chatbot itself: it wires together the four parts of the program and runs
 * the command loop.
 *
 * <p>{@link Ui} talks to the user, {@link Parser} makes sense of what they type,
 * {@link TaskList} holds the tasks, and {@link Storage} keeps them on disk. This
 * class owns one of each and does nothing but pass work between them.
 */
public class FF15 {
    /** Where the tasks are kept between sessions. */
    private static final String DATA_FILE = "data/ff15.txt";

    private final Ui ui;
    private final Storage storage;
    private TaskList tasks;

    /**
     * Greets the user and loads the tasks saved at {@code filePath}. A missing
     * file is normal and starts an empty list; a file that cannot be read or
     * understood is reported, and the session starts empty rather than stopping.
     */
    public FF15(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);

        ui.startBlock();
        ui.showWelcome();
        try {
            tasks = new TaskList(storage.load());
        } catch (IOException | FF15Exception e) {
            ui.showLoadingError(e.getMessage());
            tasks = new TaskList();
        }
        ui.endBlock();
    }

    /** Reads commands and carries them out until the user says bye. */
    public void run() {
        String input = ui.readCommand();
        Command command = Command.match(input);

        while (command != Command.BYE) {
            ui.startBlock();
            try {
                execute(command, input);
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
     * Carries out one command, reporting the result through the Ui.
     *
     * @throws FF15Exception if the command was typed wrongly
     * @throws IOException if the tasks could not be saved afterwards
     */
    private void execute(Command command, String input) throws FF15Exception, IOException {
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
                Task task = tasks.get(Parser.parseTaskNumber(input, Command.MARK, tasks.size()));
                task.markAsDone();
                storage.save(tasks);
                ui.showTask("You are cooking! I've marked this task as done:", task);
            }
            case UNMARK -> {
                Task task = tasks.get(Parser.parseTaskNumber(input, Command.UNMARK, tasks.size()));
                task.markAsNotDone();
                storage.save(tasks);
                ui.showTask("OK, I've marked this task as not done yet:", task);
            }
            case DELETE -> {
                Task task = tasks.delete(Parser.parseTaskNumber(input, Command.DELETE, tasks.size()));
                storage.save(tasks);
                ui.showTaskRemoved(task, tasks.size());
            }
            case TODO -> addTask(Parser.parseTodo(input));
            case DEADLINE -> addTask(Parser.parseDeadline(input));
            case EVENT -> addTask(Parser.parseEvent(input));
            default -> throw new FF15Exception("I'm sorry big man, I don't know what that means :-(");
        }
    }

    /**
     * Adds a freshly parsed task, saves the list, and confirms it. Shared by the
     * todo, deadline, and event commands, which differ only in how the task was
     * parsed, not in what happens to it afterwards.
     */
    private void addTask(Task task) throws IOException {
        tasks.add(task);
        storage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }

    public static void main(String[] args) {
        new FF15(DATA_FILE).run();
    }
}
