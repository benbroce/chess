package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import model.UserData;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.response.LoginResponse;
import model.response.RegisterResponse;
import service.serviceExceptions.AlreadyTakenException;
import service.serviceExceptions.BadRequestException;
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
     * @throws BadRequestException   if the username or password are missing
     */
    public RegisterResponse register(RegisterRequest request) throws AlreadyTakenException, BadRequestException {
        if ((request.username() == null) || (request.password() == null)) {
            throw new BadRequestException("username or password missing");
        }
        // convert DataAccessException -> AlreadyTakenException
        try {
            userDAO.createUser(new UserData(request.username(), request.password(), request.email()));
        } catch (DataAccessException e) {
            throw new AlreadyTakenException("username already taken");
        }
        // convert DataAccessException -> BadRequestException
        try {
            return new RegisterResponse(request.username(), authDAO.createAuth(request.username()));
        } catch (DataAccessException e) {
            throw new BadRequestException("invalid username");
        }
    }

    /**
     * Return an authToken granting authentication privileges to the current session
     *
     * @param request the login credentials of the user
     * @return a response with an authToken to authenticate the current session
     * @throws BadRequestException   if the username invalid
     * @throws UnauthorizedException if the credentials are invalid
     */
    public LoginResponse login(LoginRequest request) throws UnauthorizedException, BadRequestException {
        if (!userDAO.verifyUser(request.username(), request.password())) {
            throw new UnauthorizedException("invalid login credentials");
        }
        // convert DataAccessException -> BadRequestException
        try {
            return new LoginResponse(request.username(), authDAO.createAuth(request.username()));
        } catch (DataAccessException e) {
            throw new BadRequestException("invalid username");
        }
    }

    /**
     * Remove authentication privileges from the current session's authToken
     *
     * @param authToken the authentication token for the current session
     * @throws UnauthorizedException if the authToken is invalid
     * @throws BadRequestException   if the authToken is null
     */
    public void logout(String authToken) throws UnauthorizedException, BadRequestException {
        try {
            if (!authDAO.verifyAuthToken(authToken)) {
                throw new UnauthorizedException("bad auth token");
            }
        } catch (DataAccessException e) {
            throw new BadRequestException("null auth token");
        }
        try {
            authDAO.deleteAuth(authToken);
        } catch (DataAccessException e) {
            throw new BadRequestException("null authToken");
        }
    }
}
