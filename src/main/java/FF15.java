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
        ArrayList<Task> list = new ArrayList<>(); // Container To-Do List

        while (!input.equals("bye")) {
            if (input.equals("list")) {
                printMessage("Here are the tasks in your list:");
                for (int i = 0; i < list.size(); i++) {
                    printMessage((i + 1) + "." + list.get(i));
                }
            } else if (input.startsWith("mark ")) {
                Task task = list.get(Integer.parseInt(input.substring(5).trim()) - 1);
                task.markAsDone();
                printMessage("Nicesu! I've marked this task as done:");
                printMessage("  " + task);
            } else if (input.startsWith("unmark ")) {
                Task task = list.get(Integer.parseInt(input.substring(7).trim()) - 1);
                task.markAsNotDone();
                printMessage("OK, I've marked this task as not done yet:");
                printMessage("  " + task);
            } else {
                list.add(new Task(input));
                printMessage("added: " + input);
            }
            printDivider(line);
            System.out.println();
            input = scanner.nextLine();
        }

        printMessage("Okok bye bye, see you again soon !");
        printDivider(line);
    }

    private static void printDivider(String divider) {
        System.out.println("    " + divider);
    }

    private static void printMessage(String message) {
        System.out.println("     " + message);
    }
}
