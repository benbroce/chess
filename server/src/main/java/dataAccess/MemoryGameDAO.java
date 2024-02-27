package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> gameTable;
    private int nextGameID;

    public MemoryGameDAO() {
        this.gameTable = new HashMap<>();
        this.nextGameID = 0;
    }

    private int generateGameID() {
        int id = this.nextGameID;
        this.nextGameID++;
        return id;
    }

    @Override
    public void createGame(String gameName) {
        int gameID = generateGameID();
        this.gameTable.put(gameID, (new GameData(
                gameID, null, null, gameName, (new ChessGame()))));
    }

    @Override
    public GameData getGame(int gameID) {
        return this.gameTable.get(gameID);
    }

    @Override
    public ArrayList<GameData> listGames() {
        return new ArrayList<>(this.gameTable.values());
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        if (getGame(gameID) == null) {
            throw new DataAccessException("cannot update non-existing game");
        }
        this.gameTable.put(gameID, game);
    }

    @Override
    public void clearGames() {
        this.gameTable.clear();
    }
}
