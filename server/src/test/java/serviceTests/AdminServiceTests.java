package serviceTests;

import dataAccess.*;
import dataAccess.memoryDAO.MemoryAuthDAO;
import dataAccess.memoryDAO.MemoryGameDAO;
import dataAccess.memoryDAO.MemoryUserDAO;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AdminService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTests {
    private AdminService adminService;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    @BeforeEach
    public void setup() {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();
        adminService = new AdminService(authDAO, gameDAO, userDAO);
        adminService.clearApp();
    }

    @AfterEach
    public void tearDown() {
        adminService.clearApp();
    }

    @Test
    public void clearAppTestPositive() {
        // set pre-state
        String preAuthToken = authDAO.createAuth("testAuth");
        int preGameID = gameDAO.createGame("testGame");
        try {
            userDAO.createUser(new UserData("testUser", "testPass", "testEmail"));
        } catch (DataAccessException e) {
            fail("Could not create user");
        }
        // confirm pre-state
        assertTrue(authDAO.verifyAuthToken(preAuthToken));
        assertNotNull(gameDAO.getGame(preGameID));
        assertNotEquals(gameDAO.listGames(), (new ArrayList<>()));
        assertNotNull(userDAO.getUser("testUser"));
        // perform clear
        adminService.clearApp();
        // compare post-state
        assertFalse(authDAO.verifyAuthToken(preAuthToken));
        assertNull(gameDAO.getGame(preGameID));
        assertEquals(gameDAO.listGames(), (new ArrayList<>()));
        assertNull(userDAO.getUser("testUser"));
    }
}