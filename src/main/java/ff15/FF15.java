package ff15;

import java.io.IOException;

import ff15.command.Command;
import ff15.task.TaskList;

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

    /** Whether the last command handed to {@link #getResponse(String)} ended the session. */
    private boolean isFinished;

    /** Greets the user and loads the tasks kept in the default save file. */
    public FF15() {
        this(DATA_FILE);
    }

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

    /**
     * Reads commands and carries them out until the user says bye. Each command
     * decides for itself what to do; this loop only frames the output, hands the
     * command the three things it may need, and reports anything that went wrong.
     */
    public void run() {
        boolean isExit = false;
        while (!isExit) {
            String input = ui.readCommand();
            ui.startBlock();
            try {
                Command command = Parser.parse(input);
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (FF15Exception e) {
                ui.showError(e.getMessage());
            } catch (IOException e) {
                ui.showError("Couldn't save your tasks: " + e.getMessage());
            }
            if (isExit) {
                ui.endFinalBlock();
            } else {
                ui.endBlock();
            }
        }
    }

    /**
     * Returns the greeting for a new session, which the constructor has already
     * put together: the welcome, plus a loading error if the save file could not
     * be read.
     */
    public String getStartupMessage() {
        return ui.drainTranscript();
    }

    /**
     * Returns whether the last command carried out ended the session, so a caller
     * showing a window knows when to shut it. The console session reads the same
     * answer from the command itself, in {@link #run()}.
     */
    public boolean isFinished() {
        return isFinished;
    }

    /**
     * Carries out one command and returns what the chatbot would have said. This
     * is the same work the loop in {@link #run()} does for one line of input,
     * with the reply handed back instead of being left on the console.
     */
    public String getResponse(String input) {
        try {
            Command command = Parser.parse(input);
            command.execute(tasks, ui, storage);
            isFinished = command.isExit();
        } catch (FF15Exception e) {
            ui.showError(e.getMessage());
        } catch (IOException e) {
            ui.showError("Couldn't save your tasks: " + e.getMessage());
        }
        return ui.drainTranscript();
    }

    /**
     * Starts the chatbot, reading any previously saved tasks from
     * {@code data/ff15.txt} relative to the working directory.
     *
     * @param args ignored; the data file location is fixed
     */
    public static void main(String[] args) {
        new FF15(DATA_FILE).run();
    }
}
