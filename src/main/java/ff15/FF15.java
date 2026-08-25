package ff15;

import java.io.IOException;

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

    public static void main(String[] args) {
        new FF15(DATA_FILE).run();
    }
}
