package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import service.serviceExceptions.ServerErrorException;

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
    public void clearApp() throws ServerErrorException {
        try {
            authDAO.clearAuths();
            gameDAO.clearGames();
            userDAO.clearUsers();
        } catch (DataAccessException e) {
            throw new ServerErrorException("Data Access Error");
        }
    }
}
