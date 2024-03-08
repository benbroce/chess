package serviceTests;

import dataAccess.*;
import dataAccess.memoryDAO.MemoryAuthDAO;
import dataAccess.memoryDAO.MemoryUserDAO;
import model.UserData;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.UserService;
import service.serviceExceptions.AlreadyTakenException;
import service.serviceExceptions.BadRequestException;
import service.serviceExceptions.UnauthorizedException;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTests {
    private UserService userService;
    private AuthDAO authDAO;
    private UserDAO userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new MemoryAuthDAO();
        userDAO = new MemoryUserDAO();
        userService = new UserService(authDAO, userDAO);
        authDAO.clearAuths();
        userDAO.clearUsers();
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        authDAO.clearAuths();
        userDAO.clearUsers();
    }

    @Test
    // successful registration
    public void registerTestPositive() throws DataAccessException {
        // confirm pre-state
        assertNull(userDAO.getUser("testUser"));
        // perform register
        try {
            assertNotNull(userService.register(
                    new RegisterRequest("testUser", "testPass", "testEmail")));
        } catch (AlreadyTakenException e) {
            fail("Username already taken");
        } catch (BadRequestException e) {
            fail("Username or password missing");
        }
        // compare post-state
        assertEquals(userDAO.getUser("testUser"),
                (new UserData("testUser", "testPass", "testEmail")));
    }

    @Test
    // registration failure (username already taken)
    public void registerTestNegative() throws DataAccessException {
        // set pre-state
        try {
            userDAO.createUser(new UserData("testUser", "testPass", "testEmail"));
        } catch (DataAccessException e) {
            fail("Could not create initial user");
        }
        // confirm pre-state (initial user is present in database)
        assertEquals(userDAO.getUser("testUser"),
                (new UserData("testUser", "testPass", "testEmail")));
        // perform register, compare post-state
        // (failed registration due to duplicate username, ensure original user kept)
        assertThrows(AlreadyTakenException.class,
                (() -> userService.register(
                        new RegisterRequest("testUser", "diffPass", "diffEmail"))));
        assertEquals(userDAO.getUser("testUser"),
                (new UserData("testUser", "testPass", "testEmail")));
    }

    @Test
    public void loginTestPositive() throws DataAccessException {
        // set pre-state
        try {
            userDAO.createUser(new UserData("testUser", "testPass", "testEmail"));
        } catch (DataAccessException e) {
            fail("Could not create initial user");
        }
        // confirm pre-state (user is present in database)
        assertEquals(userDAO.getUser("testUser"),
                (new UserData("testUser", "testPass", "testEmail")));
        // perform login
        String authToken = null;
        try {
            authToken = userService.login(new LoginRequest("testUser", "testPass")).authToken();
        } catch (BadRequestException e) {
            fail("Invalid username");
        } catch (UnauthorizedException e) {
            fail("Credentials don't match");
        }
        // compare post-state (user has an authToken that matches)
        assertEquals(authDAO.getUsername(authToken), "testUser");
    }

    @Test
    public void loginTestNegative() throws DataAccessException {
        // set pre-state
        try {
            userDAO.createUser(new UserData("testUser", "testPass", "testEmail"));
        } catch (DataAccessException e) {
            fail("Could not create initial user");
        }
        // confirm pre-state (user is present in database)
        assertEquals(userDAO.getUser("testUser"),
                (new UserData("testUser", "testPass", "testEmail")));
        // perform invalid login, compare post-state (throws a credentialing error)
        assertThrows(UnauthorizedException.class,
                (() -> userService.login(new LoginRequest("testUser", "wrongPass"))));
    }

    @Test
    public void logoutTestPositive() throws DataAccessException {
        // set pre-state
        String authToken = null;
        try {
            userDAO.createUser(new UserData("testUser", "testPass", "testEmail"));
            authToken = authDAO.createAuth("testUser");
        } catch (DataAccessException e) {
            fail("Could not create initial user");
        }
        // confirm pre-state (user, auth are present in database)
        assertEquals(userDAO.getUser("testUser"),
                (new UserData("testUser", "testPass", "testEmail")));
        assertNotNull(authDAO.getUsername(authToken));
        // perform logout
        try {
            userService.logout(authToken);
        } catch (BadRequestException e) {
            fail("Null authToken");
        } catch (UnauthorizedException e) {
            fail("Invalid authToken");
        }
        // compare post-state (auth not present in database)
        assertNull(authDAO.getUsername(authToken));
    }

    @Test
    public void logoutTestNegative() throws DataAccessException {
        // set pre-state
        String authToken = null;
        try {
            userDAO.createUser(new UserData("testUser", "testPass", "testEmail"));
            authToken = authDAO.createAuth("testUser");
        } catch (DataAccessException e) {
            fail("Could not create initial user");
        }
        // confirm pre-state (user, auth are present in database)
        assertEquals(userDAO.getUser("testUser"),
                (new UserData("testUser", "testPass", "testEmail")));
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform invalid logout
        assertThrows(UnauthorizedException.class, (() -> userService.logout("wrongAuthToken")));
        // compare post-state (auth still present in database)
        assertEquals(authDAO.getUsername(authToken), "testUser");
    }
}