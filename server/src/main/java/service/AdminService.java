package service;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;

public class AdminService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;

    /**
     * Administration Service which takes DAOs and uses them to fulfill requests
     */
    public AdminService(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    /**
     * Clear the entire chess app database
     */
    public void clearApp() {
        authDAO.clearAuths();
        gameDAO.clearGames();
        userDAO.clearUsers();
    }
}
