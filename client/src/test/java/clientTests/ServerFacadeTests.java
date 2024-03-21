package clientTests;

import clientAPI.ResponseException;
import clientAPI.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() throws ResponseException {
        // set up server
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        // set up client server facade
        serverFacade = new ServerFacade("http://localhost:" + port);
        serverFacade.clearApp();
    }

    @AfterAll
    static void stopServer() throws ResponseException {
        serverFacade.clearApp();
        server.stop();
    }

    @Test
    public void isLoggedInTestPositive() {
        Assertions.assertTrue(true);
    }

    @Test
    public void isLoggedInTestNegative() {
        Assertions.assertTrue(true);
    }

    // Admin Methods //////////////////////////////////////////////////////////////////////////////

    @Test
    public void clearAppTestPositive() {
        Assertions.assertTrue(true);
    }

    @Test
    public void clearAppTestNegative() {
        Assertions.assertTrue(true);
    }

    // User Methods ///////////////////////////////////////////////////////////////////////////////

    @Test
    public void registerTestPositive() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerTestNegative() {
        Assertions.assertTrue(true);
    }

    @Test
    public void loginTestPositive() {
        Assertions.assertTrue(true);
    }

    @Test
    public void loginTestNegative() {
        Assertions.assertTrue(true);
    }

    @Test
    public void logoutTestPositive() {
        Assertions.assertTrue(true);
    }

    @Test
    public void logoutTestNegative() {
        Assertions.assertTrue(true);
    }

    // Game Methods ///////////////////////////////////////////////////////////////////////////////

    @Test
    public void listGamesTestPositive() {
        Assertions.assertTrue(true);
    }

    @Test
    public void listGamesTestNegative() {
        Assertions.assertTrue(true);
    }

    @Test
    public void createGameTestPositive() {
        Assertions.assertTrue(true);
    }

    @Test
    public void createGameTestNegative() {
        Assertions.assertTrue(true);
    }

    @Test
    public void joinGameTestPositive() {
        Assertions.assertTrue(true);
    }

    @Test
    public void joinGameTestNegative() {
        Assertions.assertTrue(true);
    }
}
