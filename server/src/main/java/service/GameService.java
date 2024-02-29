package service;

import chess.ChessGame;
import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import model.GameData;
import service.serviceExceptions.AlreadyTakenException;
import service.serviceExceptions.BadRequestException;
import service.serviceExceptions.UnauthorizedException;

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
     * Return a list of all the games in the database with their metadata
     *
     * @param authToken the authentication token for the current session
     * @return a list of all current games
     * @throws UnauthorizedException if the authToken is invalid
     */
    public ArrayList<GameData> listGames(String authToken) throws UnauthorizedException {
        if (!authDAO.verifyAuthToken(authToken)) {
            throw new UnauthorizedException("bad auth token");
        }
        return gameDAO.listGames();
    }

    /**
     * Create a new game in the database
     *
     * @param authToken the authentication token for the current session
     * @param gameName  the desired display name of the game
     * @return the internal gameID
     * @throws UnauthorizedException if the authToken is invalid
     */
    public int createGame(String authToken, String gameName) throws UnauthorizedException {
        if (!authDAO.verifyAuthToken(authToken)) {
            throw new UnauthorizedException("bad auth token");
        }
        return gameDAO.createGame(gameName);
    }

    /**
     * Join a game in the database
     *
     * @param authToken   the authentication token for the current session
     * @param playerColor the desired player color to join as
     * @param gameID      the ID of the desired game to enter
     * @throws BadRequestException   if the requested game does not exist
     * @throws UnauthorizedException if the authToken is invalid
     * @throws AlreadyTakenException if the requested team position is already claimed
     */
    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws BadRequestException, UnauthorizedException, AlreadyTakenException {
        if (!authDAO.verifyAuthToken(authToken)) {
            throw new UnauthorizedException("bad auth token");
        }
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            throw new BadRequestException("requested game does not exist");
        }
        if (playerColor == null) {
            return; // observation mode
        }
        if (((playerColor == ChessGame.TeamColor.WHITE)
                ? (gameData.whiteUsername() != null) : (gameData.blackUsername() != null))) {
            throw new AlreadyTakenException("requested team already claimed");
        }
        String playerUsername = authDAO.getUsername(authToken);
        String newWhiteUsername = (playerColor == ChessGame.TeamColor.WHITE) ? playerUsername : gameData.whiteUsername();
        String newBlackUsername = (playerColor == ChessGame.TeamColor.BLACK) ? playerUsername : gameData.blackUsername();
        gameData = new GameData(
                gameData.gameID(), newWhiteUsername, newBlackUsername, gameData.gameName(), gameData.game());
        // convert any DataAccessException -> BadRequestException
        try {
            gameDAO.updateGame(gameID, gameData);
        } catch (DataAccessException e) {
            throw new BadRequestException("game does not exist");
        }
    }
}
