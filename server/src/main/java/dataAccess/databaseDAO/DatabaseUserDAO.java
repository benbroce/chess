package dataAccess.databaseDAO;

import dataAccess.DataAccessException;
import dataAccess.DatabaseManager;
import dataAccess.UserDAO;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
                user.username(),
                (new BCryptPasswordEncoder()).encode(user.password()),
                user.email());
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
        String hashedPassword;
        try {
            Object[] userLine = DatabaseManager.executeQuery(
                    (new String[]{"password"}),
                    "SELECT username, password, email FROM user WHERE username=?",
                    username).getFirst();
            hashedPassword = (String) userLine[0];
        } catch (NoSuchElementException e) {
            return false;
        }
        return (new BCryptPasswordEncoder()).matches(password, hashedPassword);
    }

    @Override
    public void clearUsers() throws DataAccessException {
        DatabaseManager.executeUpdate("TRUNCATE user");
    }
}
