package dataAccessTests;

import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import dataAccess.databaseDAO.DatabaseUserDAO;
import dataAccess.memoryDAO.MemoryUserDAO;
import model.UserData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTests {
    /**
     * @return a Stream of UserDAO objects of different implementations for testing
     */
    private static Stream<UserDAO> userDAOImplementationsUnderTest() throws DataAccessException {
        return Stream.of((new MemoryUserDAO()), (new DatabaseUserDAO()));
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void createUserPositive(UserDAO userDAO) throws DataAccessException {
        // set pre-state
        userDAO.clearUsers();
        // confirm pre-state
        assertNull(userDAO.getEmail("testUser"));
        // perform action
        UserData testUser = new UserData("testUser", "testPass", "testEmail");
        userDAO.createUser(testUser);
        // compare post-state
        assertTrue(userDAO.verifyUser("testUser", "testPass"));
        userDAO.clearUsers();
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void createUserNegative(UserDAO userDAO) throws DataAccessException {
        // set pre-state
        userDAO.clearUsers();
        UserData initialUser = new UserData("testUser", "testPass", "testEmail");
        userDAO.createUser(initialUser);
        // confirm pre-state
        assertTrue(userDAO.verifyUser("testUser", "testPass"));
        // perform action, compare post-state
        UserData testUser = new UserData("testUser", "diffPass", "diffEmail");
        assertThrows(DataAccessException.class, (() -> userDAO.createUser(testUser)));
        userDAO.clearUsers();
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void getUserPositive(UserDAO userDAO) throws DataAccessException {
        // set pre-state
        userDAO.clearUsers();
        UserData testUser = new UserData("testUser", "testPass", "testEmail");
        userDAO.createUser(testUser);
        // perform action, compare post-state
        assertTrue(userDAO.verifyUser("testUser", "testPass"));
        userDAO.clearUsers();
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void getUserNegative(UserDAO userDAO) throws DataAccessException {
        // set pre-state
        userDAO.clearUsers();
        UserData testUser = new UserData("testUser", "testPass", "testEmail");
        userDAO.createUser(testUser);
        // perform action, compare post-state
        assertFalse(userDAO.verifyUser("otherUser", "testPass"));
        userDAO.clearUsers();
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void verifyUserPositive(UserDAO userDAO) throws DataAccessException {
        // set pre-state
        userDAO.clearUsers();
        UserData testUser = new UserData("testUser", "testPass", "testEmail");
        userDAO.createUser(testUser);
        // confirm pre-state
        assertEquals(userDAO.getEmail("testUser"), testUser.email());
        // perform action, compare post-state
        assertTrue(userDAO.verifyUser("testUser", "testPass"));
        userDAO.clearUsers();
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void verifyUserNegative(UserDAO userDAO) throws DataAccessException {
        // set pre-state
        userDAO.clearUsers();
        UserData testUser = new UserData("testUser", "testPass", "testEmail");
        userDAO.createUser(testUser);
        // confirm pre-state
        assertEquals(userDAO.getEmail("testUser"), testUser.email());
        // perform action, compare post-state
        assertFalse(userDAO.verifyUser("testUser", "badPass"));
        userDAO.clearUsers();
    }

    @ParameterizedTest
    @MethodSource("userDAOImplementationsUnderTest")
    public void clearUsersPositive(UserDAO userDAO) throws DataAccessException {
        // set pre-state
        userDAO.clearUsers();
        userDAO.createUser(new UserData("t1", "t1", "t1"));
        userDAO.createUser(new UserData("t2", "t2", "t2"));
        userDAO.createUser(new UserData("t3", "t3", "t3"));
        // confirm pre-state
        assertTrue(userDAO.verifyUser("t1", "t1"));
        assertTrue(userDAO.verifyUser("t2", "t2"));
        assertTrue(userDAO.verifyUser("t3", "t3"));
        // perform action
        userDAO.clearUsers();
        // compare post-state
        assertFalse(userDAO.verifyUser("t1", "t1"));
        assertFalse(userDAO.verifyUser("t2", "t2"));
        assertFalse(userDAO.verifyUser("t3", "t3"));
        userDAO.clearUsers();
    }
}