package main.java.com.houserental.views.tenant;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import main.java.com.houserental.dao.PropertyDAO;
import main.java.com.houserental.models.Property;
import main.java.com.houserental.models.User;
import main.java.com.houserental.views.components.PropertyCardRenderer;

public class MyRentals extends JPanel {
    private User tenant;
    private PropertyDAO propertyDAO;
    private DefaultListModel<Property> rentalListModel;

    public MyRentals(User tenant) {
        this.tenant = tenant;
        this.propertyDAO = new PropertyDAO();
        initializeUI();
        loadApprovedRentals();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("My Rentals");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Rental list
        rentalListModel = new DefaultListModel<>();
        JList<Property> rentalList = new JList<>(rentalListModel);
        rentalList.setCellRenderer(new PropertyCardRenderer());
        rentalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rentalList.setFixedCellHeight(220);
        rentalList.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(rentalList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);
    }

    // In MyRentals.java
    private void loadApprovedRentals() {
        SwingWorker<List<Property>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Property> doInBackground() throws Exception {
                return propertyDAO.getApprovedRentalsForTenant(tenant.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<Property> rentals = get();
                    rentalListModel.clear();

                    if (rentals.isEmpty()) {
                        Property emptyProperty = new Property();
                        emptyProperty.setTitle("No Approved Rentals");
                        emptyProperty.setPropertyType("You don't have any approved rentals yet");
                        emptyProperty.setDescription("Your approved rentals will appear here once the owner accepts your request");
                        rentalListModel.addElement(emptyProperty);
                    } else {
                        rentals.forEach(rental -> {
                            rental.setStatus("RENTED"); // Set to RENTED since that's the property status
                            rentalListModel.addElement(rental);
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyRentals.this,
                            "Error loading rentals: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}