package service;

import chess.ChessGame;
import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import model.GameData;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.response.CreateGameResponse;
import model.response.ListGamesResponse;
import service.serviceExceptions.AlreadyTakenException;
import service.serviceExceptions.BadRequestException;
import service.serviceExceptions.UnauthorizedException;

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
     * @return a response with the list of all current games
     * @throws BadRequestException   if the authToken is null
     * @throws UnauthorizedException if the authToken is invalid
     */
    public ListGamesResponse listGames(String authToken) throws UnauthorizedException, BadRequestException {
        try {
            if (!authDAO.verifyAuthToken(authToken)) {
                throw new UnauthorizedException("bad auth token");
            }
        } catch (DataAccessException e) {
            throw new BadRequestException("null auth token");
        }
        return new ListGamesResponse(gameDAO.listGames());
    }

    /**
     * Create a new game in the database (can have the same name as another game)
     *
     * @param authToken the authentication token for the current session
     * @param request   a request containing the desired display name of the game
     * @return a response with the internal gameID
     * @throws BadRequestException   if the authToken or gameName is null
     * @throws UnauthorizedException if the authToken is invalid
     */
    public CreateGameResponse createGame(String authToken, CreateGameRequest request)
            throws UnauthorizedException, BadRequestException {
        try {
            if (!authDAO.verifyAuthToken(authToken)) {
                throw new UnauthorizedException("bad auth token");
            }
        } catch (DataAccessException e) {
            throw new BadRequestException("null auth token");
        }
        try {
            return new CreateGameResponse(gameDAO.createGame(request.gameName()));
        } catch (DataAccessException e) {
            throw new BadRequestException("null gameName");
        }
    }

    /**
     * Join a game in the database
     *
     * @param authToken the authentication token for the current session
     * @param request   a request containing the player color to join as and the ID of the game to enter
     * @throws BadRequestException   if the requested game does not exist or the authToken is null
     * @throws UnauthorizedException if the authToken is invalid
     * @throws AlreadyTakenException if the requested team position is already claimed
     */
    public void joinGame(String authToken, JoinGameRequest request)
            throws BadRequestException, UnauthorizedException, AlreadyTakenException {
        try {
            if (!authDAO.verifyAuthToken(authToken)) {
                throw new UnauthorizedException("bad auth token");
            }
        } catch (DataAccessException e) {
            throw new BadRequestException("null auth token");
        }
        GameData gameData = gameDAO.getGame(request.gameID());
        if (gameData == null) {
            throw new BadRequestException("requested game does not exist");
        }
        if ((request.playerColor() == null) || request.playerColor().isEmpty()) {
            return; // observation mode
        }
        ChessGame.TeamColor playerColor = (request.playerColor().equals("WHITE"))
                ? ChessGame.TeamColor.WHITE
                : ChessGame.TeamColor.BLACK;
        if (((playerColor == ChessGame.TeamColor.WHITE)
                ? (gameData.whiteUsername() != null) : (gameData.blackUsername() != null))) {
            throw new AlreadyTakenException("requested team already claimed");
        }
        String playerUsername;
        try {
            playerUsername = authDAO.getUsername(authToken);
        } catch (DataAccessException e) {
            throw new BadRequestException("null authToken");
        }
        String newWhiteUsername = (playerColor == ChessGame.TeamColor.WHITE) ? playerUsername : gameData.whiteUsername();
        String newBlackUsername = (playerColor == ChessGame.TeamColor.BLACK) ? playerUsername : gameData.blackUsername();
        gameData = new GameData(
                gameData.gameID(), newWhiteUsername, newBlackUsername, gameData.gameName(), gameData.game());
        // convert any DataAccessException -> BadRequestException
        try {
            gameDAO.updateGame(request.gameID(), gameData);
        } catch (DataAccessException e) {
            throw new BadRequestException("game does not exist");
        }
    }
}
