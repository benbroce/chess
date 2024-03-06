package dataAccess;

public interface AuthDAO {
    /**
     * Create a new authentication for the given user
     *
     * @param username the username of the user requesting authentication
     * @return authToken for the user's session
     * @throws DataAccessException if username is null or empty string
     */
    String createAuth(String username) throws DataAccessException;

    /**
     * Remove authentication from the user associated with the given authToken
     * (no action if the user does not exist)
     *
     * @param authToken the authToken to remove authentication from
     * @throws DataAccessException if the authToken is null
     */
    void deleteAuth(String authToken) throws DataAccessException;

    /**
     * @param authToken the authToken to check
     * @return true if the authToken is associated with an authorized user
     * @throws DataAccessException if the authToken is null
     */
    boolean verifyAuthToken(String authToken) throws DataAccessException;

    /**
     * @param authToken the authToken to lookup
     * @return the username associated with the authToken, or null if the authToken is invalid
     * @throws DataAccessException if the authToken is null
     */
    String getUsername(String authToken) throws DataAccessException;

    /**
     * Delete all authentications
     */
    void clearAuths();
}
