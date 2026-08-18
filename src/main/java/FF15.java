import java.util.ArrayList;
import java.util.Scanner;

public class FF15 {
    public static void main(String[] args) {
        String banner = " _____ _____ _  ____  \n"
                + "|  ___|  ___/ |/ ___| \n"
                + "| |_  | |_  | |\\___ \\ \n"
                + "|  _| |  _| | | ___) |\n"
                + "|_|   |_|   |_||____/ \n";
        String line = "____________________________________________________________";

        printDivider(line);
        System.out.println(banner);
        printMessage("Eh hello bro, I'm FF15 !");
        printMessage("What can I do for you big man ?");
        printDivider(line);
        System.out.println();

        Scanner scanner = new Scanner(System.in); // Scanner object to receive input
        String input = scanner.nextLine();
        Command command = Command.match(input);
        ArrayList<Task> list = new ArrayList<>(); // Container To-Do List

        while (command != Command.BYE) {
            printDivider(line);
            try {
                switch (command) {
                    case LIST -> {
                        printMessage("Here are the tasks in your list:");
                        for (int i = 0; i < list.size(); i++) {
                            printMessage((i + 1) + "." + list.get(i));
                        }
                    }
                    case MARK -> {
                        int number = parseTaskNumber(argumentAfter(input, "mark"), list.size());
                        Task task = list.get(number - 1);
                        task.markAsDone();
                        printMessage("You are cooking! I've marked this task as done:");
                        printMessage("  " + task);
                    }
                    case UNMARK -> {
                        int number = parseTaskNumber(argumentAfter(input, "unmark"), list.size());
                        Task task = list.get(number - 1);
                        task.markAsNotDone();
                        printMessage("OK, I've marked this task as not done yet:");
                        printMessage("  " + task);
                    }
                    case DELETE -> {
                        int number = parseTaskNumber(argumentAfter(input, "delete"), list.size());
                        Task task = list.remove(number - 1);
                        printMessage("Noted. I've removed this task:");
                        printMessage("  " + task);
                        printMessage("Now you have " + list.size() + " tasks in the list.");
                    }
                    case TODO -> {
                        String description = argumentAfter(input, "todo");
                        if (description.isEmpty()) {                                                           // handle empty description for todo
                            throw new FF15Exception("The description of a todo can't be empty, bro.");
                        }
                        Task task = new Todo(description);
                        list.add(task);
                        printTaskAdded(task, list);
                    }
                    case DEADLINE -> {
                        String details = argumentAfter(input, "deadline");
                        int byIndex = details.indexOf(" /by");
                        if (byIndex == -1) {                                                                     // handle invalid date input for deadlines
                            throw new FF15Exception("A deadline needs a /by, e.g.: deadline return book /by Sunday");
                        }
                        String description = details.substring(0, byIndex).trim();
                        String by = details.substring(byIndex + " /by".length()).trim();
                        if (description.isEmpty()) {                                                            // handle empty description input for deadlines
                            throw new FF15Exception("The description of a deadline can't be empty, bro.");
                        }
                        if (by.isEmpty()) {                                                                     // handle empty date input for deadlines
                            throw new FF15Exception("The /by date/time of a deadline can't be empty, bro.");
                        }
                        Task task = new Deadline(description, by);
                        list.add(task);
                        printTaskAdded(task, list);
                    }
                    case EVENT -> {
                        String details = argumentAfter(input, "event");
                        int fromIndex = details.indexOf(" /from");
                        int toIndex = details.indexOf(" /to");
                        if (fromIndex == -1 || toIndex == -1 || toIndex < fromIndex) {                    // handle invalid date input for events
                            throw new FF15Exception(
                                    "An event needs /from and /to, e.g.: event project meeting /from Mon 2pm /to 4pm");
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
                        Task task = new Event(description, from, to);
                        list.add(task);
                        printTaskAdded(task, list);
                    }
                    default -> throw new FF15Exception("I'm sorry big man, I don't know what that means :-(");
                }
            } catch (FF15Exception e) {
                printMessage("AYY!!! " + e.getMessage());
            }
            printDivider(line);
            System.out.println();
            input = scanner.nextLine();
            command = Command.match(input);
        }

        printDivider(line);
        printMessage("Okok bye bye, see you again soon !");
        printDivider(line);
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

    private static void printDivider(String divider) {         // helper to print line
        System.out.println("    " + divider);
    }

    private static void printMessage(String message) {         // helper to print message
        System.out.println("     " + message);
    }

    private static void printTaskAdded(Task task, ArrayList<Task> list) {        // helper to print task message
        printMessage("Got it. I've added this task:");
        printMessage("  " + task);
        printMessage("Now you have " + list.size() + " tasks in the list.");
    }
}
