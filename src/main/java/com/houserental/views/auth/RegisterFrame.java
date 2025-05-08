package main.java.com.houserental.views.auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.*;

public class RegisterFrame extends JFrame {
    private JTextField usernameField, emailField, phoneField, fullNameField, addressField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton submitButton, backButton;

    public RegisterFrame() {
        setTitle("House Rental System - Sign Up");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        initializeUI();
    }

    private void initializeUI() {
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(52, 143, 226);
                Color color2 = new Color(86, 101, 115);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Form panel with white translucent background
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(255, 255, 255, 100), 1),
                new EmptyBorder(40, 60, 40, 60)
        ));
        formPanel.setBackground(new Color(255, 255, 255, 220));
        formPanel.setOpaque(true);

        // Title
        JLabel titleLabel = new JLabel("CREATE ACCOUNT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(titleLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Form fields
        addFormField(formPanel, "Full Name:", fullNameField = new JTextField(20));
        addFormField(formPanel, "Username:", usernameField = new JTextField(20));
        addFormField(formPanel, "Email:", emailField = new JTextField(20));
        addFormField(formPanel, "Phone:", phoneField = new JTextField(15));
        addFormField(formPanel, "Address:", addressField = new JTextField(20));
        addPasswordField(formPanel, "Password:", passwordField = new JPasswordField(20));
        addPasswordField(formPanel, "Confirm Password:", confirmPasswordField = new JPasswordField(20));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        submitButton = createStyledButton("REGISTER", new Color(46, 134, 222));
        backButton = createStyledButton("BACK TO LOGIN", new Color(100, 100, 100));

        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(buttonPanel);

        // Add form to center of main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void addFormField(JPanel panel, String label, JTextField field) {
        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
        fieldPanel.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(44, 62, 80));

        styleTextField(field);

        fieldPanel.add(lbl, BorderLayout.WEST);
        fieldPanel.add(field, BorderLayout.CENTER);

        panel.add(fieldPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    private void addPasswordField(JPanel panel, String label, JPasswordField field) {
        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
        fieldPanel.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(44, 62, 80));

        styleTextField(field);

        fieldPanel.add(lbl, BorderLayout.WEST);
        fieldPanel.add(field, BorderLayout.CENTER);

        panel.add(fieldPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(Color.WHITE);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
    }

    // Getters for controller access
    public String getUsername() { return usernameField.getText().trim(); }
    public String getEmail() { return emailField.getText().trim(); }
    public String getPhone() { return phoneField.getText().trim(); }
    public String getFullName() { return fullNameField.getText().trim(); }
    public String getAddress() { return addressField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getConfirmPassword() { return new String(confirmPasswordField.getPassword()); }

    public JButton getSubmitButton() { return submitButton; }
    public JButton getBackButton() { return backButton; }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Registration Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void clearFields() {
        usernameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        fullNameField.setText("");
        addressField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
    }
}