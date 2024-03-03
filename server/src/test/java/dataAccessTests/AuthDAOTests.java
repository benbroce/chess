package dataAccessTests;

import dataAccess.AuthDAO;
import dataAccess.databaseDAO.DatabaseAuthDAO;
import dataAccess.memoryDAO.MemoryAuthDAO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class AuthDAOTests {
    /**
     * @return a Stream of AuthDAO objects of different implementations for testing
     */
    private static Stream<AuthDAO> authDAOImplementationsUnderTest() {
        return Stream.of((new MemoryAuthDAO())/*, (new DatabaseAuthDAO())*/);
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void createAuthPositive(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void createAuthNegative(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void deleteAuthPositive(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void deleteAuthNegative(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void verifyAuthTokenPositive(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void verifyAuthTokenNegative(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void getUsernamePositive(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void getUsernameNegative(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void ClearAuthsPositive(AuthDAO authDAO) {
        // set pre-state
        // confirm pre-state
        // perform action
        // compare post-state
    }
}