package clientAPI;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadGameMessage;
import webSocketMessages.serverMessages.NotificationMessage;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;

import javax.websocket.*;
import java.net.URI;

public class WebSocketFacade extends Endpoint implements MessageHandler.Whole<String> {
    public Session session;
    private final GameHandler gameHandler;
    private final String authToken;
    private final int gameID;

    public WebSocketFacade(String webSocketURL, String authToken, GameHandler gameHandler, int gameID) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, (new URI(webSocketURL + "/connect")));
        this.session.addMessageHandler(this);
        this.authToken = authToken;
        this.gameHandler = gameHandler;
        this.gameID = gameID;
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    // Incoming Messages //////////////////////////////////////////////////////////////////////////

    @Override
    public void onMessage(String message) {
        ServerMessage serverMessage = (new Gson()).fromJson(message, ServerMessage.class);
        switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME ->
                    this.gameHandler.updateGame((new Gson()).fromJson(message, LoadGameMessage.class).getGame());
            case NOTIFICATION ->
                    this.gameHandler.printWebSocketMessage((new Gson()).fromJson(message, NotificationMessage.class));
            case ERROR -> this.gameHandler.printWebSocketMessage((new Gson().fromJson(message, ErrorMessage.class)));
        }
    }

    // Outgoing Commands //////////////////////////////////////////////////////////////////////////

    private void sendMessage(String message) throws Exception {
        this.session.getBasicRemote().sendText(message);
    }

    public void joinPlayer(ChessGame.TeamColor playerColor) throws Exception {
        JoinPlayerCommand command = new JoinPlayerCommand(this.authToken, this.gameID, playerColor);
        this.sendMessage((new Gson()).toJson(command));
    }

    public void joinObserver() throws Exception {
        JoinObserverCommand command = new JoinObserverCommand(this.authToken, this.gameID);
        this.sendMessage((new Gson()).toJson(command));
    }

    public void makeMove(ChessMove move) throws Exception {
        MakeMoveCommand command = new MakeMoveCommand(this.authToken, this.gameID, move);
        this.sendMessage((new Gson().toJson(command)));
    }

    public void leaveGame() throws Exception {
        LeaveCommand command = new LeaveCommand(this.authToken, this.gameID);
        this.sendMessage((new Gson()).toJson(command));
    }

    public void resignGame() throws Exception {
        ResignCommand command = new ResignCommand(this.authToken, this.gameID);
        this.sendMessage((new Gson()).toJson(command));
    }
}
