package dataAccess.memoryDAO;

import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
        if (this.getEmail(user.username()) != null) {
            throw new DataAccessException("username already taken");
        }
        user = new UserData(user.username(),
                (new BCryptPasswordEncoder().encode(user.password())),
                user.email());
        this.userTable.add(user);
    }

    @Override
    public String getEmail(String username) throws DataAccessException {
        if (username == null) {
            throw new DataAccessException("null username");
        }
        for (UserData user : this.userTable) {
            if (user.username().equals(username)) {
                return user.email();
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
            if (user.username().equals(username)
                    && (new BCryptPasswordEncoder()).matches(password, user.password())) {
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
