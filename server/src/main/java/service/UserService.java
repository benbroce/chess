package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import model.UserData;
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
     * @param username the username of the proposed user
     * @param password the password of the proposed user
     * @param email    the email address of the proposed user
     * @return an authToken to authenticate the current session
     * @throws AlreadyTakenException if the username is already taken
     */
    public String register(String username, String password, String email) throws AlreadyTakenException {
        // convert any DataAccessException -> AlreadyTakenException
        try {
            userDAO.createUser(new UserData(username, password, email));
        } catch (DataAccessException e) {
            throw new AlreadyTakenException("username already taken");
        }
        return authDAO.createAuth(username);
    }

    /**
     * Return an authToken granting authentication privileges to the current session
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return an authToken to authenticate the current session
     * @throws UnauthorizedException if the credentials are invalid
     */
    public String login(String username, String password) throws UnauthorizedException {
        if (!userDAO.verifyUser(username, password)) {
            throw new UnauthorizedException("invalid login credentials");
        }
        return authDAO.createAuth(username);
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
