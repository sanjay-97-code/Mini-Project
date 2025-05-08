package main.java.com.houserental.services;

import main.java.com.houserental.dao.UserDAO;
import main.java.com.houserental.models.User;

public class AuthService {
    private UserDAO userDAO;
    public AuthService() {
        this.userDAO = new UserDAO();
    }
    public boolean register(User user) {
        if (userDAO.usernameExists(user.getUsername())) {
            return false;
        }
        return userDAO.registerUser(user);
    }
    public User login(String username, String password) {
        return userDAO.loginUser(username, password);
    }
}