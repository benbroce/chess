package dataAccessTests;

import dataAccess.DataAccessException;
import dataAccess.DatabaseManager;
import dataAccess.GameDAO;
import dataAccess.memoryDAO.MemoryGameDAO;
import dataAccess.databaseDAO.DatabaseGameDAO;
import model.GameData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GameDAOTests {
    /**
     * @return a Stream of GameDAO objects of different implementations for testing
     */
    private static Stream<GameDAO> gameDAOImplementationsUnderTest() throws DataAccessException {
        return Stream.of((new MemoryGameDAO()), (new DatabaseGameDAO()));
    }

    private static void setup(GameDAO gameDAO) throws DataAccessException {
        DatabaseManager.createDatabase();
        gameDAO.clearGames();
    }

    private static void tearDown(GameDAO gameDAO) throws DataAccessException {
        gameDAO.clearGames();
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void createGamePositive(GameDAO gameDAO) throws DataAccessException {
        // set pre-state
        setup(gameDAO);
        // confirm pre-state
        assertTrue(gameDAO.listGames().isEmpty());
        // perform action
        int gameID = gameDAO.createGame("testGame");
        // compare post-state
        assertEquals(gameDAO.getGame(gameID).gameName(), "testGame");
        tearDown(gameDAO);
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void createGameNegative(GameDAO gameDAO) throws DataAccessException {
        // set pre-state
        setup(gameDAO);
        // confirm pre-state
        assertTrue(gameDAO.listGames().isEmpty());
        // perform action, compare post-state
        assertThrows(DataAccessException.class, (() -> gameDAO.createGame(null)));
        tearDown(gameDAO);
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void getGamePositive(GameDAO gameDAO) throws DataAccessException {
        // set pre-state
        setup(gameDAO);
        int gameID = gameDAO.createGame("testGame");
        // confirm pre-state
        assertEquals(gameDAO.listGames().size(), 1);
        // perform action, compare post-state
        assertEquals(gameDAO.getGame(gameID).gameName(), "testGame");
        tearDown(gameDAO);
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void getGameNegative(GameDAO gameDAO) throws DataAccessException {
        // set pre-state
        setup(gameDAO);
        int gameID = gameDAO.createGame("testGame");
        // confirm pre-state
        assertEquals(gameDAO.listGames().size(), 1);
        // perform action, compare post-state
        assertNull(gameDAO.getGame(gameID + 1));
        tearDown(gameDAO);
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void listGamesPositive(GameDAO gameDAO) throws DataAccessException {
        // set pre-state
        setup(gameDAO);
        int gameID0 = gameDAO.createGame("testGame0");
        int gameID1 = gameDAO.createGame("testGame1");
        int gameID2 = gameDAO.createGame("testGame2");
        // confirm pre-state
        assertEquals(gameDAO.getGame(gameID0).gameName(), "testGame0");
        assertEquals(gameDAO.getGame(gameID1).gameName(), "testGame1");
        assertEquals(gameDAO.getGame(gameID2).gameName(), "testGame2");
        // perform action, compare post-state
        assertEquals(gameDAO.listGames().size(), 3);
        assertEquals(gameDAO.listGames().getFirst().gameName().substring(0, 4), "test");
        tearDown(gameDAO);
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void listGamesNegative(GameDAO gameDAO) throws DataAccessException {
        // set pre-state
        setup(gameDAO);
        // perform action, compare post-state
        assertNotNull(gameDAO.listGames());
        assertEquals(gameDAO.listGames().size(), 0);
        tearDown(gameDAO);
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void updateGamePositive(GameDAO gameDAO) throws DataAccessException {
        // set pre-state
        setup(gameDAO);
        int gameIDOld = gameDAO.createGame("oldGame");
        int gameIDNew = gameDAO.createGame("newGame");
        GameData newGame = gameDAO.getGame(gameIDNew);
        newGame = new GameData(
                gameIDOld, newGame.whiteUsername(), newGame.blackUsername(), newGame.gameName(), newGame.game());
        // confirm pre-state
        assertEquals(gameDAO.listGames().size(), 2);
        // perform action
        gameDAO.updateGame(gameIDOld, newGame);
        // compare post-state
        assertEquals(gameDAO.getGame(gameIDOld).gameName(), "newGame");
        tearDown(gameDAO);
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void updateGameNegative(GameDAO gameDAO) throws DataAccessException {
        // set pre-state
        setup(gameDAO);
        int gameIDOld = gameDAO.createGame("oldGame");
        // perform action
        assertThrows(DataAccessException.class, (() -> gameDAO.updateGame((gameIDOld + 1), gameDAO.getGame(gameIDOld))));
        // compare post-state
        tearDown(gameDAO);
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void clearGamesPositive(GameDAO gameDAO) throws DataAccessException {
        // set pre-state
        setup(gameDAO);
        gameDAO.createGame("testGame");
        // confirm pre-state
        assertEquals(gameDAO.listGames().size(), 1);
        // perform action
        gameDAO.clearGames();
        // compare post-state
        assertEquals(gameDAO.listGames().size(), 0);
        tearDown(gameDAO);
    }
}