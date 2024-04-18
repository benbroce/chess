import ui.REPL;

public class Main {
    public static void main(String[] args) {
        String serverURL = "localhost:8080";
        if (args.length == 1) {
            serverURL = args[0];
        }
        new REPL(serverURL).run();
    }
}