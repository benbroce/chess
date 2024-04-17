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

    @OnWebSocketClose
    public void onClose(Session session) {
        sessionManager.removeSession(session);
    }

    private void sendMessage(int gameID, String authToken, String message) throws IOException {
        sessionManager.getSessionsForGame(gameID).get(authToken).getRemote().sendString(message);
    }

    private void sendMessage(Session session, String message) throws IOException {
        session.getRemote().sendString(message);
    }

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

    // Incoming Commands //////////////////////////////////////////////////////////////////////////
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
                this.packNotificationMessage("join "
                        + this.authDAO.getUsername(command.getAuthString())
                        + ((command.getPlayerColor() == ChessGame.TeamColor.WHITE) ? " white" : " black")),
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
                this.packNotificationMessage("join "
                        + this.authDAO.getUsername(command.getAuthString())
                        + " observer"),
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
                this.packNotificationMessage("move " + command.getMove().toString()),
                command.getAuthString());
        // on check, checkmate, or stalemate, notify all clients
        ChessGame gameState = gameData.game();
        if (gameState.isInCheckmate(gameState.getTeamTurn())) {
            this.broadcastMessage(command.getGameID(),
                    this.packNotificationMessage("checkmate " + getCurrentTurnUsername(gameData)),
                    null);
        } else if (gameState.isInStalemate(gameState.getTeamTurn())) {
            this.broadcastMessage(command.getGameID(),
                    this.packNotificationMessage("stalemate " + getCurrentTurnUsername(gameData)),
                    null);
        } else if (gameState.isInCheck(gameState.getTeamTurn())) {
            this.broadcastMessage(command.getGameID(),
                    this.packNotificationMessage("check " + getCurrentTurnUsername(gameData)),
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
                this.packNotificationMessage("leave " + this.authDAO.getUsername(command.getAuthString())),
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
                this.packNotificationMessage("resign " + this.authDAO.getUsername(command.getAuthString())),
                command.getAuthString());
    }

    // Outgoing Messages //////////////////////////////////////////////////////////////////////////

    private void loadGameForRootClient(int gameID, String authToken)
            throws UnauthorizedException, BadRequestException, DataAccessException, IOException {
        this.sendMessage(gameID, authToken, (new Gson()).toJson(this.gameService.getGame(authToken, gameID)));
    }

    private String packErrorMessage(String errorMessage) {
        return (new Gson()).toJson(new ErrorMessage("Error: " + errorMessage));
    }

    private String packNotificationMessage(String message) {
        return (new Gson()).toJson(new NotificationMessage(message));
    }
}
