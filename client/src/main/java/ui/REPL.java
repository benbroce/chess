package ui;

import java.util.Scanner;

import static ui.ColorScheme.*;

public class REPL {
    private final Client client;

    public REPL(String serverURL) {
        this.client = new Client(serverURL);
    }

    public void run() {
        System.out.print(SET_PROMPT_COLOR);
        System.out.println("\uD83D\uDC51 Welcome to 240 chess. Type Help to get started. \uD83D\uDC51");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                System.out.print(SET_RESULT_COLOR + result);
            } catch (Throwable e) {
                System.out.print(e.toString());
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + SET_PROMPT_COLOR
                + "[LOGGED_" + (client.isLoggedIn() ? "IN" : "OUT") + "] >>> "
                + SET_USER_INPUT_COLOR);
    }

    // TODO: Maybe modify to print at a logical place in the REPL flow (with a flag or shared var)
    public static void notify(String message) {
        System.out.println(SET_RESULT_COLOR + message);
    }
}
