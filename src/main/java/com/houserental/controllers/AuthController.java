package main.java.com.houserental.controllers;

import main.java.com.houserental.services.AuthService;
import main.java.com.houserental.models.User;
import main.java.com.houserental.views.auth.LoginFrame;
import main.java.com.houserental.views.auth.RegisterFrame;
import main.java.com.houserental.views.auth.RoleSelectionFrame;

public class AuthController {
    private AuthService authService;
    private LoginFrame loginFrame;
    private RegisterFrame registerFrame;

    public AuthController(LoginFrame loginFrame, RegisterFrame registerFrame) {
        this.authService = new AuthService();
        this.loginFrame = loginFrame;
        this.registerFrame = registerFrame;
        attachEventListeners();
    }

    private void attachEventListeners() {
        loginFrame.getLoginButton().addActionListener(e -> handleLogin());
        loginFrame.getRegisterButton().addActionListener(e -> {
            loginFrame.setVisible(false);
            registerFrame.setVisible(true);
        });

        registerFrame.getSubmitButton().addActionListener(e -> handleRegistration());

        registerFrame.getBackButton().addActionListener(e -> {
            registerFrame.setVisible(false);
            loginFrame.setVisible(true);
        });
    }

    private void handleLogin() {
        String username = loginFrame.getUsername();
        String password = loginFrame.getPassword();

        User user = authService.login(username, password);
        if (user != null) {
            loginFrame.dispose();
            new RoleSelectionFrame(user).setVisible(true);
        } else {
            loginFrame.showError("Invalid username or password");
        }
    }

    private void handleRegistration() {
        User user = new User(
                registerFrame.getUsername(),
                registerFrame.getPassword(),
                registerFrame.getEmail(),
                registerFrame.getPhone(),
                registerFrame.getFullName(),
                null // Address not collected during registration
        );

        if (!registerFrame.getPassword().equals(registerFrame.getConfirmPassword())) {
            registerFrame.showError("Passwords do not match");
            return;
        }
        if (authService.register(user)) {
            registerFrame.showSuccess("Registration successful!");
            registerFrame.clearFields();
            registerFrame.setVisible(false);
            loginFrame.setVisible(true);
        } else {
            registerFrame.showError("Username already exists or registration failed");
        }
    }
}