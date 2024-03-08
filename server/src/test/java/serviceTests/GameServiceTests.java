package serviceTests;

import dataAccess.*;
import dataAccess.memoryDAO.MemoryAuthDAO;
import dataAccess.memoryDAO.MemoryGameDAO;
import model.GameData;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.GameService;
import service.serviceExceptions.AlreadyTakenException;
import service.serviceExceptions.BadRequestException;
import service.serviceExceptions.UnauthorizedException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTests {
    private GameService gameService;
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(authDAO, gameDAO);
        authDAO.clearAuths();
        gameDAO.clearGames();
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        authDAO.clearAuths();
        gameDAO.clearGames();
    }

    @Test
    public void listGamesTestPositive() throws BadRequestException, UnauthorizedException, DataAccessException {
        // set pre-state
        int gameID0 = gameDAO.createGame("game0");
        int gameID1 = gameDAO.createGame("game1");
        String authToken = "";
        try {
            authToken = authDAO.createAuth("testUser");
        } catch (DataAccessException e) {
            fail("invalid username");
        }
        // confirm pre-state
        assertNotNull(gameDAO.getGame(gameID0));
        assertNotNull(gameDAO.getGame(gameID1));
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform listGames
        ArrayList<GameData> gameList;
        gameList = gameService.listGames(authToken).games();
        // compare post-state
        assertEquals(gameList.get(0).gameName(), "game0");
        assertEquals(gameList.get(1).gameName(), "game1");
    }

    @Test
    public void listGamesTestNegative() throws DataAccessException {
        // set pre-state
        int gameID1 = gameDAO.createGame("game0");
        int gameID2 = gameDAO.createGame("game1");
        String authToken = "";
        try {
            authToken = authDAO.createAuth("testUser");
        } catch (DataAccessException e) {
            fail("invalid username");
        }
        // confirm pre-state
        assertNotNull(gameDAO.getGame(gameID1));
        assertNotNull(gameDAO.getGame(gameID2));
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform listGames, compare post-state (thrown credentialing exception)
        assertThrows(UnauthorizedException.class, (() -> gameService.listGames("wrongAuthToken")));
    }

    @Test
    public void createGameTestPositive() throws BadRequestException, DataAccessException {
        // set pre-state
        String authToken = "";
        try {
            authToken = authDAO.createAuth("testUser");
        } catch (DataAccessException e) {
            fail("invalid username");
        }
        // confirm pre-state
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform createGame
        int gameID = -1;
        try {
            gameID = gameService.createGame(authToken, (new CreateGameRequest("testGame"))).gameID();
        } catch (UnauthorizedException e) {
            fail("Unauthorized to create game");
        }
        // compare post-state
        assertNotNull(gameDAO.getGame(gameID));
    }

    @Test
    public void createGameTestNegative() throws DataAccessException {
        // set pre-state
        String authToken = "";
        try {
            authToken = authDAO.createAuth("testUser");
        } catch (DataAccessException e) {
            fail("invalid username");
        }
        // confirm pre-state
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform createGame (invalid, bad authentication)
        assertThrows(UnauthorizedException.class,
                (() -> gameService.createGame("wrongAuthToken",
                        (new CreateGameRequest("testGame")))));
        // compare post-state
        assertEquals(gameDAO.listGames().size(), 0);
    }

    @Test
    public void joinGameTestPositive() throws DataAccessException {
        // set pre-state
        int gameID = gameDAO.createGame("game0");
        String authToken = "";
        try {
            authToken = authDAO.createAuth("testUser");
        } catch (DataAccessException e) {
            fail("invalid username");
        }
        // confirm pre-state
        assertNotNull(gameDAO.getGame(gameID));
        assertNull(gameDAO.getGame(gameID).whiteUsername());
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform joinGame
        try {
            gameService.joinGame(authToken, new JoinGameRequest("WHITE", gameID));
        } catch (BadRequestException e) {
            fail("bad request");
        } catch (UnauthorizedException e) {
            fail("unauthorized");
        } catch (AlreadyTakenException e) {
            fail("position already taken");
        }
        // compare post-state
        assertEquals(gameDAO.getGame(gameID).whiteUsername(), "testUser");
    }

    @Test
    public void joinGameTestNegative() throws DataAccessException {
        // set pre-state
        int gameID = gameDAO.createGame("game0");
        String authToken = "";
        String opponentAuthToken = "";
        try {
            authToken = authDAO.createAuth("testUser");
            opponentAuthToken = authDAO.createAuth("opponent");
        } catch (DataAccessException e) {
            fail("invalid username");
        }
        // confirm pre-state
        assertNotNull(gameDAO.getGame(gameID));
        assertNull(gameDAO.getGame(gameID).whiteUsername());
        assertEquals(authDAO.getUsername(authToken), "testUser");
        assertEquals(authDAO.getUsername(opponentAuthToken), "opponent");
        // perform invalid joinGame (game doesn't exist), confirm throw
        String finalAuthToken = authToken;
        assertThrows(BadRequestException.class,
                (() -> gameService.joinGame(finalAuthToken, new JoinGameRequest("WHITE", -1))));
        // perform invalid joinGame (credentials incorrect), confirm throw
        assertThrows(UnauthorizedException.class,
                (() -> gameService.joinGame("wrongAuthToken",
                        new JoinGameRequest("BLACK", gameID))));
        // perform invalid joinGame (position already taken by opponent), confirm throw
        try {
            gameService.joinGame(opponentAuthToken, new JoinGameRequest("BLACK", gameID));
        } catch (BadRequestException e) {
            fail("opponent bad request");
        } catch (UnauthorizedException e) {
            fail("opponent unauthorized");
        } catch (AlreadyTakenException e) {
            fail("opponent position already taken");
        }
        String finalAuthToken1 = authToken;
        assertThrows(AlreadyTakenException.class,
                (() -> gameService.joinGame(finalAuthToken1, new JoinGameRequest("BLACK", gameID))));
        // compare post-state
        assertEquals(gameDAO.getGame(gameID).blackUsername(), "opponent");
    }
}