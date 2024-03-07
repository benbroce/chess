package dataAccess.memoryDAO;

import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import model.UserData;

import java.util.HashSet;

public class MemoryUserDAO implements UserDAO {
    private final HashSet<UserData> userTable;

    public MemoryUserDAO() {
        this.userTable = new HashSet<>();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null) {
            throw new DataAccessException("null user");
        }
        if (this.getUser(user.username()) != null) {
            throw new DataAccessException("username already taken");
        }
        this.userTable.add(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username == null) {
            throw new DataAccessException("null username");
        }
        for (UserData user : this.userTable) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean verifyUser(String username, String password) throws DataAccessException {
        if ((username == null) || (password == null)) {
            throw new DataAccessException("null credentials");
        }
        for (UserData user : this.userTable) {
            if (user.username().equals(username) && user.password().equals(password)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearUsers() {
        this.userTable.clear();
    }
}
