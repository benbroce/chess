package webSocketMessages.userCommands;

import chess.ChessMove;

import java.util.Objects;

public class MakeMoveCommand extends UserGameCommand {
    Integer gameID;
    ChessMove move;

    public MakeMoveCommand(String authToken, Integer gameID, ChessMove move) {
        super(authToken);
        this.commandType = CommandType.MAKE_MOVE;
        this.gameID = gameID;
        this.move = move;
    }

    public Integer getGameID() {
        return this.gameID;
    }

    public ChessMove getMove() {
        return this.move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MakeMoveCommand that = (MakeMoveCommand) o;
        return Objects.equals(gameID, that.gameID) && Objects.equals(move, that.move);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gameID, move);
    }
}