package dataAccessTests;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.DatabaseManager;
import dataAccess.databaseDAO.DatabaseAuthDAO;
import dataAccess.memoryDAO.MemoryAuthDAO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AuthDAOTests {
    /**
     * @return a Stream of AuthDAO objects of different implementations for testing
     */
    private static Stream<AuthDAO> authDAOImplementationsUnderTest() throws DataAccessException {
        return Stream.of((new MemoryAuthDAO()), (new DatabaseAuthDAO()));
    }

    private static void setup(AuthDAO authDAO) throws DataAccessException {
        DatabaseManager.createDatabase();
        authDAO.clearAuths();
    }

    private static void tearDown(AuthDAO authDAO) throws DataAccessException {
        authDAO.clearAuths();
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void createAuthPositive(AuthDAO authDAO) throws DataAccessException {
        // set pre-state
        setup(authDAO);
        // perform action
        String authToken = "";
        try {
            authToken = authDAO.createAuth("testUser");
        } catch (DataAccessException e) {
            fail("invalid username");
        }
        // compare post-state
        assertEquals(authDAO.getUsername(authToken), "testUser");
        tearDown(authDAO);
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void createAuthNegative(AuthDAO authDAO) throws DataAccessException {
        // set pre-state
        setup(authDAO);
        // perform action, compare post-state
        assertThrows(DataAccessException.class, (() -> authDAO.createAuth("")));
        tearDown(authDAO);
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void deleteAuthPositive(AuthDAO authDAO) throws DataAccessException {
        // set pre-state
        setup(authDAO);
        String authToken = "";
        try {
            authToken = authDAO.createAuth("testUser");
        } catch (DataAccessException e) {
            fail("invalid username");
        }
        // confirm pre-state
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform action
        try {
            authDAO.deleteAuth(authToken);
        } catch (DataAccessException e) {
            fail("null authToken");
        }
        // compare post-state
        assertNull(authDAO.getUsername(authToken));
        tearDown(authDAO);
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void deleteAuthNegative(AuthDAO authDAO) throws DataAccessException {
        // set pre-state
        setup(authDAO);
        // perform action
        assertThrows(DataAccessException.class, (() -> authDAO.deleteAuth(null)));
        // compare post-state
        tearDown(authDAO);
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void verifyAuthTokenPositive(AuthDAO authDAO) throws DataAccessException {
        // set pre-state
        setup(authDAO);
        String authToken = authDAO.createAuth("testUser");
        // confirm pre-state
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform action, compare post-state
        assertTrue(authDAO.verifyAuthToken(authToken));
        tearDown(authDAO);
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void verifyAuthTokenNegative(AuthDAO authDAO) throws DataAccessException {
        // set pre-state
        setup(authDAO);
        String authToken = authDAO.createAuth("testUser");
        // confirm pre-state
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform action, compare post-state
        assertFalse(authDAO.verifyAuthToken("wrong" + authToken));
        tearDown(authDAO);
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void getUsernamePositive(AuthDAO authDAO) throws DataAccessException {
        // set pre-state
        setup(authDAO);
        String authToken = authDAO.createAuth("testUser");
        // perform action, compare post-state
        assertEquals(authDAO.getUsername(authToken), "testUser");
        tearDown(authDAO);
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void getUsernameNegative(AuthDAO authDAO) throws DataAccessException {
        // set pre-state
        setup(authDAO);
        String authToken = authDAO.createAuth("testUser");
        // perform action, compare post-state
        assertNotEquals(authDAO.getUsername("wrong" + authToken), "testUser");
        tearDown(authDAO);
    }

    @ParameterizedTest
    @MethodSource("authDAOImplementationsUnderTest")
    public void ClearAuthsPositive(AuthDAO authDAO) throws DataAccessException {
        // set pre-state
        setup(authDAO);
        String authToken0 = authDAO.createAuth("testUser0");
        String authToken1 = authDAO.createAuth("testUser1");
        String authToken2 = authDAO.createAuth("testUser2");
        // confirm pre-state
        assertEquals(authDAO.getUsername(authToken0), "testUser0");
        assertEquals(authDAO.getUsername(authToken1), "testUser1");
        assertEquals(authDAO.getUsername(authToken2), "testUser2");
        // perform action
        authDAO.clearAuths();
        // compare post-state
        assertNull(authDAO.getUsername(authToken0));
        assertNull(authDAO.getUsername(authToken1));
        assertNull(authDAO.getUsername(authToken2));
        tearDown(authDAO);
    }
}