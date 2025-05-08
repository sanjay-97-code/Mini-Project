package main.java.com.houserental.views.shared;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.awt.image.BufferedImage;
import main.java.com.houserental.models.User;

public class ProfileFrame extends JFrame {
    private User user;
    private JLabel nameLabel, emailLabel, phoneLabel, addressLabel, usernameLabel;
    private JTextField nameField, emailField, phoneField, addressField;
    private JPanel namePanel, emailPanel, phonePanel, addressPanel;
    private JButton editButton, saveButton, closeButton;
    private boolean isEditing = false;

    public ProfileFrame(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("User Profile - " + (user.getFullName() != null ? user.getFullName() : "User"));
        setSize(500, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));
        add(mainPanel);

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        detailsPanel.add(createDetailRow("Username:", user.getUsername(), false));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        namePanel = createDetailRow("Full Name:", user.getFullName(), true);
        detailsPanel.add(namePanel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        emailPanel = createDetailRow("Email:", user.getEmail(), true);
        detailsPanel.add(emailPanel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        phonePanel = createDetailRow("Phone:", user.getPhone(), true);
        detailsPanel.add(phonePanel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        addressPanel = createDetailRow("Address:", user.getAddress(), true);
        detailsPanel.add(addressPanel);

        mainPanel.add(detailsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        buttonPanel.setOpaque(false);

        editButton = createStyledButton("Edit", new Color(70, 130, 180));
        editButton.addActionListener(e -> toggleEditMode());

        saveButton = createStyledButton("Save", new Color(76, 175, 80));
        saveButton.setVisible(false);
        saveButton.addActionListener(e -> saveChanges());

        closeButton = createStyledButton("Close", new Color(244, 67, 54));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(editButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel avatarLabel = new JLabel();
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/icons/profile.png"));
            Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            avatarLabel.setIcon(createRoundIcon(new Color(70, 130, 180), 100));
        }

        JLabel name = new JLabel(user.getFullName() != null ? user.getFullName() : "Unknown User");
        name.setFont(new Font("Segoe UI", Font.BOLD, 22));
        name.setForeground(Color.DARK_GRAY);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setBorder(new EmptyBorder(10, 0, 0, 0));

        headerPanel.add(avatarLabel);
        headerPanel.add(name);

        return headerPanel;
    }

    private JPanel createDetailRow(String label, String value, boolean editable) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setOpaque(false);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelComponent.setForeground(new Color(70, 70, 70));
        labelComponent.setPreferredSize(new Dimension(100, 30));

        JLabel valueLabel = new JLabel(value != null ? value : "Not provided");
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueLabel.setForeground(Color.BLACK);

        if (editable) {
            JTextField textField = new JTextField(value != null ? value : "");
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            textField.setVisible(false);

            JPanel container = new JPanel(new CardLayout());
            container.setOpaque(false);
            container.add(valueLabel, "label");
            container.add(textField, "field");

            switch (label) {
                case "Full Name:":
                    nameLabel = valueLabel;
                    nameField = textField;
                    break;
                case "Email:":
                    emailLabel = valueLabel;
                    emailField = textField;
                    break;
                case "Phone:":
                    phoneLabel = valueLabel;
                    phoneField = textField;
                    break;
                case "Address:":
                    addressLabel = valueLabel;
                    addressField = textField;
                    break;
            }

            rowPanel.add(labelComponent, BorderLayout.WEST);
            rowPanel.add(container, BorderLayout.CENTER);
        } else {
            usernameLabel = valueLabel;
            rowPanel.add(labelComponent, BorderLayout.WEST);
            rowPanel.add(valueLabel, BorderLayout.CENTER);
        }

        return rowPanel;
    }

    private void toggleEditMode() {
        isEditing = !isEditing;

        nameLabel.setVisible(!isEditing);
        nameField.setVisible(isEditing);
        emailLabel.setVisible(!isEditing);
        emailField.setVisible(isEditing);
        phoneLabel.setVisible(!isEditing);
        phoneField.setVisible(isEditing);
        addressLabel.setVisible(!isEditing);
        addressField.setVisible(isEditing);

        editButton.setText(isEditing ? "Cancel" : "Edit");
        saveButton.setVisible(isEditing);

        if (!isEditing) {
            nameField.setText(user.getFullName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
            addressField.setText(user.getAddress());
        }

        revalidate();
        repaint();
    }

    private void saveChanges() {
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPhone = phoneField.getText().trim();
        String newAddress = addressField.getText().trim();

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newEmail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        user.setFullName(newName);
        user.setEmail(newEmail);
        user.setPhone(newPhone.isEmpty() ? null : newPhone);
        user.setAddress(newAddress.isEmpty() ? null : newAddress);

        nameLabel.setText(newName);
        emailLabel.setText(newEmail);
        phoneLabel.setText(newPhone.isEmpty() ? "Not provided" : newPhone);
        addressLabel.setText(newAddress.isEmpty() ? "Not provided" : newAddress);

        setTitle("User Profile - " + newName);
        toggleEditMode();

        JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private ImageIcon createRoundIcon(Color color, int diameter) {
        BufferedImage image = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillOval(0, 0, diameter, diameter);
        g2d.dispose();
        return new ImageIcon(image);
    }
}
