package clientAPI;

import model.GameData;
import webSocketMessages.serverMessages.ServerMessage;

public interface GameHandler {
    void updateGame(GameData game);

    void printWebSocketMessage(ServerMessage message);
}
