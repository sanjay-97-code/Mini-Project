package main.java.com.houserental.views.owner;

import main.java.com.houserental.models.Property;
import main.java.com.houserental.models.User;
import main.java.com.houserental.services.PropertyService;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AddPropertyView extends JPanel {
    private PropertyService propertyService;
    private User owner;
    private Property property;

    // Form fields
    private JTextField titleField, addressField, cityField, stateField, pincodeField;
    private JTextArea descriptionArea;
    private JComboBox<String> typeCombo;
    private JSpinner bedroomsSpinner, bathroomsSpinner, areaSpinner;
    private JFormattedTextField rentField, depositField;
    private JButton addImageButton, saveButton, setPrimaryButton;
    private JLabel imageCountLabel;

    // Image handling
    private List<String> imagePaths = new ArrayList<>();
    private String primaryImagePath;

    public AddPropertyView(User owner) {
        this(owner, null);
    }

    public AddPropertyView(User owner, Property property) {
        this.owner = owner;
        this.property = property;
        this.propertyService = new PropertyService();
        initializeUI();
        if (property != null) {
            populateFields();
        }
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // Create a main panel with proper padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Form panel that will be scrollable
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Add section panels to the form
        formPanel.add(createBasicInfoSection());
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(createLocationSection());
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(createPropertyDetailsSection());
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(createImagesSection());

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Button panel at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        saveButton = createStyledButton(
                property == null ? "Add Property" : "Update Property",
                new Color(46, 204, 113)
        );
        saveButton.addActionListener(e -> saveProperty());
        buttonPanel.add(saveButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createBasicInfoSection() {
        JPanel panel = createSectionPanel("Basic Information");

        titleField = createTextField();
        typeCombo = createTypeComboBox();
        descriptionArea = createTextArea();

        panel.add(createFormRow("Title:", titleField));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createFormRow("Property Type:", typeCombo));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createFormRow("Description:", new JScrollPane(descriptionArea)));

        return panel;
    }

    private JPanel createLocationSection() {
        JPanel panel = createSectionPanel("Location Details");

        addressField = createTextField();
        cityField = createTextField();
        stateField = createTextField();
        pincodeField = createTextField();

        panel.add(createFormRow("Address:", addressField));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createFormRow("City:", cityField));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createFormRow("State:", stateField));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createFormRow("Pincode:", pincodeField));

        return panel;
    }

    private JPanel createPropertyDetailsSection() {
        JPanel panel = createSectionPanel("Property Details");

        bedroomsSpinner = createSpinner(1, 1, 20, 1);
        bathroomsSpinner = createSpinner(1, 1, 10, 1);
        areaSpinner = createSpinner(1000, 100, 10000, 100);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        rentField = createCurrencyField(currencyFormat, 10000);
        depositField = createCurrencyField(currencyFormat, 20000);

        panel.add(createFormRow("Bedrooms:", bedroomsSpinner));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createFormRow("Bathrooms:", bathroomsSpinner));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createFormRow("Area (sqft):", areaSpinner));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createFormRow("Monthly Rent:", rentField));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createFormRow("Security Deposit:", depositField));

        return panel;
    }

    private JPanel createImagesSection() {
        JPanel panel = createSectionPanel("Images");

        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        imagePanel.setBackground(Color.WHITE);

        addImageButton = createStyledButton("Add Images", new Color(52, 152, 219));
        addImageButton.addActionListener(e -> addImages());

        setPrimaryButton = createStyledButton("Set Primary", new Color(155, 89, 182));
        setPrimaryButton.setEnabled(false);
        setPrimaryButton.addActionListener(e -> setPrimaryImage());

        imageCountLabel = new JLabel("0 images selected");
        imageCountLabel.setForeground(Color.GRAY);
        imageCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        imagePanel.add(addImageButton);
        imagePanel.add(setPrimaryButton);
        imagePanel.add(imageCountLabel);

        panel.add(imagePanel);

        return panel;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);

        return panel;
    }

    private JPanel createFormRow(String label, JComponent field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jLabel.setPreferredSize(new Dimension(150, 30));
        jLabel.setMinimumSize(new Dimension(150, 30));
        jLabel.setMaximumSize(new Dimension(150, 30));

        field.setMaximumSize(new Dimension(400, 30));
        if (field instanceof JScrollPane) {
            field.setPreferredSize(new Dimension(400, 80));
            field.setMaximumSize(new Dimension(400, 80));
        }

        row.add(jLabel);
        row.add(Box.createRigidArea(new Dimension(20, 0)));
        row.add(field);

        return row;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(400, 30));
        return field;
    }

    private JComboBox<String> createTypeComboBox() {
        JComboBox<String> combo = new JComboBox<>(new String[]{"House", "Apartment", "Villa", "Condo", "Other"});
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setMaximumSize(new Dimension(400, 30));
        return combo;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea(3, 20);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return area;
    }

    private JSpinner createSpinner(int value, int min, int max, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinner.setMaximumSize(new Dimension(400, 30));
        return spinner;
    }

    private JFormattedTextField createCurrencyField(NumberFormat format, double value) {
        JFormattedTextField field = new JFormattedTextField(format);
        field.setValue(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(400, 30));
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 40));
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

    private void populateFields() {
        titleField.setText(property.getTitle());
        typeCombo.setSelectedItem(property.getPropertyType());
        descriptionArea.setText(property.getDescription());
        addressField.setText(property.getAddress());
        cityField.setText(property.getCity());
        stateField.setText(property.getState());
        pincodeField.setText(property.getPincode());
        bedroomsSpinner.setValue(property.getBedrooms());
        bathroomsSpinner.setValue(property.getBathrooms());
        areaSpinner.setValue(property.getAreaSqft());
        rentField.setValue(property.getMonthlyRent());
        depositField.setValue(property.getSecurityDeposit());

        if (property.getImagePaths() != null) {
            imagePaths = new ArrayList<>(property.getImagePaths());
            primaryImagePath = property.getPrimaryImagePath();
            updateImageStatus();
        }
    }

    private void addImages() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            for (File file : fileChooser.getSelectedFiles()) {
                try {
                    String imagePath = file.getAbsolutePath();
                    imagePaths.add(imagePath);

                    if (primaryImagePath == null) {
                        primaryImagePath = imagePath;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error reading file: " + file.getName(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            updateImageStatus();
        }
    }

    private void setPrimaryImage() {
        if (!imagePaths.isEmpty()) {
            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "Select primary image:",
                    "Set Primary Image",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    imagePaths.toArray(),
                    primaryImagePath);

            if (selected != null) {
                primaryImagePath = selected;
            }
        }
    }

    private void updateImageStatus() {
        imageCountLabel.setText(imagePaths.size() + " images selected");
        setPrimaryButton.setEnabled(!imagePaths.isEmpty());
    }

    private void saveProperty() {
        try {
            Property newProperty = property != null ? property : new Property();

            // Set property details
            newProperty.setOwnerId(owner.getUserId());
            newProperty.setTitle(titleField.getText());
            newProperty.setPropertyType((String) typeCombo.getSelectedItem());
            newProperty.setDescription(descriptionArea.getText());
            newProperty.setAddress(addressField.getText());
            newProperty.setCity(cityField.getText());
            newProperty.setState(stateField.getText());
            newProperty.setPincode(pincodeField.getText());
            newProperty.setBedrooms((Integer) bedroomsSpinner.getValue());
            newProperty.setBathrooms((Integer) bathroomsSpinner.getValue());
            newProperty.setAreaSqft((Integer) areaSpinner.getValue());
            newProperty.setMonthlyRent(Double.parseDouble(rentField.getText().replaceAll("[^\\d.]", "")));
            newProperty.setSecurityDeposit(Double.parseDouble(depositField.getText().replaceAll("[^\\d.]", "")));
            newProperty.setStatus("available");
            newProperty.setImagePaths(imagePaths);
            newProperty.setPrimaryImagePath(primaryImagePath);

            boolean success = property == null ?
                    propertyService.addProperty(newProperty) :
                    propertyService.updateProperty(newProperty);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        property == null ? "Property added successfully!" : "Property updated successfully!");
                // Close the dialog or refresh parent view as needed
            } else {
                JOptionPane.showMessageDialog(this,
                        "Operation failed. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid input. Please check all fields.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}