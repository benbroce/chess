package dataAccess;

import model.UserData;

public interface UserDAO {
    /**
     * Create a new user based on the given data object
     *
     * @param user the data object to populate the user with
     * @throws DataAccessException if user null or username of user already taken
     */
    void createUser(UserData user) throws DataAccessException;

    /**
     * Get the user with the given username, or null if the user does not exist
     *
     * @param username the username to search for
     * @return the UserData object for the user
     * @throws DataAccessException if the username is null
     */
    UserData getUser(String username) throws DataAccessException;

    /**
     * @param username the username to search
     * @param password the password to verify
     * @return true if the given login info matches for a user in the database
     * @throws DataAccessException if username or password are null
     */
    boolean verifyUser(String username, String password) throws DataAccessException;

    /**
     * Delete all users
     */
    void clearUsers();
}