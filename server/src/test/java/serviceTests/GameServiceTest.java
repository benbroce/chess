package serviceTests;

import chess.ChessGame;
import dataAccess.*;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.GameService;
import service.serviceExceptions.AlreadyTakenException;
import service.serviceExceptions.BadRequestException;
import service.serviceExceptions.UnauthorizedException;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    private GameService gameService;
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    @BeforeEach
    public void setup() {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(authDAO, gameDAO);
    }

    @Test
    public void listGamesTestPositive() {
        // set pre-state
        int gameID0 = gameDAO.createGame("game0");
        int gameID1 = gameDAO.createGame("game1");
        String authToken = authDAO.createAuth("testUser");
        // confirm pre-state
        assertNotNull(gameDAO.getGame(gameID0));
        assertNotNull(gameDAO.getGame(gameID1));
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform listGames
        ArrayList<GameData> gameList = new ArrayList<>();
        try {
            gameList = gameService.listGames(authToken);
        } catch (UnauthorizedException e) {
            fail("bad auth token");
        }
        // compare post-state
        assertEquals(gameList.get(0).gameName(), "game0");
        assertEquals(gameList.get(1).gameName(), "game1");
    }

    @Test
    public void listGamesTestNegative() {
        // set pre-state
        int gameID1 = gameDAO.createGame("game0");
        int gameID2 = gameDAO.createGame("game1");
        String authToken = authDAO.createAuth("testUser");
        // confirm pre-state
        assertNotNull(gameDAO.getGame(gameID1));
        assertNotNull(gameDAO.getGame(gameID2));
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform listGames, compare post-state (thrown credentialing exception)
        assertThrows(UnauthorizedException.class, (() -> gameService.listGames("wrongAuthToken")));
    }

    @Test
    public void createGameTestPositive() {
        // set pre-state
        String authToken = authDAO.createAuth("testUser");
        // confirm pre-state
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform createGame
        int gameID = -1;
        try {
            gameID = gameService.createGame(authToken, "testGame");
        } catch (UnauthorizedException e) {
            fail("Unauthorized to create game");
        }
        // compare post-state
        assertNotNull(gameDAO.getGame(gameID));
    }

    @Test
    public void createGameTestNegative() {
        // set pre-state
        String authToken = authDAO.createAuth("testUser");
        // confirm pre-state
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform createGame (invalid, bad authentication)
        assertThrows(UnauthorizedException.class,
                (() -> gameService.createGame("wrongAuthToken", "testGame")));
        // compare post-state
        assertEquals(gameDAO.listGames().size(), 0);
    }

    @Test
    public void joinGameTestPositive() {
        // set pre-state
        int gameID = gameDAO.createGame("game0");
        String authToken = authDAO.createAuth("testUser");
        // confirm pre-state
        assertNotNull(gameDAO.getGame(gameID));
        assertNull(gameDAO.getGame(gameID).whiteUsername());
        assertEquals(authDAO.getUsername(authToken), "testUser");
        // perform joinGame
        try {
            gameService.joinGame(authToken, ChessGame.TeamColor.WHITE, gameID);
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
    public void joinGameTestNegative() {
        // set pre-state
        int gameID = gameDAO.createGame("game0");
        String authToken = authDAO.createAuth("testUser");
        String opponentAuthToken = authDAO.createAuth("opponent");
        // confirm pre-state
        assertNotNull(gameDAO.getGame(gameID));
        assertNull(gameDAO.getGame(gameID).whiteUsername());
        assertEquals(authDAO.getUsername(authToken), "testUser");
        assertEquals(authDAO.getUsername(opponentAuthToken), "opponent");
        // perform invalid joinGame (game doesn't exist), confirm throw
        assertThrows(BadRequestException.class,
                (() -> gameService.joinGame(authToken, ChessGame.TeamColor.WHITE, -1)));
        // perform invalid joinGame (credentials incorrect), confirm throw
        assertThrows(UnauthorizedException.class,
                (() -> gameService.joinGame("wrongAuthToken", ChessGame.TeamColor.BLACK, gameID)));
        // perform invalid joinGame (position already taken by opponent), confirm throw
        try {
            gameService.joinGame(opponentAuthToken, ChessGame.TeamColor.BLACK, gameID);
        } catch (BadRequestException e) {
            fail("opponent bad request");
        } catch (UnauthorizedException e) {
            fail("opponent unauthorized");
        } catch (AlreadyTakenException e) {
            fail("opponent position already taken");
        }
        assertThrows(AlreadyTakenException.class,
                (() -> gameService.joinGame(authToken, ChessGame.TeamColor.BLACK, gameID)));
        // compare post-state
        assertEquals(gameDAO.getGame(gameID).blackUsername(), "opponent");
    }
}