import java.util.Scanner;

public class FF15 {
    public static void main(String[] args) {
        String banner = " _____ _____ _  ____  \n"
                + "|  ___|  ___/ |/ ___| \n"
                + "| |_  | |_  | |\\___ \\ \n"
                + "|  _| |  _| | | ___) |\n"
                + "|_|   |_|   |_||____/ \n";
        String line = "____________________________________________________________";

        System.out.println(line);
        System.out.println(banner);
        System.out.println("Eh hello ah, I'm FF15 lah!");
        System.out.println("What can I do for you sia?");
        System.out.println(line);

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        while (!input.equals("bye")) {
            System.out.println(input);
            System.out.println(line);
            input = scanner.nextLine();
        }

        System.out.println("Okok bye bye, see you again soon ah!");
        System.out.println(line);
    }
}
