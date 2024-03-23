package clientTests;

import clientAPI.ResponseException;
import clientAPI.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {
    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        // set up server
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        // set up client server facade
        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    @AfterEach
    public void cleanup() throws ResponseException {
        serverFacade.clearApp();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    // Admin Methods //////////////////////////////////////////////////////////////////////////////

    @Test
    public void clearAppTestPositive() throws ResponseException {
        serverFacade.register("testUser", "testPass", "testEmail");
        serverFacade.createGame("testGame");
        assertTrue(serverFacade.isLoggedIn());
        assertFalse(serverFacade.listGames().isEmpty());
        serverFacade.clearApp();
        assertFalse(serverFacade.isLoggedIn());
        assertThrows(ResponseException.class, (() -> serverFacade.listGames()));
    }

    @Test
    public void clearAppTestNegative() throws ResponseException {
        serverFacade.clearApp();
        assertFalse(serverFacade.isLoggedIn());
        assertThrows(ResponseException.class, (() -> serverFacade.listGames()));
    }

    // User Methods ///////////////////////////////////////////////////////////////////////////////

    @Test
    public void isLoggedInTestPositive() throws ResponseException {
        serverFacade.register("testUser", "testPass", "testEmail");
        assertTrue(serverFacade.isLoggedIn());
    }

    @Test
    public void isLoggedInTestNegative() {
        assertFalse(serverFacade.isLoggedIn());
    }

    @Test
    public void registerTestPositive() throws ResponseException {
        assertFalse(serverFacade.isLoggedIn());
        serverFacade.register("testUser", "testPass", "testEmail");
        assertTrue(serverFacade.isLoggedIn());
        assertDoesNotThrow(() -> serverFacade.logout());
    }

    @Test
    public void registerTestNegative() {
        assertThrows(ResponseException.class,
                (() -> serverFacade.register(null, "testPass", "testEmail")));
    }

    @Test
    public void loginTestPositive() throws ResponseException {
        serverFacade.register("testUser", "testPass", "testEmail");
        serverFacade.logout();
        assertFalse(serverFacade.isLoggedIn());
        serverFacade.login("testUser", "testPass");
        assertTrue(serverFacade.isLoggedIn());
    }

    @Test
    public void loginTestNegative() throws ResponseException {
        serverFacade.register("testUser", "testPass", "testEmail");
        serverFacade.logout();
        assertFalse(serverFacade.isLoggedIn());
        assertThrows(ResponseException.class, (() -> serverFacade.login("testUser", "wrongPass")));
    }

    @Test
    public void logoutTestPositive() throws ResponseException {
        serverFacade.register("testUser", "testPass", "testEmail");
        assertDoesNotThrow(() -> serverFacade.listGames());
        serverFacade.logout();
        assertThrows(ResponseException.class, (() -> serverFacade.listGames()));
    }

    @Test
    public void logoutTestNegative() {
        assertThrows(ResponseException.class, (() -> serverFacade.logout()));
    }

    // Game Methods ///////////////////////////////////////////////////////////////////////////////

    @Test
    public void listGamesTestPositive() throws ResponseException {
        serverFacade.register("testUser", "testPass", "testEmail");
        serverFacade.createGame("testGame");
        assertEquals(serverFacade.listGames().size(), 1);
        serverFacade.createGame("testGame");
        assertEquals(serverFacade.listGames().size(), 2);
    }

    @Test
    public void listGamesTestNegative() {
        assertThrows(ResponseException.class, (() -> serverFacade.listGames()));
    }

    @Test
    public void createGameTestPositive() throws ResponseException {
        serverFacade.register("testUser", "testPass", "testEmail");
        int gameID = serverFacade.createGame("testGame");
        assertEquals(gameID, serverFacade.listGames().getFirst().gameID());
    }

    @Test
    public void createGameTestNegative() throws ResponseException {
        assertThrows(ResponseException.class, (() -> serverFacade.createGame("testGame")));
        serverFacade.register("testUser", "testPass", "testEmail");
        assertThrows(ResponseException.class, (() -> serverFacade.createGame(null)));
    }

    @Test
    public void joinGameTestPositive() throws ResponseException {
        serverFacade.register("testUser", "testPass", "testEmail");
        int gameID = serverFacade.createGame("testGame");
        serverFacade.joinGame("WHITE", gameID);
        assertEquals(serverFacade.listGames().getFirst().whiteUsername(), "testUser");
    }

    @Test
    public void joinGameTestNegative() throws ResponseException {
        // not authorized
        assertThrows(ResponseException.class, (() -> serverFacade.joinGame("WHITE", 0)));
        // already taken
        serverFacade.register("otherUser", "testPass", "testEmail");
        int gameID = serverFacade.createGame("testGame");
        serverFacade.joinGame("WHITE", gameID);
        serverFacade.register("testUser", "testPass", "testEmail");
        assertThrows(ResponseException.class, (() -> serverFacade.joinGame("WHITE", gameID)));
    }
}
