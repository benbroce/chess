package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import model.UserData;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.result.LoginResponse;
import model.result.RegisterResponse;
import service.serviceExceptions.AlreadyTakenException;
import service.serviceExceptions.UnauthorizedException;

public class UserService {
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    /**
     * User Service which takes DAOs and uses them to fulfill requests
     */
    public UserService(AuthDAO authDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    /**
     * Create a new user in the user database and login
     *
     * @param request the account data of the proposed user
     * @return a response with an authToken to authenticate the current session
     * @throws AlreadyTakenException if the username is already taken
     */
    public RegisterResponse register(RegisterRequest request) throws AlreadyTakenException {
        // convert any DataAccessException -> AlreadyTakenException
        try {
            userDAO.createUser(new UserData(request.username(), request.password(), request.email()));
        } catch (DataAccessException e) {
            throw new AlreadyTakenException("username already taken");
        }
        return new RegisterResponse(request.username(), authDAO.createAuth(request.username()));
    }

    /**
     * Return an authToken granting authentication privileges to the current session
     *
     * @param request the login credentials of the user
     * @return a response with an authToken to authenticate the current session
     * @throws UnauthorizedException if the credentials are invalid
     */
    public LoginResponse login(LoginRequest request) throws UnauthorizedException {
        if (!userDAO.verifyUser(request.username(), request.password())) {
            throw new UnauthorizedException("invalid login credentials");
        }
        return new LoginResponse(request.username(), authDAO.createAuth(request.username()));
    }

    /**
     * Remove authentication privileges from the current session's authToken
     *
     * @param authToken the authentication token for the current session
     * @throws UnauthorizedException if the authToken is invalid
     */
    public void logout(String authToken) throws UnauthorizedException {
        if (!authDAO.verifyAuthToken(authToken)) {
            throw new UnauthorizedException("bad auth token");
        }
        authDAO.deleteAuth(authToken);
    }
}
