package service;

import chess.ChessGame;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;

import java.util.ArrayList;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    /**
     * User Service which takes DAOs and uses them to fulfill requests
     */
    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    /**
     * @param authToken the authentication token for the current session
     * @return a list of all current games
     * @throws
     */
    public ArrayList<ChessGame> listGames(String authToken) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @param authToken the authentication token for the current session
     * @param gameName  the desired display name of the game
     * @return the internal gameID
     * @throws
     */
    public int createGame(String authToken, String gameName) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @param authToken   the authentication token for the current session
     * @param playerColor the desired player color to join as
     * @param gameID      the ID of the desired game to enter
     * @throws
     */
    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) {
        throw new RuntimeException("Not implemented");
    }
}
