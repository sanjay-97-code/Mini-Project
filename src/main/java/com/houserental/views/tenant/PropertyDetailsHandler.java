package main.java.com.houserental.views.tenant;

import main.java.com.houserental.models.*;
import main.java.com.houserental.config.*;
import main.java.com.houserental.services.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PropertyDetailsHandler {
    private final BrowseProperties parent;
    private final User tenant;
    private final PropertyService propertyService;
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SUCCESS_COLOR = new Color(50, 205, 50);
    private static final Color WARNING_COLOR = new Color(255, 102, 102);

    public PropertyDetailsHandler(BrowseProperties parent, User tenant) {
        this.parent = parent;
        this.tenant = tenant;
        this.propertyService = new PropertyService();
    }

    public void showPropertyDetails(Property property) {
        JDialog detailsDialog = createDetailsDialog(property);
        JPanel mainPanel = createMainPanel(property, detailsDialog);
        detailsDialog.add(mainPanel);
        detailsDialog.setVisible(true);
    }

    private JDialog createDetailsDialog(Property property) {
        // Find the parent Window (e.g., JFrame) containing BrowseProperties
        Window parentWindow = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog;
        if (parentWindow != null) {
            dialog = new JDialog(parentWindow, property.getTitle(), Dialog.ModalityType.APPLICATION_MODAL);
        } else {
            dialog = new JDialog((Frame) null, property.getTitle(), true);
        }
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Maximize to fit screen with title bar
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setSize(screenSize.width, screenSize.height);
        dialog.setLocation(0, 0); // Top-left corner to cover screen

        // Add escape key to close dialog
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        return dialog;
    }

    private JPanel createMainPanel(Property property, JDialog dialog) {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));

        try {
            propertyService.checkFavoriteStatus(tenant, property);
        } catch (Exception e) {
            e.printStackTrace();
        }

        PropertyGalleryPanel galleryPanel = createGalleryPanel(property);
        JPanel infoPanel = createInfoPanel(property);
        JTextArea descriptionArea = createDescriptionArea(property);
        JPanel actionPanel = createActionPanel(dialog, property);

        JPanel contentPanel = new JPanel(new BorderLayout(30, 30));
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.add(galleryPanel, BorderLayout.CENTER);
        contentPanel.add(infoPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setBackground(new Color(245, 245, 245));
        bottomPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private PropertyGalleryPanel createGalleryPanel(Property property) {
        List<PropertyImage> images = fetchPropertyImages(property.getPropertyId());
        List<String> imagePaths = images.stream()
                .map(PropertyImage::getImagePath)
                .collect(Collectors.toList());
        return new PropertyGalleryPanel(imagePaths);
    }

    private List<PropertyImage> fetchPropertyImages(int propertyId) {
        List<PropertyImage> images = new ArrayList<>();
        String query = "SELECT image_id, property_id, image_path, is_primary " +
                "FROM property_images WHERE property_id = ? " +
                "ORDER BY is_primary DESC, image_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, propertyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PropertyImage image = new PropertyImage();
                image.setImageId(rs.getInt("image_id"));
                image.setPropertyId(rs.getInt("property_id"));
                image.setImagePath(rs.getString("image_path"));
                image.setPrimary(rs.getBoolean("is_primary"));
                images.add(image);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching images: " + e.getMessage());
            showError("Failed to load property images from database.");
        }

        return images;
    }

    private JPanel createInfoPanel(Property property) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(property.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        addDetailRow(panel, "Type:", property.getPropertyType());
        addDetailRow(panel, "Address:", property.getAddress());
        addDetailRow(panel, "City:", property.getCity());
        addDetailRow(panel, "Price:", String.format("₹%,.2f/month", property.getMonthlyRent()));
        addDetailRow(panel, "Bedrooms:", String.valueOf(property.getBedrooms()));
        addDetailRow(panel, "Bathrooms:", String.valueOf(property.getBathrooms()));
        addDetailRow(panel, "Area:", property.getAreaSqft() + " sqft");

        return panel;
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new BorderLayout(15, 0));
        rowPanel.setBackground(Color.WHITE);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelComp.setPreferredSize(new Dimension(120, 25));

        JLabel valueComp = new JLabel(value != null ? value : "N/A");
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        rowPanel.add(labelComp, BorderLayout.WEST);
        rowPanel.add(valueComp, BorderLayout.CENTER);
        rowPanel.add(Box.createHorizontalStrut(15), BorderLayout.EAST);

        panel.add(rowPanel);
        panel.add(Box.createVerticalStrut(8));
    }

    private JTextArea createDescriptionArea(Property property) {
        JTextArea area = new JTextArea(property.getDescription());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        area.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Description",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14)
        ));
        return area;
    }

    private JPanel createActionPanel(JDialog dialog, Property property) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(new Color(245, 245, 245));

        JButton favoriteButton = createFavoriteButton(property);
        JButton contactButton = createContactButton(property);
        JButton rentButton = createRentButton(dialog, property);

        panel.add(favoriteButton);
        panel.add(contactButton);
        panel.add(rentButton);

        return panel;
    }

    private JButton createFavoriteButton(Property property) {
        JButton button = new JButton();
        updateFavoriteButtonText(button, property.isFavorite());
        button.addActionListener(e -> toggleFavorite(property, button));
        styleButton(button, WARNING_COLOR);
        return button;
    }

    private JButton createContactButton(Property property) {
        JButton button = new JButton("Contact Owner");
        button.addActionListener(e -> showOwnerContactInfo(property.getOwnerId()));
        styleButton(button, PRIMARY_COLOR);
        return button;
    }

    private JButton createRentButton(JDialog dialog, Property property) {
        JButton button = new JButton("Rent This Property");

        // Disable rent button if current user is the owner
        if (tenant.getUserId() == property.getOwnerId()) {
            button.setEnabled(false);
            button.setToolTipText("You cannot rent your own property");
        }

        button.addActionListener(e -> {
            RentalRequest request = showRentalDialog();
            if (request != null) {
                try {
                    boolean success = propertyService.sendRentalRequest(
                            tenant.getUserId(),
                            property.getPropertyId(),
                            request.startDate(),
                            request.endDate(),
                            request.message());

                    if (success) {
                        Window parentWindow = SwingUtilities.getWindowAncestor(parent);
                        JDialog successDialog;

                        if (parentWindow != null) {
                            successDialog = new JDialog(parentWindow, "Success");
                            successDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                        } else {
                            successDialog = new JDialog();
                            successDialog.setTitle("Success");
                            successDialog.setModal(true);
                        }

                        successDialog.setSize(400, 200);
                        successDialog.setLocationRelativeTo(parentWindow);

                        JPanel panel = new JPanel(new BorderLayout());
                        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

                        // Plain text message with line breaks
                        JTextArea message = new JTextArea(
                                "Rental request submitted successfully!\n" +
                                        "The owner will review your request."
                        );
                        message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        message.setEditable(false);
                        message.setOpaque(false);
                        message.setLineWrap(true);
                        message.setWrapStyleWord(true);
                        message.setAlignmentX(Component.CENTER_ALIGNMENT);

                        JButton okButton = new JButton("OK");
                        okButton.addActionListener(ev -> successDialog.dispose());
                        okButton.setPreferredSize(new Dimension(100, 30));

                        panel.add(message, BorderLayout.CENTER);
                        panel.add(okButton, BorderLayout.SOUTH);

                        successDialog.add(panel);
                        successDialog.setVisible(true);

                        parent.loadProperties();
                        button.setEnabled(false); // Disable after successful submission
                    } else {
                        showError("This property is no longer available for rent.");
                    }
                } catch (SQLException ex) {
                    showError("Error processing rental: " + ex.getMessage());
                }
            }
        });

        styleButton(button, SUCCESS_COLOR);
        return button;
    }

    private void toggleFavorite(Property property, JButton button) {
        try {
            boolean success = propertyService.toggleFavorite(
                    tenant.getUserId(), property.getPropertyId());
            if (success) {
                property.setFavorite(!property.isFavorite());
                updateFavoriteButtonText(button, property.isFavorite());
            }
        } catch (Exception ex) {
            showError("Error updating favorites: " + ex.getMessage());
        }
    }

    private void updateFavoriteButtonText(JButton button, boolean isFavorite) {
        button.setText(isFavorite ? "❤ Remove from Favorites" : "♡ Add to Favorites");
    }

    private record RentalRequest(Date startDate, Date endDate, String message) {}

    private RentalRequest showRentalDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        JSpinner startSpinner = createDateSpinner(new Date());
        JSpinner endSpinner = createDateSpinner(getNextMonthDate());
        JTextArea messageArea = new JTextArea("I would like to rent this property");

        panel.add(createDatePanel("Start Date:", startSpinner));
        panel.add(createDatePanel("End Date:", endSpinner));
        panel.add(new JScrollPane(messageArea));

        Window parentWindow = SwingUtilities.getWindowAncestor(parent);
        int result = JOptionPane.showConfirmDialog(
                parentWindow, panel, "Confirm Rental Request",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return new RentalRequest(
                    (Date) startSpinner.getValue(),
                    (Date) endSpinner.getValue(),
                    messageArea.getText()
            );
        }
        return null;
    }

    private JPanel createDatePanel(String label, JSpinner spinner) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.add(new JLabel(label));
        panel.add(spinner);
        return panel;
    }

    private JSpinner createDateSpinner(Date initialDate) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setValue(initialDate);
        return spinner;
    }

    private Date getNextMonthDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        return cal.getTime();
    }

    private void showOwnerContactInfo(int ownerId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT full_name, email, phone FROM users WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, ownerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Window parentWindow = SwingUtilities.getWindowAncestor(parent);
                JDialog contactDialog;
                if (parentWindow != null) {
                    contactDialog = new JDialog(parentWindow, "Owner Contact", Dialog.ModalityType.APPLICATION_MODAL);
                } else {
                    contactDialog = new JDialog((Frame) null, "Owner Contact", true);
                }
                contactDialog.setSize(400, 250);
                contactDialog.setLocationRelativeTo(parentWindow != null ? parentWindow : null);

                JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
                panel.setBorder(new EmptyBorder(20, 20, 20, 20));

                JLabel title = new JLabel("Owner Contact Information", SwingConstants.CENTER);
                title.setFont(new Font("Segoe UI", Font.BOLD, 16));
                panel.add(title);

                panel.add(createContactRow("Name:", rs.getString("full_name")));
                panel.add(createContactRow("Email:", rs.getString("email")));
                panel.add(createContactRow("Phone:", rs.getString("phone")));

                contactDialog.add(panel);
                contactDialog.setVisible(true);
            } else {
                showError("Owner information not found");
            }
        } catch (SQLException e) {
            showError("Error retrieving owner information: " + e.getMessage());
        }
    }

    private JPanel createContactRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel valueLbl = new JLabel(value != null ? value : "N/A");
        row.add(labelLbl);
        row.add(valueLbl);
        return row;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}