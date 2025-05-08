package main.java.com.houserental;

import main.java.com.houserental.controllers.AuthController;
import main.java.com.houserental.views.auth.LoginFrame;
import main.java.com.houserental.views.auth.RegisterFrame;

public class Main {
    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        LoginFrame loginFrame = new LoginFrame();
        RegisterFrame registerFrame = new RegisterFrame();
        new AuthController(loginFrame, registerFrame);
        loginFrame.setVisible(true);
    }
}
