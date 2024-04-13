package service;

import chess.ChessGame;
import chess.InvalidMoveException;
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
import service.serviceExceptions.ServerErrorException;
import service.serviceExceptions.UnauthorizedException;
import webSocketMessages.userCommands.*;

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

    // HTTP Service Request Methods ///////////////////////////////////////////////////////////////

    /**
     * Return a list of all the games in the database with their metadata
     *
     * @param authToken the authentication token for the current session
     * @return a response with the list of all current games
     * @throws BadRequestException   if the authToken is null
     * @throws UnauthorizedException if the authToken is invalid
     */
    public ListGamesResponse listGames(String authToken)
            throws UnauthorizedException, BadRequestException, ServerErrorException {
        this.assertAuthTokenVerified(authToken);
        try {
            return new ListGamesResponse(this.gameDAO.listGames());
        } catch (DataAccessException e) {
            throw new ServerErrorException("database connection failed");
        }
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
        this.assertAuthTokenVerified(authToken);
        try {
            return new CreateGameResponse(this.gameDAO.createGame(request.gameName()));
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
            throws BadRequestException, UnauthorizedException, AlreadyTakenException, ServerErrorException {
        this.assertAuthTokenVerified(authToken);
        GameData gameData;
        try {
            gameData = this.gameDAO.getGame(request.gameID());
        } catch (DataAccessException e) {
            throw new ServerErrorException("Database access error");
        }
        if (gameData == null) {
            throw new BadRequestException("requested game does not exist");
        }
        if ((request.playerColor() == null) || request.playerColor().isEmpty()) {
            return; // observation mode
        }
        ChessGame.TeamColor playerColor = (request.playerColor().equalsIgnoreCase("WHITE"))
                ? ChessGame.TeamColor.WHITE
                : ChessGame.TeamColor.BLACK;
        if (((playerColor == ChessGame.TeamColor.WHITE)
                ? (gameData.whiteUsername() != null) : (gameData.blackUsername() != null))) {
            throw new AlreadyTakenException("requested team already claimed");
        }
        String playerUsername;
        try {
            playerUsername = this.authDAO.getUsername(authToken);
        } catch (DataAccessException e) {
            throw new BadRequestException("null authToken");
        }
        String newWhiteUsername = (playerColor == ChessGame.TeamColor.WHITE) ? playerUsername : gameData.whiteUsername();
        String newBlackUsername = (playerColor == ChessGame.TeamColor.BLACK) ? playerUsername : gameData.blackUsername();
        gameData = new GameData(
                gameData.gameID(), newWhiteUsername, newBlackUsername, gameData.gameName(), gameData.game());
        // convert any DataAccessException -> BadRequestException
        try {
            this.gameDAO.updateGame(request.gameID(), gameData);
        } catch (DataAccessException e) {
            throw new BadRequestException("game does not exist");
        }
    }

    // Websocket Client Command Methods

    public void joinPlayer(JoinPlayerCommand command) throws UnauthorizedException, BadRequestException {
        this.assertAuthTokenVerified(command.getAuthString());
        // TODO: Return GameData from all websocket methods (or make a getGame() method)
        // return this.gameDAO.getGame(command.getGameID());
    }

    public void joinObserver(JoinObserverCommand command) throws UnauthorizedException, BadRequestException {
        this.assertAuthTokenVerified(command.getAuthString());
    }

    //
    public void makeMove(MakeMoveCommand command)
            throws UnauthorizedException, BadRequestException, ServerErrorException {
        this.assertAuthTokenVerified(command.getAuthString());
        try {
            GameData gameData = this.getExistingGame(command.getGameID());
            // attempt the requested move (throw exception on invalid move)
            gameData.game().makeMove(command.getMove());
            // reinsert the post-move game to DAO
            this.gameDAO.updateGame(gameData.gameID(), gameData);
        } catch (DataAccessException e) {
            throw new ServerErrorException("could not communicate with database");
        } catch (InvalidMoveException e) {
            throw new BadRequestException("Invalid Move. " + e.getMessage());
        }
    }

    public void leaveGame(LeaveCommand command)
            throws UnauthorizedException, BadRequestException, ServerErrorException {
        this.assertAuthTokenVerified(command.getAuthString());
        try {
            GameData gameData = this.getExistingGame(command.getGameID());
            // remove the root client from the game
            String rootClientUsername = this.authDAO.getUsername(command.getAuthString());
            if (gameData.whiteUsername().equals(rootClientUsername)) {
                gameData = new GameData(gameData.gameID(),
                        null, gameData.blackUsername(), gameData.gameName(), gameData.game());
            } else if (gameData.blackUsername().equals(rootClientUsername)) {
                gameData = new GameData(gameData.gameID(), gameData.whiteUsername(),
                        null, gameData.gameName(), gameData.game());
            } else {
                throw new BadRequestException("not a player in the specified game");
            }
            this.gameDAO.updateGame(gameData.gameID(), gameData);
        } catch (DataAccessException e) {
            throw new ServerErrorException("database error: " + e.getMessage());
        }
    }

    public void resignGame(ResignCommand command)
            throws UnauthorizedException, BadRequestException, ServerErrorException {
        this.assertAuthTokenVerified(command.getAuthString());
        try {
            GameData gameData = this.getExistingGame(command.getGameID());
            // set the game to over
            gameData.game().setOver();
            // reinsert the game to DAO
            this.gameDAO.updateGame(gameData.gameID(), gameData);
        } catch (DataAccessException e) {
            throw new ServerErrorException("could not communicate with database");
        }
    }

    private void assertAuthTokenVerified(String authToken) throws UnauthorizedException, BadRequestException {
        try {
            if (!this.authDAO.verifyAuthToken(authToken)) {
                throw new UnauthorizedException("bad auth token");
            }
        } catch (DataAccessException e) {
            throw new BadRequestException("null auth token");
        }
    }

    private GameData getExistingGame(int gameID) throws DataAccessException, BadRequestException {
        // fetch game data from DAO by ID
        GameData gameData = this.gameDAO.getGame(gameID);
        // assert that the game data exists
        if ((gameData == null) || (gameData.game() == null)) {
            throw new BadRequestException("game does not exist");
        }
        return gameData;
    }
}
