package dataAccessTests;

import dataAccess.GameDAO;
import dataAccess.memoryDAO.MemoryGameDAO;
import dataAccess.databaseDAO.DatabaseGameDAO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GameDAOTests {
    /**
     * @return a Stream of GameDAO objects of different implementations for testing
     */
    private static Stream<GameDAO> gameDAOImplementationsUnderTest() {
        return Stream.of((new MemoryGameDAO())/*, (new DatabaseGameDAO())*/);
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void createGamePositive(GameDAO gameDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void createGameNegative(GameDAO gameDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void getGamePositive(GameDAO gameDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void getGameNegative(GameDAO gameDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void listGamesPositive(GameDAO gameDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void listGamesNegative(GameDAO gameDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void updateGamePositive(GameDAO gameDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void updateGameNegative(GameDAO gameDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("gameDAOImplementationsUnderTest")
    public void clearGamesPositive(GameDAO gameDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }
}