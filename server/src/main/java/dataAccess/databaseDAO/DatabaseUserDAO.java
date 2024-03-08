package dataAccess.databaseDAO;

import dataAccess.DataAccessException;
import dataAccess.DatabaseManager;
import dataAccess.UserDAO;
import model.UserData;

import java.util.NoSuchElementException;

public class DatabaseUserDAO implements UserDAO {
    public DatabaseUserDAO() throws DataAccessException {
        DatabaseManager.executeUpdate("""
                CREATE TABLE IF NOT EXISTS user (
                    `username` VARCHAR(255) NOT NULL,
                    `password` VARCHAR(255) NOT NULL,
                    `email` VARCHAR(255) NOT NULL,
                    PRIMARY KEY (`username`)
                )""");
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null) {
            throw new DataAccessException("null user");
        }
        if (this.getEmail(user.username()) != null) {
            throw new DataAccessException("username already taken");
        }
        DatabaseManager.executeUpdate(
                "INSERT INTO user (username, password, email) VALUES (?, ?, ?)",
                user.username(), user.password(), user.email());
    }

    @Override
    public String getEmail(String username) throws DataAccessException {
        if (username == null) {
            throw new DataAccessException("null username");
        }
        try {
            return (String) DatabaseManager.executeQuery(
                    (new String[]{"email"}),
                    "SELECT username, password, email FROM user WHERE username=?",
                    username).getFirst()[0];
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public boolean verifyUser(String username, String password) throws DataAccessException {
        if ((username == null) || (password == null)) {
            throw new DataAccessException("null credentials");
        }
        return !(DatabaseManager.executeQuery(
                (new String[]{"username"}),
                "SELECT username, password, email FROM user WHERE username=? AND password=?",
                username, password).isEmpty());
    }

    @Override
    public void clearUsers() throws DataAccessException {
        DatabaseManager.executeUpdate("TRUNCATE user");
    }
}
