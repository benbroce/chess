package dataAccess;

import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
    /**
     * Create a new game
     *
     * @param gameName the display name of the game
     * @return the gameID of the new game
     * @throws DataAccessException if gameName is null
     */
    int createGame(String gameName) throws DataAccessException;

    /**
     * @param gameID the ID of the game to search for
     * @return the GameData object, or null if gameID matches no games
     */
    GameData getGame(int gameID) throws DataAccessException;

    /**
     * @return a list of every game on the server
     */
    ArrayList<GameData> listGames() throws DataAccessException;

    /**
     * Update the game with the given gameID
     * NOTE: If gameID and game.gameID do not match,
     * the game at gameID is removed and the new game is added at game.gameID,
     * allowing updating of the gameID
     *
     * @param gameID the gameID to replace the data for
     * @param game   the new game object to insert
     * @throws DataAccessException if gameID matches no games
     */
    void updateGame(int gameID, GameData game) throws DataAccessException;

    /**
     * Delete all games
     */
    void clearGames() throws DataAccessException;
}