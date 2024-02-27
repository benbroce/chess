package dataAccess;

import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
    /**
     * Create a new game
     *
     * @param gameName the display name of the game
     */
    void createGame(String gameName);

    /**
     * @param gameID the ID of the game to search for
     * @return the GameData object, or null if gameID matches no games
     */
    GameData getGame(int gameID);

    /**
     * @return a list of every game on the server
     */
    ArrayList<GameData> listGames();

    /**
     * Update the game with the given gameID
     *
     * @param gameID the gameID to replace the data for
     * @param game   the new game object to insert
     * @throws DataAccessException if gameID matches no games
     */
    void updateGame(int gameID, GameData game) throws DataAccessException;

    /**
     * Delete all games
     */
    void clearGames();
}