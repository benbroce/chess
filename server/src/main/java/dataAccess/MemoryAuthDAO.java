package dataAccess;

import model.AuthData;

import java.util.HashSet;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private final HashSet<AuthData> authTable;

    public MemoryAuthDAO() {
        this.authTable = new HashSet<>();
    }

    @Override
    public String createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        this.authTable.add(new AuthData(authToken, username));
        return authToken;
    }

    @Override
    public void deleteAuth(String authToken) {
        this.authTable.removeIf(auth -> auth.authToken().equals(authToken));
    }

    @Override
    public boolean verifyAuthToken(String authToken) {
        for (AuthData auth : this.authTable) {
            if (auth.authToken().equals(authToken)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearAuths() {
        this.authTable.clear();
    }
}
