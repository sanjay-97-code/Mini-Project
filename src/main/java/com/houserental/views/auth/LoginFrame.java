package main.java.com.houserental.views.auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private JButton togglePasswordButton;
    private boolean passwordVisible = false;

    public LoginFrame() {
        setTitle("House Rental System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
       // setUndecorated(true);
        initializeUI();
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintBackground(g);
            }
        };

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JPanel loginPanel = createLoginForm();
        centerPanel.add(loginPanel, new GridBagConstraints());

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void paintBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Color color1 = new Color(52, 143, 226);
        Color color2 = new Color(86, 101, 115);
        GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private JPanel createLoginForm() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(255, 255, 255, 100), 1),
                new EmptyBorder(40, 40, 40, 40)
        ));
        loginPanel.setBackground(new Color(255, 255, 255, 220));
        loginPanel.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("HOUSE RENTAL SYSTEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        loginPanel.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Login to your account", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        loginPanel.add(subtitleLabel, gbc);

        // Username Field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 5, 5, 5);
        loginPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        styleTextField(usernameField);
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        loginPanel.add(usernameField, gbc);

        // Password Field with toggle
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 5, 5, 5);
        loginPanel.add(passwordLabel, gbc);

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(Color.WHITE);
        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        // Toggle password visibility button
        togglePasswordButton = new JButton();
        togglePasswordButton.setPreferredSize(new Dimension(40, passwordField.getPreferredSize().height));
        togglePasswordButton.setBorder(BorderFactory.createEmptyBorder());
        togglePasswordButton.setContentAreaFilled(false);
        togglePasswordButton.setFocusPainted(false);
        updateEyeIcon();
        togglePasswordButton.addActionListener(e -> togglePasswordVisibility());
        passwordPanel.add(togglePasswordButton, BorderLayout.EAST);

        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        loginPanel.add(passwordPanel, gbc);

        // Login Button
        loginButton = new JButton("LOGIN");
        styleButton(loginButton, new Color(46, 134, 222));
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(20, 0, 0, 5);
        loginPanel.add(loginButton, gbc);

        // Register Button
        registerButton = new JButton("REGISTER");
        styleButton(registerButton, new Color(100, 100, 100));
        gbc.gridx = 1;
        gbc.insets = new Insets(20, 5, 0, 0);
        loginPanel.add(registerButton, gbc);

        return loginPanel;
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        passwordField.setEchoChar(passwordVisible ? (char) 0 : '•');
        updateEyeIcon();
    }

    private void updateEyeIcon() {
        String icon = passwordVisible ? "👁️" : "🔒";
        togglePasswordButton.setText(icon);
        togglePasswordButton.setIcon(null);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(Color.WHITE);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 40));
        button.setBorder(new EmptyBorder(10, 25, 10, 25));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

    // Getters for controller to access
    public String getUsername() { return usernameField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }

    // Button getters for adding action listeners
    public JButton getLoginButton() { return loginButton; }
    public JButton getRegisterButton() { return registerButton; }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }
}