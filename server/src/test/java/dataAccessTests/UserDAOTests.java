package dataAccessTests;

import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import dataAccess.memoryDAO.MemoryUserDAO;
import dataAccess.databaseDAO.DatabaseUserDAO;
import model.UserData;
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
    public void createUserPositive(UserDAO userDAO) throws DataAccessException {
        // set pre-state
        userDAO.clearUsers();
        // confirm pre-state
        assertNull(userDAO.getUser("testUser"));
        // perform action
        UserData testUser = new UserData("testUser", "testPass", "testEmail");
        userDAO.createUser(testUser);
        // compare post-state
        assertEquals(userDAO.getUser("testUser"), testUser);
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
        assertEquals(userDAO.getUser("testUser"), initialUser);
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
        assertEquals(userDAO.getUser("testUser"), testUser);
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
        assertNull(userDAO.getUser("otherUser"));
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
        assertEquals(userDAO.getUser("testUser"), testUser);
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
        assertEquals(userDAO.getUser("testUser"), testUser);
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