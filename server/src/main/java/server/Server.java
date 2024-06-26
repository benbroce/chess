package server;

import com.google.gson.Gson;
import dataAccess.*;
import dataAccess.databaseDAO.DatabaseAuthDAO;
import dataAccess.databaseDAO.DatabaseGameDAO;
import dataAccess.databaseDAO.DatabaseUserDAO;
import model.request.CreateGameRequest;
import model.response.*;
import model.request.JoinGameRequest;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import server.websocket.WebSocketHandler;
import service.AdminService;
import service.GameService;
import service.UserService;
import service.serviceExceptions.AlreadyTakenException;
import service.serviceExceptions.BadRequestException;
import service.serviceExceptions.ServerErrorException;
import service.serviceExceptions.UnauthorizedException;
import spark.*;

public class Server {
    // Services
    private final AdminService adminService;
    private final UserService userService;
    private final GameService gameService;
    private final WebSocketHandler webSocketHandler;

    public Server() {
        try {
            // create the database if it doesn't already exist
            DatabaseManager.createDatabase();
            // initialize DAO (Data Access Object) instances
            AuthDAO authDAO = new DatabaseAuthDAO();
            GameDAO gameDAO = new DatabaseGameDAO();
            UserDAO userDAO = new DatabaseUserDAO();
            // initialize service instances
            this.adminService = new AdminService(authDAO, gameDAO, userDAO);
            this.userService = new UserService(authDAO, userDAO);
            this.gameService = new GameService(authDAO, gameDAO);
            // initialize websocket handler
            this.webSocketHandler = new WebSocketHandler(this.gameService, authDAO);
        } catch (DataAccessException e) {
            // TODO: more graceful server failure?
            throw new RuntimeException("Error: could not connect to database");
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // endpoint registration
        Spark.webSocket("/connect", this.webSocketHandler);
        Spark.delete("/db", this::clearApp);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);

        // exception handling
        Spark.exception(AlreadyTakenException.class, this::alreadyTakenHandler);
        Spark.exception(BadRequestException.class, this::badRequestHandler);
        Spark.exception(UnauthorizedException.class, this::unauthorizedHandler);
        Spark.exception(ServerErrorException.class, this::serverErrorHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    // Endpoint Handlers

    private Object clearApp(Request req, Response res) throws ServerErrorException {
        this.adminService.clearApp();
        res.status(200);
        return "";
    }

    private Object register(Request req, Response res) throws AlreadyTakenException, BadRequestException {
        RegisterRequest requestBody = (new Gson()).fromJson(req.body(), RegisterRequest.class);
        RegisterResponse responseBody = this.userService.register(requestBody);
        res.type("application/json");
        res.status(200);
        return (new Gson()).toJson(responseBody);
    }

    private Object login(Request req, Response res) throws UnauthorizedException, BadRequestException {
        LoginRequest requestBody = (new Gson()).fromJson(req.body(), LoginRequest.class);
        LoginResponse responseBody = this.userService.login(requestBody);
        res.type("application/json");
        res.status(200);
        return (new Gson()).toJson(responseBody);
    }

    private Object logout(Request req, Response res) throws UnauthorizedException, BadRequestException {
        String authToken = req.headers("Authorization");
        this.userService.logout(authToken);
        res.status(200);
        return "";
    }

    private Object listGames(Request req, Response res)
            throws UnauthorizedException, BadRequestException, ServerErrorException {
        String authToken = req.headers("Authorization");
        ListGamesResponse responseBody = this.gameService.listGames(authToken);
        res.type("application/json");
        res.status(200);
        return (new Gson()).toJson(responseBody);
    }

    private Object createGame(Request req, Response res) throws UnauthorizedException, BadRequestException {
        String authToken = req.headers("Authorization");
        CreateGameRequest requestBody = (new Gson()).fromJson(req.body(), CreateGameRequest.class);
        CreateGameResponse responseBody = this.gameService.createGame(authToken, requestBody);
        res.type("application/json");
        res.status(200);
        return (new Gson()).toJson(responseBody);
    }

    private Object joinGame(Request req, Response res)
            throws UnauthorizedException, BadRequestException, AlreadyTakenException, ServerErrorException {
        String authToken = req.headers("Authorization");
        JoinGameRequest requestBody = (new Gson()).fromJson(req.body(), JoinGameRequest.class);
        this.gameService.joinGame(authToken, requestBody);
        res.status(200);
        return "";
    }

    // Exception Handlers

    public void alreadyTakenHandler(AlreadyTakenException e, Request req, Response res) {
        String body = (new Gson()).toJson(new FailureResponse("Error: Already Taken - " + e.getMessage()));
        res.type("application/json");
        res.status(403);
        res.body(body);
    }

    public void badRequestHandler(BadRequestException e, Request req, Response res) {
        String body = (new Gson()).toJson(new FailureResponse("Error: Bad Request - " + e.getMessage()));
        res.type("application/json");
        res.status(400);
        res.body(body);
    }

    public void unauthorizedHandler(UnauthorizedException e, Request req, Response res) {
        String body = (new Gson()).toJson(new FailureResponse("Error: Unauthorized - " + e.getMessage()));
        res.type("application/json");
        res.status(401);
        res.body(body);
    }

    public void serverErrorHandler(ServerErrorException e, Request req, Response res) {
        String body = (new Gson()).toJson(new FailureResponse("Error: Server Failed - " + e.getMessage()));
        res.type("application/json");
        res.status(500);
        res.body(body);
    }
}