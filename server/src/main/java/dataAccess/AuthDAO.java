package dataAccess;

public interface AuthDAO {
    /**
     * Create a new authentication for the given user
     *
     * @param username the username of the user requesting authentication
     * @return authToken for the user's session
     */
    String createAuth(String username);

    /**
     * Remove authentication from the user associated with the given authToken
     * (no action if the user does not exist)
     *
     * @param authToken the authToken to remove authentication from
     */
    void deleteAuth(String authToken);

    /**
     * @param authToken the authToken to check
     * @return true if the authToken is associated with an authorized user
     */
    boolean verifyAuthToken(String authToken);

    /**
     * @param authToken the authToken to lookup
     * @return the username associated with the authToken, or null if the authToken is invalid
     */
    String getUsername(String authToken);

    /**
     * Delete all authentications
     */
    void clearAuths();
}
