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
import service.serviceExceptions.ServerErrorException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTests {
    private AdminService adminService;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    @BeforeEach
    public void setup() throws ServerErrorException {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();
        adminService = new AdminService(authDAO, gameDAO, userDAO);
        adminService.clearApp();
    }

    @AfterEach
    public void tearDown() throws ServerErrorException {
        adminService.clearApp();
    }

    @Test
    public void clearAppTestPositive() throws DataAccessException, ServerErrorException {
        // set pre-state
        String preAuthToken = authDAO.createAuth("testAuth");
        int preGameID = gameDAO.createGame("testGame");
        userDAO.createUser(new UserData("testUser", "testPass", "testEmail"));
        // confirm pre-state
        assertTrue(authDAO.verifyAuthToken(preAuthToken));
        assertNotNull(gameDAO.getGame(preGameID));
        assertNotEquals(gameDAO.listGames(), (new ArrayList<>()));
        assertNotNull(userDAO.getEmail("testUser"));
        // perform clear
        adminService.clearApp();
        // compare post-state
        assertFalse(authDAO.verifyAuthToken(preAuthToken));
        assertNull(gameDAO.getGame(preGameID));
        assertEquals(gameDAO.listGames(), (new ArrayList<>()));
        assertNull(userDAO.getEmail("testUser"));
    }
}