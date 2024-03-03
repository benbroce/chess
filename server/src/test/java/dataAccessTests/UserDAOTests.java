package dataAccessTests;

import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import dataAccess.memoryDAO.MemoryUserDAO;
import dataAccess.databaseDAO.DatabaseUserDAO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTests {
    /**
     * @return a Stream of UserDAO objects of different implementations for testing
     */
    private static Stream<UserDAO> userDAOImplementationsUnderTest() {
        return Stream.of((new MemoryUserDAO())/*, (new DatabaseUserDAO())*/);
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void createUserPositive(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void createUserNegative(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void getUserPositive(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void getUserNegative(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void verifyUserPositive(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void verifyUserNegative(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void clearUsersPositive(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }
}