package dataAccess.databaseDAO;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.DatabaseManager;

import java.util.NoSuchElementException;
import java.util.UUID;

public class DatabaseAuthDAO implements AuthDAO {
    public DatabaseAuthDAO() throws DataAccessException {
        DatabaseManager.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auth (
                    `authToken` VARCHAR(255) NOT NULL,
                    `username` VARCHAR(255) NOT NULL,
                    PRIMARY KEY (`authToken`)
                )""");
    }

    @Override
    public String createAuth(String username) throws DataAccessException {
        if ((username == null) || username.isEmpty()) {
            throw new DataAccessException("invalid username");
        }
        String authToken = UUID.randomUUID().toString();
        DatabaseManager.executeUpdate(
                "INSERT INTO auth (authToken, username) VALUES (?, ?)",
                authToken, username);
        return authToken;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("null authToken");
        }
        DatabaseManager.executeUpdate(
                "DELETE FROM auth WHERE authToken=?",
                authToken);
    }

    @Override
    public boolean verifyAuthToken(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("null authToken");
        }
        return !(DatabaseManager.executeQuery(
                (new String[]{"authToken", "username"}),
                "SELECT authToken, username FROM auth WHERE authToken=?",
                authToken).isEmpty());
    }

    @Override
    public String getUsername(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("null authToken");
        }
        try {
            return (String) (DatabaseManager.executeQuery(
                    (new String[]{"username"}),
                    "SELECT authToken, username FROM auth WHERE authToken=?",
                    authToken).getFirst()[0]);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public void clearAuths() throws DataAccessException {
        DatabaseManager.executeUpdate("TRUNCATE auth");
    }
}
