package ui;

import clientAPI.ResponseException;
import clientAPI.ServerFacade;
import com.google.gson.Gson;
import model.GameData;

import static ui.ColorScheme.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Client {
    private final ServerFacade serverFacade;
    private final HashMap<Integer, Integer> gameNumberToGameID;

    public Client(String serverURL) {
        this.serverFacade = new ServerFacade(serverURL);
        this.gameNumberToGameID = new HashMap<>();
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> this.register(params);
                case "login" -> this.login(params);
                case "logout" -> this.logout();
                case "create" -> this.createGame(params);
                case "list" -> this.listGames();
                case "join" -> this.joinGame(params);
                case "observe" -> this.observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    public boolean isLoggedIn() {
        return this.serverFacade.isLoggedIn();
    }

    public void assertLoggedIn() throws ResponseException {
        if (!isLoggedIn()) {
            throw new ResponseException("Unauthorized - Log in required");
        }
    }

    public String help() {
        StringBuilder result = new StringBuilder();
        if (this.serverFacade.isLoggedIn()) {
            result.append(getHelpEntry("create <NAME>", "a game"));
            result.append(getHelpEntry("list", "games"));
            result.append(getHelpEntry("join <GAME NUMBER> [WHITE|BLACK|<empty>]", "a game"));
            result.append(getHelpEntry("observe <GAME NUMBER>", "a game"));
            result.append(getHelpEntry("logout", "when you are done"));
            result.append(getHelpEntry("quit", "playing chess"));
            result.append(getHelpEntry("help", "with possible commands"));
        } else {
            result.append(getHelpEntry("register <USERNAME> <PASSWORD> <EMAIL>", "a new account"));
            result.append(getHelpEntry("login <USERNAME> <PASSWORD>", "to play chess"));
            result.append(getHelpEntry("quit", "playing chess"));
            result.append(getHelpEntry("help", "with possible commands"));
        }
        return result.toString();
    }

    private String getHelpEntry(String action, String description) {
        return ("\t" + SET_HELP_OPTION_COLOR + action + SET_HELP_DESCRIPTOR_COLOR + " - " + description + "\n");
    }

    // Pre-login UI ///////////////////////////////////////////////////////////////////////////////

    private String register(String... params) throws ResponseException {
        if (params.length != 3) {
            throw new ResponseException("Expected: <USERNAME> <PASSWORD> <EMAIL>");
        }
        this.serverFacade.register(params[0], params[1], params[2]);
        return "Registration Successful. Logged In.";
    }

    private String login(String... params) throws ResponseException {
        if (params.length != 2) {
            throw new ResponseException("Expected: <USERNAME> <PASSWORD>");
        }
        this.serverFacade.login(params[0], params[1]);
        return "Logged In.";
    }

    // Post-login UI //////////////////////////////////////////////////////////////////////////////

    private String logout() throws ResponseException {
        assertLoggedIn();
        this.serverFacade.logout();
        return "Logged Out.";
    }

    private String createGame(String... params) throws ResponseException {
        assertLoggedIn();
        if (params.length != 1) {
            throw new ResponseException("Expected: <NAME>");
        }
        return ("Created Game ID " + this.serverFacade.createGame(params[0]) + ".");
    }

    // display a numbered list of all games in Json format
    // clear and rebuild the gameIDMap to match what is displayed
    private String listGames() throws ResponseException {
        assertLoggedIn();
        ArrayList<GameData> games = this.serverFacade.listGames();
        StringBuilder result = new StringBuilder();
        Gson gson = new Gson();
        this.gameNumberToGameID.clear();
        int gameNumber = 0;
        for (GameData game : games) {
            this.gameNumberToGameID.put(gameNumber, game.gameID());
            result.append(gameNumber).append(": ").append(gson.toJson(game)).append("\n");
            gameNumber++;
        }
        return result.toString();
    }

    private String joinGame(String... params) throws ResponseException {
        assertLoggedIn();
        if (params.length != 1 && params.length != 2) {
            throw new ResponseException("Expected: <GAME NUMBER> [WHITE|BLACK|<empty>]");
        }
        int gameNumber = Integer.parseInt(params[0]);
        StringBuilder result = new StringBuilder();
        if (params.length == 2) {
            this.serverFacade.joinGame(params[1], this.gameNumberToGameID.get(gameNumber));
            result.append("Joined Game ").append(gameNumber).append(" as ").append(params[1]).append(".");
        } else {
            this.serverFacade.joinGame(null, this.gameNumberToGameID.get(gameNumber));
            result.append("Observing Game ").append(gameNumber).append(".");
        }
        result.append("\n\n").append(getGameFancyString(gameNumber));
        return result.toString();
    }

    private String observeGame(String... params) throws ResponseException {
        assertLoggedIn();
        if (params.length != 1) {
            throw new ResponseException("Expected: <GAME NUMBER>");
        }
        return this.joinGame(params[0]);
    }

    // Gameplay UI ////////////////////////////////////////////////////////////////////////////////

    private GameData getGame(int gameNumber) throws ResponseException {
        for (GameData game : this.serverFacade.listGames()) {
            if (game.gameID() == this.gameNumberToGameID.get(gameNumber)) {
                return game;
            }
        }
        throw new ResponseException("No such game.");
    }

    private String getGameFancyString(int gameNumber) throws ResponseException {
        GameData game = getGame(gameNumber);
        StringBuilder output = new StringBuilder();
        output.append(SET_GAME_NAME_COLOR).append("~~ ").append(game.gameName()).append(" ~~\n");
        output.append(SET_WHITE_USERNAME_COLOR).append("White: ").append(game.whiteUsername());
        output.append(SET_RESULT_COLOR).append("\t");
        output.append(SET_BLACK_USERNAME_COLOR).append("Black: ").append(game.blackUsername());
        output.append(SET_RESULT_COLOR).append("\n");
        output.append(game.game().getBoard().toString());
        //output.append(game.game().getBoard().getPiece())
        return output.toString();
    }
}
