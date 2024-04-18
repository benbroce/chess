package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import clientAPI.GameHandler;
import clientAPI.ResponseException;
import clientAPI.ServerFacade;
import clientAPI.WebSocketFacade;
import model.GameData;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.NotificationMessage;
import webSocketMessages.serverMessages.ServerMessage;

import static chess.ChessBoard.BOARD_SIDE_LENGTH;
import static ui.ColorScheme.*;
import static ui.EscapeSequences.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Client implements GameHandler {
    private final String serverURL;
    private final ServerFacade serverFacade;
    private WebSocketFacade webSocketFacade;
    private final HashMap<Integer, Integer> gameNumberToGameID;
    private GameData currentGameData;
    private ChessGame.TeamColor currentPlayerColor;


    public Client(String serverURL) {
        this.serverURL = serverURL;
        this.serverFacade = new ServerFacade(serverURL);
        this.webSocketFacade = null;
        this.gameNumberToGameID = new HashMap<>();
        this.currentGameData = null;
        this.currentPlayerColor = null;
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                // pre-login UI
                case "register" -> this.register(params);
                case "login" -> this.login(params);
                // post-login UI
                case "logout" -> this.logout();
                case "create" -> this.createGame(params);
                case "list" -> this.listGames();
                case "join" -> this.joinGame(params);
                case "observe" -> this.observeGame(params);
                // game UI
                case "move" -> this.makeMove(params);
                case "redraw" -> this.drawGame();
                case "show" -> this.highlightLegalMoves(params);
                case "resign" -> this.resignGame();
                case "leave" -> this.leaveGame();
                // utility
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public boolean isInGame() {
        return (this.webSocketFacade != null);
    }

    public void assertInGame() throws ResponseException {
        if (!isInGame()) {
            throw new ResponseException("Unsupported - Not in a game session");
        }
    }

    public boolean isLoggedIn() {
        return this.serverFacade.isLoggedIn();
    }

    public void assertLoggedIn() throws ResponseException {
        if (!this.isLoggedIn()) {
            throw new ResponseException("Unauthorized - Log in required");
        }
    }

    public String help() {
        StringBuilder result = new StringBuilder();
        if (this.isInGame()) {
            result.append(getHelpEntry("move <PIECE POSITION> <NEW POSITION>", "a piece"));
            result.append(getHelpEntry("redraw", "chess board"));
            result.append(getHelpEntry("show <PIECE POSITION>", "legal moves"));
            result.append(getHelpEntry("resign", "from the game"));
            result.append(getHelpEntry("leave", "the game session"));
            result.append(getHelpEntry("help", "with possible commands"));
        } else if (this.isLoggedIn()) {
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
        this.gameNumberToGameID.clear();
        int gameNumber = 0;
        for (GameData game : games) {
            this.gameNumberToGameID.put(gameNumber, game.gameID());
            result.append(gameNumber).append(":\n").append(
                    getGameFancyString(game, false, false)).append("\n");
            gameNumber++;
        }
        return result.toString();
    }

    private String joinGame(String... params) throws Exception {
        assertLoggedIn();
        if (params.length != 1 && params.length != 2) {
            throw new ResponseException("Expected: <GAME NUMBER> [WHITE|BLACK|<empty>]");
        }
        int gameNumber = Integer.parseInt(params[0]);
        StringBuilder result = new StringBuilder();
        if (params.length == 2) {
            // joining as player
            int gameID = this.gameNumberToGameID.get(gameNumber);
            // join game on server via HTTP
            this.serverFacade.joinGame(params[1], gameID);
            // create a websocket session for the game
            this.webSocketFacade = new WebSocketFacade(
                    this.serverURL, this.serverFacade.getAuthToken(), this, gameID);
            // join game on websocket session
            this.currentPlayerColor = (params[1].equalsIgnoreCase("WHITE")
                    ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK);
            this.webSocketFacade.joinPlayer(this.currentPlayerColor);
            // print status
            result.append("Joined Game ").append(gameNumber).append(" as ").append(params[1]).append(".");
        } else {
            // joining as observer
            int gameID = this.gameNumberToGameID.get(gameNumber);
            // join game on server via HTTP
            this.serverFacade.joinGame(null, gameID);
            // create a websocket session for the game
            this.webSocketFacade = new WebSocketFacade(
                    this.serverURL, this.serverFacade.getAuthToken(), this, gameID);
            // join game on websocket session
            this.currentPlayerColor = null;
            this.webSocketFacade.joinObserver();
            // print status
            result.append("Observing Game ").append(gameNumber).append(".");
        }
        return result.toString();
    }

    private String observeGame(String... params) throws Exception {
        assertLoggedIn();
        if (params.length != 1) {
            throw new ResponseException("Expected: <GAME NUMBER>");
        }
        return this.joinGame(params[0]);
    }

    // Gameplay UI ////////////////////////////////////////////////////////////////////////////////

    // called by WebSocketFacade on message from server

    @Override
    public void printWebSocketMessage(ServerMessage serverMessage) {
        String message = switch (serverMessage.getServerMessageType()) {
            case NOTIFICATION -> SET_NOTIFICATION_COLOR + ((NotificationMessage) serverMessage).getMessage();
            case ERROR -> SET_ERROR_COLOR + ((ErrorMessage) serverMessage).getErrorMessage();
            default -> null;
        };
        REPL.notify(message);
    }

    @Override
    public void updateGame(GameData game) {
        if (this.isInGame()) {
            this.currentGameData = game;
            try {
                REPL.notify(this.drawGame());
            } catch (ResponseException ignored) {
            }
        }
    }

    // gameplay UI methods

    private String drawGame() throws ResponseException {
        assertInGame();
        // display the correct board orientation
        // (black on bottom if black player, white on bottom otherwise)
        boolean isBlackPlayer = (this.currentPlayerColor == ChessGame.TeamColor.BLACK);
        return ("\n\n" + this.getGameFancyString(this.currentGameData, !isBlackPlayer, isBlackPlayer));
    }

    private String highlightLegalMoves(String... params) throws ResponseException {
        this.assertInGame();
        if (params.length != 1) {
            throw new ResponseException("Expected: <PIECE POSITION>");
        }
        ChessPosition position = getChessPositionFromString(params[0]);
        // TODO: highlight the legal moves (local)
        boolean isBlackPlayer = (this.currentPlayerColor == ChessGame.TeamColor.BLACK);
        return ("\n\n" + this.getGameFancyString(this.currentGameData, !isBlackPlayer, isBlackPlayer));
    }

    private String makeMove(String... params) throws Exception {
        this.assertInGame();
        if (params.length != 2) {
            throw new ResponseException("Expected: <PIECE POSITION> <NEW POSITION>");
        }
        ChessPosition position = getChessPositionFromString(params[0]);
        ChessPosition newPosition = getChessPositionFromString(params[1]);
        this.webSocketFacade.makeMove(new ChessMove(position, newPosition, null));
        return "Moved Piece.";
    }


    private String leaveGame() throws Exception {
        this.assertInGame();
        this.webSocketFacade.leaveGame();
        this.webSocketFacade = null;
        this.currentGameData = null;
        this.currentPlayerColor = null;
        return "Left Game.";
    }

    private String resignGame() throws Exception {
        this.assertInGame();
        this.webSocketFacade.resignGame();
        return "Resigned From Game.";
    }

    // helper methods

    private ChessPosition getChessPositionFromString(String positionString) {
        positionString = positionString.toLowerCase();
        return new ChessPosition(
                positionString.charAt(0),
                Character.getNumericValue(positionString.charAt(1)));
    }

    private String getGameFancyString(GameData game, boolean includeWhiteBoard, boolean includeBlackBoard) {
        StringBuilder output = new StringBuilder();
        // game name
        output.append(SET_GAME_NAME_COLOR).append("~~ ").append(game.gameName()).append(" ~~\n");
        // player roles
        output.append(SET_WHITE_USERNAME_COLOR).append("White: ").append(game.whiteUsername());
        output.append(SET_RESULT_COLOR).append("\t");
        output.append(SET_BLACK_USERNAME_COLOR).append("Black: ").append(game.blackUsername());
        output.append(SET_RESULT_COLOR).append("\n");
        String gridLetterRow;
        // game (black at bottom) if requested
        if (includeBlackBoard) {
            gridLetterRow = String.join(GRID_LETTER_SPACE, "h", "g", "f", "e", "d", "c", "b", "a");
            output.append(EMPTY).append(gridLetterRow).append("\n");
            for (int row = 1; row <= BOARD_SIDE_LENGTH; ++row) {
                output.append(row).append(" ");
                for (int col = BOARD_SIDE_LENGTH; col >= 1; --col) {
                    output.append(getSquareFancyString(game, row, col));
                }
                output.append(SET_RESULT_COLOR).append(" ").append(row).append("\n");
            }
            output.append(EMPTY).append(gridLetterRow).append("\n");
        }
        // game (white at bottom) if requested
        if (includeWhiteBoard) {
            gridLetterRow = String.join(GRID_LETTER_SPACE, "a", "b", "c", "d", "e", "f", "g", "h");
            output.append(EMPTY).append(gridLetterRow).append("\n");
            for (int row = BOARD_SIDE_LENGTH; row >= 1; --row) {
                output.append(row).append(" ");
                for (int col = 1; col <= BOARD_SIDE_LENGTH; ++col) {
                    output.append(getSquareFancyString(game, row, col));
                }
                output.append(SET_RESULT_COLOR).append(" ").append(row).append("\n");
            }
            output.append(EMPTY).append(gridLetterRow).append("\n");
        }
        // return the result
        return output.toString();
    }

    private String getSquareFancyString(GameData game, int row, int col) {
        ChessPiece square = game.game().getBoard().getPiece(new ChessPosition(row, col));
        StringBuilder output = new StringBuilder();
        // grid background: dark if row and column match parity, light otherwise
        if (((row % 2) == 0) == ((col % 2) == 0)) {
            output.append(SET_GRID_DARK_COLOR);
        } else {
            output.append(SET_GRID_LIGHT_COLOR);
        }
        // determine character for piece
        if (square == null) {
            output.append(EMPTY);
        } else {
            // set color for piece
            output.append((square.getTeamColor() == ChessGame.TeamColor.WHITE)
                    ? SET_WHITE_PIECE_COLOR
                    : SET_BLACK_PIECE_COLOR);
            // get the matching character
            output.append(switch (square.getPieceType()) {
                case KING -> BLACK_KING;
                case QUEEN -> BLACK_QUEEN;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case ROOK -> BLACK_ROOK;
                case PAWN -> BLACK_PAWN;
            });
        }
        return output.toString();
    }
}
