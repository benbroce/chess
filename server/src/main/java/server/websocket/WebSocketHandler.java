package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import service.serviceExceptions.BadRequestException;
import service.serviceExceptions.ServerErrorException;
import service.serviceExceptions.UnauthorizedException;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadGameMessage;
import webSocketMessages.serverMessages.NotificationMessage;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.util.Map;

@WebSocket
public class WebSocketHandler {
    SessionManager sessionManager;
    GameService gameService;
    AuthDAO authDAO;

    public WebSocketHandler(GameService gameService, AuthDAO authDAO) {
        sessionManager = new SessionManager();
        this.gameService = gameService;
        this.authDAO = authDAO;
    }

    /**
     * When a websocket session closes, remove it from the SessionManager
     *
     * @param session the session to remove
     */
    @OnWebSocketClose
    public void onClose(Session session) {
        sessionManager.removeSession(session);
    }

    /**
     * Send a message to the client by client info
     *
     * @param gameID    the ID of the game context to send to
     * @param authToken the authToken of the client
     * @param message   the message to send (pack as a type)
     * @throws IOException on send error
     */
    private void sendMessage(int gameID, String authToken, String message) throws IOException {
        sessionManager.getSessionsForGame(gameID).get(authToken).getRemote().sendString(message);
    }

    /**
     * Send a message to the client by session (only use for errors)
     *
     * @param session the session to send a message to
     * @param message the message to send (pack as a type)
     * @throws IOException on send error
     */
    private void sendMessage(Session session, String message) throws IOException {
        session.getRemote().sendString(message);
    }

    /**
     * Send a message to multiple clients (can exclude one by authToken)
     */
    private void broadcastMessage(int gameID, String message, String excludeAuthToken) throws IOException {
        // run through all <authToken, session> pairs associated with the given game
        for (Map.Entry<String, Session> sessionEntry : sessionManager.getSessionsForGame(gameID).entrySet()) {
            // if the session is open and the authToken isn't excluded
            if (sessionEntry.getValue().isOpen() && !sessionEntry.getKey().equals(excludeAuthToken)) {
                // send the message to the session
                this.sendMessage(gameID, sessionEntry.getKey(), message);
            }
        }
    }

    // Incoming Commands //////////////////////////////////////////////////////////////////////////\

    /**
     * When a websocket message is received from a client, direct it to the correct method
     *
     * @param session the session the message was received on
     * @param message the message received (unpack from a type)
     * @throws IOException on failure to send an error message
     */
    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = (new Gson()).fromJson(message, UserGameCommand.class);
        try {
            switch (command.getCommandType()) {
                case JOIN_PLAYER -> this.joinPlayer(message, session);
                case JOIN_OBSERVER -> this.joinObserver(message, session);
                case MAKE_MOVE -> this.makeMove(message);
                case LEAVE -> this.leaveGame(message);
                case RESIGN -> this.resignGame(message);
            }
        } catch (Throwable throwable) {
            this.sendMessage(session, this.packErrorMessage(throwable.getMessage()));
        }
    }

    private void joinPlayer(String message, Session session)
            throws UnauthorizedException, BadRequestException, DataAccessException, IOException {
        // decode JSON command
        JoinPlayerCommand command = (new Gson()).fromJson(message, JoinPlayerCommand.class);
        // add the player session to this game in the SessionManager
        this.sessionManager.addSessionToGame(command.getGameID(), command.getAuthString(), session);
        // run the gameService method on the command
        gameService.joinPlayer(command);
        // send the game state to the root client
        this.loadGameForRootClient(command.getGameID(), command.getAuthString());
        // broadcast a join notification to all clients except root
        this.broadcastMessage(command.getGameID(),
                this.packNotificationMessage(this.authDAO.getUsername(command.getAuthString())
                        + " joined as "
                        + ((command.getPlayerColor() == ChessGame.TeamColor.WHITE) ? "white" : "black")),
                command.getAuthString());
    }

    private void joinObserver(String message, Session session)
            throws UnauthorizedException, BadRequestException, DataAccessException, IOException {
        // decode JSON command
        JoinObserverCommand command = (new Gson()).fromJson(message, JoinObserverCommand.class);
        // add the observer session to this game in the SessionManager
        this.sessionManager.addSessionToGame(command.getGameID(), command.getAuthString(), session);
        // run the gameService method on the command
        gameService.joinObserver(command);
        // send the game state to the root client
        this.loadGameForRootClient(command.getGameID(), command.getAuthString());
        // broadcast a join notification to all clients except root
        this.broadcastMessage(command.getGameID(),
                this.packNotificationMessage(this.authDAO.getUsername(command.getAuthString())
                        + " joined as observer"),
                command.getAuthString());
    }

    private void makeMove(String message)
            throws UnauthorizedException, BadRequestException, ServerErrorException, IOException, DataAccessException {
        // decode JSON command
        MakeMoveCommand command = (new Gson()).fromJson(message, MakeMoveCommand.class);
        // verify move validity, make move in gameService
        this.gameService.makeMove(command);
        // send the new game state to all clients
        GameData gameData = this.gameService.getGame(command.getAuthString(), command.getGameID());
        this.broadcastMessage(command.getGameID(),
                (new Gson().toJson(new LoadGameMessage(gameData))),
                null);
        // broadcast a move notification to all clients except root
        this.broadcastMessage(command.getGameID(),
                this.packNotificationMessage(this.authDAO.getUsername(command.getAuthString())
                        + " moved "
                        + command.getMove().toString()),
                command.getAuthString());
        // on check, checkmate, or stalemate, notify all clients
        ChessGame gameState = gameData.game();
        if (gameState.isInCheckmate(gameState.getTeamTurn())) {
            this.broadcastMessage(command.getGameID(),
                    this.packNotificationMessage(getCurrentTurnUsername(gameData) + " was checkmated"),
                    null);
        } else if (gameState.isInStalemate(gameState.getTeamTurn())) {
            this.broadcastMessage(command.getGameID(),
                    this.packNotificationMessage("Stalemate"),
                    null);
        } else if (gameState.isInCheck(gameState.getTeamTurn())) {
            this.broadcastMessage(command.getGameID(),
                    this.packNotificationMessage(getCurrentTurnUsername(gameData) + " is in check"),
                    null);
        }
    }

    private String getCurrentTurnUsername(GameData gameState) {
        return (gameState.game().getTeamTurn() == ChessGame.TeamColor.WHITE)
                ? gameState.whiteUsername() : gameState.blackUsername();
    }

    private void leaveGame(String message)
            throws UnauthorizedException, BadRequestException, ServerErrorException, IOException, DataAccessException {
        // decode JSON command
        LeaveCommand command = (new Gson().fromJson(message, LeaveCommand.class));
        // leave game in gameService
        this.gameService.leaveGame(command);
        // broadcast a leave notification to all clients except root
        this.broadcastMessage(command.getGameID(),
                this.packNotificationMessage(this.authDAO.getUsername(command.getAuthString()) + " left the game"),
                command.getAuthString());
        // remove the user session from this game in the SessionManager
        this.sessionManager.removeSessionFromGame(command.getGameID(), command.getAuthString());
    }

    private void resignGame(String message)
            throws UnauthorizedException, BadRequestException, ServerErrorException, DataAccessException, IOException {
        // decode JSON command
        ResignCommand command = (new Gson()).fromJson(message, ResignCommand.class);
        // resign from game in gameService
        this.gameService.resignGame(command);
        // broadcast a resignation notification to all clients
        this.broadcastMessage(command.getGameID(),
                this.packNotificationMessage(this.authDAO.getUsername(command.getAuthString()) + " resigned"),
                command.getAuthString());
    }

    // Outgoing Messages //////////////////////////////////////////////////////////////////////////

    private void loadGameForRootClient(int gameID, String authToken)
            throws UnauthorizedException, BadRequestException, DataAccessException, IOException {
        this.sendMessage(gameID, authToken,
                (new Gson()).toJson(new LoadGameMessage(this.gameService.getGame(authToken, gameID))));
    }

    private String packErrorMessage(String errorMessage) {
        return (new Gson()).toJson(new ErrorMessage("Error: " + errorMessage));
    }

    private String packNotificationMessage(String message) {
        return (new Gson()).toJson(new NotificationMessage(message));
    }
}
