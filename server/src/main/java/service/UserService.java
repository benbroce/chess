package service;

import dataAccess.AuthDAO;
import dataAccess.UserDAO;

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
     * @param username the username of the proposed user
     * @param password the password of the proposed user
     * @param email    the email address of the proposed user
     * @return an authToken to authenticate the current session
     * @throws
     */
    public String register(String username, String password, String email) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @param username the username of the user
     * @param password the password of the user
     * @return an authToken to authenticate the current session
     * @throws
     */
    public String login(String username, String password) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @param authToken the authentication token for the current session
     * @throws
     */
    public void logout(String authToken) {
        throw new RuntimeException("Not implemented");
    }
}
