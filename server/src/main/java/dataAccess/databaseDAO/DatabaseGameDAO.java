package dataAccess.databaseDAO;

import chess.ChessGame;
import com.google.gson.Gson;
import dataAccess.DataAccessException;
import dataAccess.DatabaseManager;
import dataAccess.GameDAO;
import model.GameData;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class DatabaseGameDAO implements GameDAO {
    public DatabaseGameDAO() throws DataAccessException {
        DatabaseManager.executeUpdate("""
                CREATE TABLE IF NOT EXISTS game (
                    `gameID` INT NOT NULL AUTO_INCREMENT,
                    `whiteUsername` VARCHAR(255) DEFAULT NULL,
                    `blackUsername` VARCHAR(255) DEFAULT NULL,
                    `gameName` VARCHAR(255) NOT NULL,
                    `game` TEXT NOT NULL,
                    PRIMARY KEY (`gameID`)
                )""");
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null) {
            throw new DataAccessException("null gameName");
        }
        return DatabaseManager.executeUpdate(
                "INSERT INTO game (gameName, game) VALUES (?, ?)",
                gameName, ((new Gson()).toJson(new ChessGame())));
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        Object[] gameRow;
        try {
            gameRow = DatabaseManager.executeQuery(
                    (new String[]{"gameID", "whiteUsername", "blackUsername", "gameName", "game"}),
                    "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID=?",
                    gameID).getFirst();
        } catch (NoSuchElementException e) {
            return null;
        }
        return new GameData(gameID,
                (String) gameRow[1],
                (String) gameRow[2],
                (String) gameRow[3],
                (new Gson()).fromJson((String) gameRow[4], ChessGame.class));
    }

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException {
        ArrayList<Object[]> queryResult = DatabaseManager.executeQuery(
                (new String[]{"gameID", "whiteUsername", "blackUsername", "gameName", "game"}),
                "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game");
        ArrayList<GameData> gameList = new ArrayList<>();
        for (Object[] gameRow : queryResult) {
            gameList.add(new GameData(
                    (int) gameRow[0],
                    (String) gameRow[1],
                    (String) gameRow[2],
                    (String) gameRow[3],
                    (new Gson()).fromJson((String) gameRow[4], ChessGame.class)));
        }
        return gameList;
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        if (getGame(gameID) == null) {
            throw new DataAccessException("cannot update non-existing game");
        }
        DatabaseManager.executeUpdate(
                "DELETE FROM game WHERE gameID=?",
                gameID);
        DatabaseManager.executeUpdate(
                "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)",
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                ((new Gson()).toJson(game.game())));
    }

    @Override
    public void clearGames() throws DataAccessException {
        DatabaseManager.executeUpdate("TRUNCATE game");
    }
}
