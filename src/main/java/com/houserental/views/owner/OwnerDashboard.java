package main.java.com.houserental.views.owner;

import main.java.com.houserental.models.User;
import main.java.com.houserental.models.Property;
import main.java.com.houserental.services.PropertyService;
import main.java.com.houserental.views.components.PropertyCardRenderer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class OwnerDashboard extends JFrame {
    private User owner;
    private PropertyService propertyService;
    private DefaultListModel<Property> propertyListModel;
    private JTabbedPane tabbedPane;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public OwnerDashboard(User owner) {
        this.owner = owner;
        this.propertyService = new PropertyService();
        initializeUI();
        loadProperties();
    }

    private void initializeUI() {
        setTitle("Owner Dashboard - " + owner.getFullName());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel with improved gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(52, 143, 226);
                Color color2 = new Color(86, 101, 115);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header panel with back button
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Initialize tabbed pane with improved rendering
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setOpaque(false);

        // Create and add tabs
        tabbedPane.addTab("My Properties", createPropertiesTab());
        tabbedPane.addTab("Rental Requests", new RentalRequestView(owner));
        tabbedPane.addTab("Add Property", new AddPropertyView(owner));

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        // Left panel with back button and user info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        // Back button
        JButton backBtn = createStyledButton("← Back", new Color(52, 152, 219));
        backBtn.setPreferredSize(new Dimension(100, 35));
        backBtn.addActionListener(e -> {
            dispose();
            new main.java.com.houserental.views.auth.RoleSelectionFrame(owner).setVisible(true);
        });
        leftPanel.add(backBtn);

        // User avatar and name
        JLabel avatarLabel = new JLabel(createRoundIcon(new Color(255, 255, 255, 150), 50));
        JLabel userLabel = new JLabel(owner.getFullName());
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        userLabel.setForeground(Color.WHITE);
        leftPanel.add(avatarLabel);
        leftPanel.add(userLabel);

        // Welcome label
        JLabel welcomeLabel = new JLabel("Owner Dashboard", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);

        // Logout button
        JButton logoutBtn = createStyledButton("Logout", new Color(231, 76, 60));
        logoutBtn.addActionListener(e -> logout());

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createPropertiesTab() {
        JPanel propertiesTab = new JPanel(new BorderLayout());
        propertiesTab.setOpaque(false);
        propertiesTab.setBorder(new EmptyBorder(10, 10, 10, 10));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        // Property list panel
        JPanel listPanel = createPropertyListPanel();
        cardPanel.add(listPanel, "LIST_VIEW");
        propertiesTab.add(cardPanel, BorderLayout.CENTER);

        return propertiesTab;
    }

    private JPanel createPropertyListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setOpaque(false);

        // Initialize property list with improved rendering
        propertyListModel = new DefaultListModel<>();
        JList<Property> propertyList = new JList<>(propertyListModel);
        propertyList.setCellRenderer(new PropertyCardRenderer());
        propertyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propertyList.setFixedCellHeight(220);
        propertyList.setBackground(new Color(255, 255, 255, 80));
        propertyList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        propertyList.addMouseListener(new PropertyListMouseListener());

        JScrollPane scrollPane = new JScrollPane(propertyList);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Center panel for proper scrolling
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        centerPanel.add(scrollPane, gbc);

        listPanel.add(centerPanel, BorderLayout.CENTER);
        listPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        return listPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        buttonPanel.setOpaque(false);

        JButton refreshBtn = createStyledButton("Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> refreshProperties());

        buttonPanel.add(refreshBtn);
        return buttonPanel;
    }

    private class PropertyListMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int index = ((JList<?>) e.getSource()).locationToIndex(e.getPoint());
                if (index >= 0) {
                    Property selected = propertyListModel.getElementAt(index);
                    showPropertyDetails(selected);
                }
            }
        }
    }

    private void loadProperties() {
        SwingWorker<List<Property>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Property> doInBackground() throws Exception {
                return propertyService.getOwnerProperties(owner.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<Property> properties = get();
                    propertyListModel.clear();

                    if (properties.isEmpty()) {
                        Property emptyProperty = new Property();
                        emptyProperty.setTitle("No properties found");
                        emptyProperty.setPropertyType("Click 'Add New Property' to get started");
                        propertyListModel.addElement(emptyProperty);
                    } else {
                        properties.forEach(propertyListModel::addElement);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(OwnerDashboard.this,
                            "Error loading properties: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void refreshProperties() {
        propertyListModel.clear();
        loadProperties();
    }

    private void showPropertyDetails(Property property) {
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Back button
        JButton backButton = createStyledButton("← Back to List", new Color(52, 152, 219));
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "LIST_VIEW"));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(backButton, BorderLayout.WEST);
        detailsPanel.add(headerPanel, BorderLayout.NORTH);

        // Content panel with improved layout
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Image panel with better sizing
        contentPanel.add(createImagePanel(property), BorderLayout.WEST);

        // Info panel with improved text contrast
        contentPanel.add(createInfoPanel(property), BorderLayout.CENTER);

        // Action buttons
        contentPanel.add(createActionButtons(property), BorderLayout.SOUTH);

        detailsPanel.add(contentPanel, BorderLayout.CENTER);

        // Update card panel
        if (cardPanel.getComponentCount() > 1) {
            cardPanel.remove(1);
        }
        cardPanel.add(detailsPanel, "DETAILS_VIEW");
        cardLayout.show(cardPanel, "DETAILS_VIEW");
    }

    private JPanel createImagePanel(Property property) {
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setOpaque(false);
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        loadPrimaryImage(property, imageLabel);

        JPanel imageControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        imageControls.setOpaque(false);

        JButton deleteImageBtn = createStyledButton("Delete Image", new Color(231, 76, 60));
        deleteImageBtn.setPreferredSize(new Dimension(150, 35));
        deleteImageBtn.addActionListener(e -> deletePropertyImage(property, imageLabel));

        imageControls.add(deleteImageBtn);
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        imagePanel.add(imageControls, BorderLayout.SOUTH);

        return imagePanel;
    }

    private JPanel createInfoPanel(Property property) {
        // Main content panel with vertical layout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 20, 20, 0)); // Added bottom padding

        // Color scheme
        Color titleColor = new Color(30, 30, 30); // White
        Color labelColor = new Color(21, 37, 179);
        Color valueColor = new Color(122, 33, 33);
        Color availableColor = new Color(66, 250, 66); // Light green
        Color notAvailableColor = new Color(248, 106, 106); // Light red

        // Fonts
        Font titleFont = new Font("Segoe UI", Font.BOLD, 24);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font statusFont = new Font("Segoe UI", Font.BOLD, 16);

        // Title
        JLabel titleLabel = new JLabel(property.getTitle());
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(titleColor);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Property details
        addDetailRow(contentPanel, "Type:", property.getPropertyType(), labelFont, labelColor, valueFont, valueColor);
        addDetailRow(contentPanel, "Address:", property.getAddress(), labelFont, labelColor, valueFont, valueColor);
        addDetailRow(contentPanel, "City:", property.getCity(), labelFont, labelColor, valueFont, valueColor);
        addDetailRow(contentPanel, "State:", property.getState(), labelFont, labelColor, valueFont, valueColor);
        addDetailRow(contentPanel, "Pincode:", property.getPincode(), labelFont, labelColor, valueFont, valueColor);
        addDetailRow(contentPanel, "Bedrooms:", String.valueOf(property.getBedrooms()), labelFont, labelColor, valueFont, valueColor);
        addDetailRow(contentPanel, "Bathrooms:", String.valueOf(property.getBathrooms()), labelFont, labelColor, valueFont, valueColor);
        addDetailRow(contentPanel, "Area:", property.getAreaSqft() + " sqft", labelFont, labelColor, valueFont, valueColor);
        addDetailRow(contentPanel, "Monthly Rent:", "₹" + property.getMonthlyRent(), labelFont, labelColor, valueFont, valueColor);
        addDetailRow(contentPanel, "Security Deposit:", "₹" + property.getSecurityDeposit(), labelFont, labelColor, valueFont, valueColor);

        // Status
        contentPanel.add(createStatusPanel(property, statusFont,
                "available".equalsIgnoreCase(property.getStatus()) ? availableColor : notAvailableColor));
        contentPanel.add(Box.createVerticalStrut(30));

        // Description
        contentPanel.add(createDescriptionPanel(property, valueFont, valueColor));

        // Scroll pane wrapping the content panel
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Scrollbar styling
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBackground(new Color(240, 240, 240));
        verticalScrollBar.setForeground(new Color(100, 100, 100));

        // Container panel
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(scrollPane, BorderLayout.CENTER);
        containerPanel.setOpaque(false);

        return containerPanel;
    }


    // Updated helper methods to include color parameters
    private void addDetailRow(JPanel panel, String label, String value,
                              Font labelFont, Color labelColor,
                              Font valueFont, Color valueColor) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setOpaque(false);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(labelFont);
        labelComp.setForeground(labelColor);
        labelComp.setPreferredSize(new Dimension(150, 20));

        JLabel valueComp = new JLabel(value != null ? value : "N/A");
        valueComp.setFont(valueFont);
        valueComp.setForeground(valueColor);

        rowPanel.add(labelComp, BorderLayout.WEST);
        rowPanel.add(valueComp, BorderLayout.CENTER);
        panel.add(rowPanel);
        panel.add(Box.createVerticalStrut(5));
    }

    private JPanel createStatusPanel(Property property, Font font, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        JLabel statusLabel = new JLabel("Status: ");
        statusLabel.setFont(font);
        statusLabel.setForeground(new Color(29, 47, 135)); // Use label color

        JLabel statusValue = new JLabel(property.getStatus());
        statusValue.setFont(font);
        statusValue.setForeground(color);

        panel.add(statusLabel);
        panel.add(statusValue);
        return panel;
    }

    private JPanel createDescriptionPanel(Property property, Font font, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(font);
        descLabel.setForeground(color);

        JTextArea descArea = new JTextArea(property.getDescription());
        descArea.setFont(font);
        descArea.setForeground(color);
        descArea.setOpaque(false);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(new EmptyBorder(5, 0, 0, 0));

        panel.add(descLabel, BorderLayout.NORTH);
        panel.add(descArea, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionButtons(Property property) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton editButton = createStyledButton("Edit Property", new Color(52, 152, 219));
        editButton.setPreferredSize(new Dimension(180, 40));
        editButton.addActionListener(e -> {
            AddPropertyDialog editDialog = new AddPropertyDialog(OwnerDashboard.this, owner, property);
            editDialog.setVisible(true);
            cardLayout.show(cardPanel, "LIST_VIEW");
            refreshProperties();
        });

        JButton deleteButton = createStyledButton("Delete Property", new Color(231, 76, 60));
        deleteButton.setPreferredSize(new Dimension(180, 40));
        deleteButton.addActionListener(e -> deleteProperty(property));

        actionPanel.add(editButton);
        actionPanel.add(deleteButton);

        return actionPanel;
    }

    private void deletePropertyImage(Property property, JLabel imageLabel) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this image?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            property.setPrimaryImagePath(null);
            if (propertyService.updateProperty(property)) {
                loadPrimaryImage(property, imageLabel);
                cardLayout.show(cardPanel, "LIST_VIEW");
                refreshProperties();
                JOptionPane.showMessageDialog(this, "Image deleted successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete image", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteProperty(Property property) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete '" + property.getTitle() + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return propertyService.deleteProperty(property.getPropertyId(), owner.getUserId());
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(
                                    OwnerDashboard.this,
                                    "Property deleted successfully",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            cardLayout.show(cardPanel, "LIST_VIEW");
                            refreshProperties();
                        } else {
                            JOptionPane.showMessageDialog(
                                    OwnerDashboard.this,
                                    "Failed to delete property",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                OwnerDashboard.this,
                                "Error: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        }
    }

    private void loadPrimaryImage(Property property, JLabel imageLabel) {
        try {
            if (property.getPrimaryImagePath() != null && !property.getPrimaryImagePath().isEmpty()) {
                ImageIcon icon = new ImageIcon(property.getPrimaryImagePath());
                if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    // Maintain aspect ratio while scaling
                    int width = 400;
                    int height = 300;
                    Image img = icon.getImage();

                    // Calculate scaled dimensions maintaining aspect ratio
                    double aspectRatio = (double) img.getWidth(null) / img.getHeight(null);
                    if (width / aspectRatio > height) {
                        width = (int) (height * aspectRatio);
                    } else {
                        height = (int) (width / aspectRatio);
                    }

                    img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(img));
                    return;
                }
            }
            // Create better placeholder
            imageLabel.setIcon(new ImageIcon(createPlaceholderImage(400, 300)));
        } catch (Exception e) {
            imageLabel.setIcon(new ImageIcon(createPlaceholderImage(400, 300)));
        }
    }

    private void addDetailRow(JPanel panel, String label, String value, Font labelFont, Font valueFont) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowPanel.setOpaque(false);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelFont);
        labelComponent.setForeground(new Color(220, 220, 220));
        labelComponent.setPreferredSize(new Dimension(150, labelComponent.getPreferredSize().height));

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(valueFont);
        valueComponent.setForeground(Color.WHITE);

        rowPanel.add(labelComponent);
        rowPanel.add(valueComponent);
        panel.add(rowPanel);
        panel.add(Box.createVerticalStrut(10));
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 45));
        button.setBorder(new EmptyBorder(10, 25, 10, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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

    private ImageIcon createRoundIcon(Color color, int diameter) {
        BufferedImage image = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillOval(0, 0, diameter, diameter);
        g2d.dispose();
        return new ImageIcon(image);
    }

    private BufferedImage createPlaceholderImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        g2d.setColor(new Color(70, 70, 70, 150));
        g2d.fillRect(0, 0, width, height);

        // Draw border
        g2d.setColor(new Color(200, 200, 200, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(5, 5, width-10, height-10);

        // Draw icon
        g2d.setColor(new Color(220, 220, 220));
        g2d.setFont(new Font("Arial", Font.PLAIN, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "🏠";
        int x = (width - fm.stringWidth(text)) / 2;
        int y = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, x, y);

        // Draw text
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        String message = "No Image Available";
        x = (width - g2d.getFontMetrics().stringWidth(message)) / 2;
        y += 40;
        g2d.drawString(message, x, y);

        g2d.dispose();
        return image;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new main.java.com.houserental.Main();
        }
    }
}