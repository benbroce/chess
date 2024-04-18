package clientAPI;

import com.google.gson.Gson;
import model.GameData;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.response.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class ServerFacade {
    private final String serverURL;
    private String authToken;

    public ServerFacade(String url) {
        this.serverURL = url;
        this.authToken = null;
    }

    public String getAuthToken() {
        return authToken;
    }

    // Admin Methods //////////////////////////////////////////////////////////////////////////////

    /**
     * Clear the app of all data (Warning: do not expose to user)
     *
     * @throws ResponseException if the request fails
     */
    public void clearApp() throws ResponseException {
        this.makeRequest("DELETE", "/db", null, null, false);
        this.authToken = null;
    }

    // User Methods ///////////////////////////////////////////////////////////////////////////////

    /**
     * @return Whether the current ServerFacade client session is logged in
     */
    public boolean isLoggedIn() {
        return (authToken != null);
    }

    /**
     * Register a new user
     *
     * @param username proposed username
     * @param password proposed password
     * @param email    user email address
     * @throws ResponseException if the request fails
     */
    public void register(String username, String password, String email) throws ResponseException {
        RegisterRequest req = new RegisterRequest(username, password, email);
        RegisterResponse res = this.makeRequest(
                "POST", "/user", req, RegisterResponse.class, false);
        if (!username.equals(res.username())) {
            throw new ResponseException("The requested username does not match the server record");
        }
        this.authToken = res.authToken();
    }

    /**
     * Login an existing user, creating a session
     *
     * @param username username
     * @param password password
     * @throws ResponseException if the request fails
     */
    public void login(String username, String password) throws ResponseException {
        LoginRequest req = new LoginRequest(username, password);
        LoginResponse res = this.makeRequest(
                "POST", "/session", req, LoginResponse.class, false);
        if (!username.equals(res.username())) {
            throw new ResponseException("The requested username does not match the server record");
        }
        this.authToken = res.authToken();
    }

    /**
     * Logout the user from the current session
     * (requires authentication)
     *
     * @throws ResponseException if the request fails
     */
    public void logout() throws ResponseException {
        this.makeRequest(
                "DELETE", "/session", null, null, true);
        this.authToken = null;
    }

    // Game Methods ///////////////////////////////////////////////////////////////////////////////

    /**
     * Get a list of all games (metadata and state) that exist on the server
     * (requires authentication)
     *
     * @return the list of games
     * @throws ResponseException if the request fails
     */
    public ArrayList<GameData> listGames() throws ResponseException {
        return this.makeRequest(
                        "GET", "/game", null, ListGamesResponse.class, true)
                .games();
    }

    /**
     * Create a new game on the server
     * (requires authentication)
     *
     * @param gameName the display name of the new game
     * @return the gameID of the new game
     * @throws ResponseException if the request fails
     */
    public int createGame(String gameName) throws ResponseException {
        CreateGameRequest req = new CreateGameRequest(gameName);
        return this.makeRequest("POST", "/game", req, CreateGameResponse.class, true)
                .gameID();
    }

    /**
     * Join an existing game on the server
     * (requires authentication)
     *
     * @param playerColor the color team to join
     * @param gameID      the gameID of the game to join
     * @throws ResponseException if the request fails
     */
    public void joinGame(String playerColor, int gameID) throws ResponseException {
        JoinGameRequest req = new JoinGameRequest(playerColor, gameID);
        this.makeRequest("PUT", "/game", req, null, true);
    }

    // Utilities for HTTP requests/responses //////////////////////////////////////////////////////

    /**
     * Make an HTTP request to the server, and return the response
     *
     * @param requestMethod the HTTP method to use ("GET", "POST", etc.)
     * @param path          the path to the requested resource on the server
     * @param requestBody   the request body
     * @param responseClass the Class type of the expected response
     * @param authenticate  whether to authenticate the request with the session authToken
     * @param <T>           a generic for response type (defined by passing in responseClass)
     * @return the server response (of type responseClass)
     * @throws ResponseException if the request fails
     */
    private <T> T makeRequest(String requestMethod,
                              String path,
                              Object requestBody,
                              Class<T> responseClass,
                              boolean authenticate) throws ResponseException {
        try {
            // set up an HTTP connection object with the given method and path
            URL url = (new URI(this.serverURL + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(requestMethod);
            http.setDoOutput(true);
            // write the request body to the outgoing stream of the HTTP connection object
            ServerFacade.writeRequest((authenticate ? this.authToken : null), requestBody, http);
            // establish an HTTP network connection (allowing request stream to be read by server)
            http.connect();
            // on request failure, throw an exception
            if (!ServerFacade.requestSuccessful(http)) {
                throw new Exception(String.format("\n%s\n[Status %d: %s]",
                        ServerFacade.readResponseBody(http, FailureResponse.class).message(),
                        http.getResponseCode(),
                        http.getResponseMessage()));
            }
            // read the server response and return
            return ServerFacade.readResponseBody(http, responseClass);
        } catch (Exception e) {
            throw new ResponseException(e.getMessage());
        }
    }

    /**
     * Write an HTTP request body to the output stream of an HTTP connection object
     *
     * @param authToken   an authorization token, or null if not applicable
     * @param requestBody the request body object
     * @param http        the HTTP connection object
     * @throws IOException if the HTTP output stream cannot be written to
     */
    private static void writeRequest(String authToken, Object requestBody, HttpURLConnection http) throws IOException {
        if (authToken != null) {
            http.addRequestProperty("Authorization", authToken);
        }
        if (requestBody != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(requestBody);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    /**
     * Read an HTTP response body from the input (or error) stream of an HTTP connection object
     *
     * @param http          the HTTP connection object
     * @param responseClass the Class type of the expected response
     * @param <T>           a generic for response type (defined by passing in responseClass)
     * @return the server response (of type responseClass), or null if there is none
     * @throws IOException if the HTTP input (or error) stream cannot be read from
     */
    private static <T> T readResponseBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        try (InputStream responseBody = (requestSuccessful(http) ? http.getInputStream() : http.getErrorStream())) {
            InputStreamReader reader = new InputStreamReader(responseBody);
            if (responseClass != null) {
                response = new Gson().fromJson(reader, responseClass);
            }
        }
        return response;
    }

    /**
     * @param http the HTTP connection object to test
     * @return whether the connection's last request was successful (status 2XX)
     * @throws IOException if the HTTP object has no response code
     */
    private static boolean requestSuccessful(HttpURLConnection http) throws IOException {
        return (http.getResponseCode() / 100) == 2;
    }
}
