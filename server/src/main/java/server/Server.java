package server;

import com.google.gson.Gson;
import dataAccess.*;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import service.AdminService;
import service.GameService;
import service.UserService;
import service.serviceExceptions.AlreadyTakenException;
import service.serviceExceptions.BadRequestException;
import service.serviceExceptions.UnauthorizedException;
import spark.*;

public class Server {
    // Services
    private final AdminService adminService;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        // initialize DAO instances
        // Data Access Objects
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        UserDAO userDAO = new MemoryUserDAO();
        // initialize service instances
        this.adminService = new AdminService(authDAO, gameDAO, userDAO);
        this.userService = new UserService(authDAO, userDAO);
        this.gameService = new GameService(authDAO, gameDAO);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // endpoint registration
        Spark.delete("/db", this::clearApp);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);

        // exception handling
//        Spark.exception(AlreadyTakenException.class, );

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    // Endpoint Handlers

    private Object clearApp(Request req, Response res) {
        this.adminService.clearApp();
        res.status(200);
        return "";
    }

    private Object register(Request req, Response res) throws AlreadyTakenException {
        RegisterRequest requestBody = (new Gson()).fromJson(req.body(), RegisterRequest.class);
        RegisterResponse responseBody = this.userService.register(requestBody);
        res.status(200);
        return (new Gson()).toJson(responseBody);
    }

    private Object login(Request req, Response res) throws UnauthorizedException {
        LoginRequest requestBody = (new Gson()).fromJson(req.body(), LoginRequest.class);
        LoginResponse responseBody = this.userService.login(requestBody);
        res.status(200);
        return (new Gson()).toJson(responseBody);
    }

    private Object logout(Request req, Response res) throws UnauthorizedException {
        String authToken = req.headers("Authorization");
        this.userService.logout(authToken);
        res.status(200);
        return "";
    }

    private Object listGames(Request req, Response res) throws UnauthorizedException {
        String authToken = req.headers("Authorization");
        ListGamesResponse responseBody = this.gameService.listGames(authToken);
        res.status(200);
        return (new Gson()).toJson(responseBody);
    }

    private Object createGame(Request req, Response res) throws UnauthorizedException {
        String authToken = req.headers("Authorization");
        CreateGameRequest requestBody = (new Gson()).fromJson(req.body(), CreateGameRequest.class);
        CreateGameResponse responseBody = this.gameService.createGame(authToken, requestBody);
        res.status(200);
        return (new Gson()).toJson(responseBody);
    }

    private Object joinGame(Request req, Response res)
            throws UnauthorizedException, BadRequestException, AlreadyTakenException {
        String authToken = req.headers("Authorization");
        JoinGameRequest requestBody = (new Gson()).fromJson(req.body(), JoinGameRequest.class);
        this.gameService.joinGame(authToken, requestBody);
        res.status(200);
        return "";
    }

    // Exception Handlers

//    private void alreadyTakenHandler(AlreadyTakenException ex, Request req, Response res) {
//        res.status(403);
//        res.body()
//    }
}