package webSocketMessages.userCommands;

import java.util.Objects;

public class JoinObserverCommand extends UserGameCommand {
    Integer gameID;

    public JoinObserverCommand(String authToken, Integer gameID) {
        super(authToken);
        this.commandType = CommandType.JOIN_OBSERVER;
        this.gameID = gameID;
    }

    public Integer getGameID() {
        return this.gameID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JoinObserverCommand that = (JoinObserverCommand) o;
        return Objects.equals(gameID, that.gameID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gameID);
    }
}