package ff15;

import java.util.List;
import java.util.Scanner;

/**
 * Deals with everything the user sees and types: reading commands from the
 * keyboard and printing the chatbot's replies.
 *
 * <p>Gathering it here means the rest of the program never touches
 * {@code System.in} or {@code System.out}, so the wording, the indentation, and
 * the divider lines can all be changed without touching any task logic.
 *
 * <p>Replies are printed in blocks: a divider line, the message lines, then a
 * closing divider and a blank line separating the block from the next one.
 */
public class Ui {
    private static final String DIVIDER = "____________________________________________________________";

    private static final String BANNER = " _____ _____ _  ____  \n"
            + "|  ___|  ___/ |/ ___| \n"
            + "| |_  | |_  | |\\___ \\ \n"
            + "|  _| |  _| | | ___) |\n"
            + "|_|   |_|   |_||____/ \n";

    private final Scanner scanner;

    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    /** Reads the next line the user types. */
    public String readCommand() {
        return scanner.nextLine();
    }

    /** Opens a block of output with a divider line. */
    public void startBlock() {
        printDivider();
    }

    /** Closes a block of output, leaving a blank line before the next one. */
    public void endBlock() {
        printDivider();
        System.out.println();
    }

    /**
     * Prints the banner and greeting as one block.
     *
     * @param loadWarning a problem met while loading the save file, reported
     *     under the greeting; null when the tasks loaded cleanly
     */
    public void showWelcome(String loadWarning) {
        startBlock();
        System.out.println(BANNER);
        showMessage("Eh hello bro, I'm FF15 !");
        showMessage("What can I do for you big man ?");
        if (loadWarning != null) {
            showError(loadWarning);
            showMessage("Starting you off with an empty list.");
        }
        endBlock();
    }

    /** Prints the farewell as the final block, with no blank line after it. */
    public void showGoodbye() {
        startBlock();
        showMessage("Okok bye bye, see you again soon !");
        printDivider();
    }

    /** Prints one line of the chatbot's reply. */
    public void showMessage(String message) {
        System.out.println("     " + message);
    }

    /** Prints something that went wrong, tagged so it stands out from ordinary replies. */
    public void showError(String message) {
        showMessage("AYY!!! " + message);
    }

    /** Prints a heading followed by the tasks under it, numbered from 1. */
    public void showTaskList(String header, List<Task> tasks) {
        showMessage(header);
        for (int i = 0; i < tasks.size(); i++) {
            showMessage((i + 1) + "." + tasks.get(i));
        }
    }

    /** Prints a heading followed by the single task it refers to. */
    public void showTask(String header, Task task) {
        showMessage(header);
        showMessage("  " + task);
    }

    /** Confirms a newly added task and how many tasks there are now. */
    public void showTaskAdded(Task task, int taskCount) {
        showTask("Got it. I've added this task:", task);
        showTaskCount(taskCount);
    }

    /** Confirms a deleted task and how many tasks are left. */
    public void showTaskRemoved(Task task, int taskCount) {
        showTask("Noted. I've removed this task:", task);
        showTaskCount(taskCount);
    }

    private void showTaskCount(int taskCount) {
        showMessage("Now you have " + taskCount + " tasks in the list.");
    }

    private void printDivider() {
        System.out.println("    " + DIVIDER);
    }
}
