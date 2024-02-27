package serviceTests;

import dataAccess.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;
    private AuthDAO authDAO;
    private UserDAO userDAO;

    @BeforeEach
    public void setup() {
        authDAO = new MemoryAuthDAO();
        userDAO = new MemoryUserDAO();
        userService = new UserService(authDAO, userDAO);
    }

    @Test
    public void registerTestPositive() {
    }

    @Test
    public void registerTestNegative() {
    }

    @Test
    public void loginTestPositive() {
    }

    @Test
    public void loginTestNegative() {
    }

    @Test
    public void logoutTestPositive() {
    }

    @Test
    public void logoutTestNegative() {
    }
}