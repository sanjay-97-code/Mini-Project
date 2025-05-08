package main.java.com.houserental.views.owner;

import main.java.com.houserental.models.Property;
import main.java.com.houserental.models.User;
import main.java.com.houserental.services.PropertyService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AddPropertyDialog extends JDialog {
    private PropertyService propertyService;
    private Property property;
    private User owner;

    // Form fields
    private JTextField titleField, addressField, cityField, stateField, pincodeField;
    private JTextArea descriptionArea;
    private JComboBox<String> typeCombo;
    private JSpinner bedroomsSpinner, bathroomsSpinner, areaSpinner;
    private JFormattedTextField rentField, depositField;
    private JButton addImageButton, saveButton, setPrimaryButton;
    private JLabel imageCountLabel;
    private List<byte[]> images = new ArrayList<>();
    private List<String> imagePaths = new ArrayList<>();
    private String primaryImagePath;

    public AddPropertyDialog(JFrame parent, User owner) {
        this(parent, owner, null);
    }

    public AddPropertyDialog(JFrame parent, User owner, Property property) {
        super(parent, property == null ? "Add New Property" : "Edit Property", true);
        this.owner = owner;
        this.property = property;
        this.propertyService = new PropertyService();
        setSize(900, 700);
        setLocationRelativeTo(parent);
        initializeUI();
        if (property != null) {
            populateFields();
        }
    }

    private void initializeUI() {
        setTitle(property == null ? "Add New Property" : "Edit Property");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 248, 255)); // Light blue background
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        createBasicInfoSection(formPanel, gbc);
        createLocationSection(formPanel, gbc);
        createDetailsSection(formPanel, gbc);
        createImageSection(formPanel, gbc);
        saveButton = createStyledButton(
                property == null ? "Add Property" : "Update Property",
                new Color(46, 204, 113));
        saveButton.addActionListener(e -> saveProperty());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createBasicInfoSection(JPanel formPanel, GridBagConstraints gbc) {
        Font sectionFont = new Font("Segoe UI", Font.BOLD, 16);
        JLabel sectionLabel = new JLabel("Basic Information");
        sectionLabel.setFont(sectionFont);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        gbc.gridwidth = 4;
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(sectionLabel, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;
        addFormField(formPanel, gbc, "Title:", titleField = createTextField(300, 30));
        gbc.gridy++;
        addFormField(formPanel, gbc, "Property Type:",
                typeCombo = new JComboBox<>(new String[]{"House", "Apartment", "Villa", "Condo", "Other"}));
        gbc.gridy++;
        addFormField(formPanel, gbc, "Description:",
                descriptionArea = createTextArea(3, 20));
        gbc.gridy++;
    }

    private void createLocationSection(JPanel formPanel, GridBagConstraints gbc) {
        Font sectionFont = new Font("Segoe UI", Font.BOLD, 16);
        JLabel sectionLabel = new JLabel("Location Details");
        sectionLabel.setFont(sectionFont);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        gbc.gridwidth = 4;
        gbc.gridx = 0;
        formPanel.add(sectionLabel, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;
        addFormField(formPanel, gbc, "Address:", addressField = createTextField(300, 30));
        gbc.gridy++;
        addFormField(formPanel, gbc, "City:", cityField = createTextField(300, 30));
        gbc.gridy++;
        addFormField(formPanel, gbc, "State:", stateField = createTextField(300, 30));
        gbc.gridy++;
        addFormField(formPanel, gbc, "Pincode:", pincodeField = createTextField(300, 30));
        gbc.gridy++;
    }

    private void createDetailsSection(JPanel formPanel, GridBagConstraints gbc) {
        Font sectionFont = new Font("Segoe UI", Font.BOLD, 16);
        JLabel sectionLabel = new JLabel("Property Details");
        sectionLabel.setFont(sectionFont);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        gbc.gridwidth = 4;
        gbc.gridx = 0;
        formPanel.add(sectionLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        addFormField(formPanel, gbc, "Bedrooms:",
                bedroomsSpinner = createSpinner(1, 1, 20, 1));
        gbc.gridy++;
        addFormField(formPanel, gbc, "Bathrooms:",
                bathroomsSpinner = createSpinner(1, 1, 10, 1));
        gbc.gridy++;
        addFormField(formPanel, gbc, "Area (sqft):",
                areaSpinner = createSpinner(1000, 100, 10000, 100));
        gbc.gridy++;
        Locale indiaLocale = new Locale("en", "IN");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(indiaLocale);
        addFormField(formPanel, gbc, "Monthly Rent:",
                rentField = createCurrencyField(currencyFormat, 1000.00));
        gbc.gridy++;
        addFormField(formPanel, gbc, "Security Deposit:",
                depositField = createCurrencyField(currencyFormat, 2000.00));
        gbc.gridy++;
    }

    private void createImageSection(JPanel formPanel, GridBagConstraints gbc) {
        Font sectionFont = new Font("Segoe UI", Font.BOLD, 16);
        JLabel sectionLabel = new JLabel("Images");
        sectionLabel.setFont(sectionFont);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        gbc.gridwidth = 4;
        gbc.gridx = 0;
        formPanel.add(sectionLabel, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        imagePanel.setBackground(Color.WHITE);
        addImageButton = createStyledButton("Add Images", new Color(52, 152, 219));
        addImageButton.addActionListener(e -> addImages());
        setPrimaryButton = createStyledButton("Set Primary", new Color(155, 89, 182));
        setPrimaryButton.setEnabled(false);
        setPrimaryButton.addActionListener(e -> setPrimaryImage());
        imageCountLabel = new JLabel("0 images selected");
        imageCountLabel.setForeground(new Color(100, 100, 100));
        imagePanel.add(addImageButton);
        imagePanel.add(setPrimaryButton);
        imagePanel.add(imageCountLabel);
        gbc.gridwidth = 4;
        formPanel.add(imagePanel, gbc);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.weightx = 0.2;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(jLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        gbc.gridwidth = 3;
        panel.add(field, gbc);
        gbc.gridwidth = 1;
    }

    private JTextField createTextField(int width, int height) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(width, height));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    private JTextArea createTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(300, 80));
        return area;
    }

    private JSpinner createSpinner(int value, int min, int max, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        spinner.setBackground(Color.WHITE);
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return spinner;
    }

    private JFormattedTextField createCurrencyField(NumberFormat format, double initialValue) {
        JFormattedTextField field = new JFormattedTextField(format);
        field.setValue(initialValue);
        field.setPreferredSize(new Dimension(150, 30));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
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
                    byte[] imageData = Files.readAllBytes(file.toPath());
                    images.add(imageData);
                    imagePaths.add(file.getAbsolutePath());

                    if (primaryImagePath == null) {
                        primaryImagePath = file.getAbsolutePath();
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

            // Populate property fields
            newProperty.setOwnerId(owner.getUserId());
            newProperty.setTitle(titleField.getText());
            newProperty.setPropertyType(((String) typeCombo.getSelectedItem()).toLowerCase());
            newProperty.setDescription(descriptionArea.getText());
            newProperty.setAddress(addressField.getText());
            newProperty.setCity(cityField.getText());
            newProperty.setState(stateField.getText());
            newProperty.setPincode(pincodeField.getText());
            newProperty.setBedrooms((Integer) bedroomsSpinner.getValue());
            newProperty.setBathrooms((Integer) bathroomsSpinner.getValue());
            newProperty.setAreaSqft((Integer) areaSpinner.getValue());
            newProperty.setMonthlyRent(((Number) rentField.getValue()).doubleValue());
            newProperty.setSecurityDeposit(((Number) depositField.getValue()).doubleValue());
            newProperty.setStatus("available");

            boolean success;
            if (property == null) {
                // Adding a new property
                newProperty.setImagePaths(imagePaths);
                newProperty.setPrimaryImagePath(primaryImagePath);
                success = propertyService.addProperty(newProperty);
            } else {
                // Updating an existing property
                success = propertyService.updateProperty(newProperty);
                if (success) {
                    // Update images separately
                    success = propertyService.updatePropertyImages(
                            newProperty.getPropertyId(),
                            imagePaths,
                            primaryImagePath
                    );
                }
            }

            if (success) {
                JOptionPane.showMessageDialog(this,
                        property == null ? "Property added successfully!" : "Property updated successfully!");
                dispose();
            } else {
                showError("Operation failed. Please try again.");
            }
        } catch (Exception e) {
            showError("Invalid input. Please check all fields.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}