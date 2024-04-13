package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import webSocketMessages.userCommands.UserGameCommand;

@WebSocket
public class WebSocketHandler {
    SessionManager sessionManager;

    public WebSocketHandler() {
        sessionManager = new SessionManager();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {

    }

    @OnWebSocketClose
    public void onClose(Session session) {
        sessionManager.removeSession(session);
    }

    @OnWebSocketError
    public void onError(Throwable throwable) {

    }

    private void sendMessage(int gameID, String message, String authToken) {

    }

    private void broadcastMessage(int gameID, String message, String excludeAuthToken) {

    }

    // Incoming Commands //////////////////////////////////////////////////////////////////////////
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case JOIN_PLAYER -> this.joinPlayer(message);
            case JOIN_OBSERVER -> this.joinObserver(message);
            case MAKE_MOVE -> this.makeMove(message);
            case LEAVE -> this.leaveGame(message);
            case RESIGN -> this.resignGame(message);
        }
    }

    private void joinPlayer(String message) {

    }

    private void joinObserver(String message) {

    }

    private void makeMove(String message) {


    }

    private void leaveGame(String message) {

    }

    private void resignGame(String message) {

    }

    // Outgoing Messages //////////////////////////////////////////////////////////////////////////
    public void loadGame() {

    }

    public void sendError() {

    }

    public void sendNotification() {

    }
}
