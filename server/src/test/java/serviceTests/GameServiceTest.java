package serviceTests;

import dataAccess.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.GameService;

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
    }

    @Test
    public void listGamesTestNegative() {
    }

    @Test
    public void createGameTestPositive() {
    }

    @Test
    public void createGameTestNegative() {
    }

    @Test
    public void joinGameTestPositive() {
    }

    @Test
    public void joinGameTestNegative() {
    }
}