package main.java.com.houserental.views.tenant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import main.java.com.houserental.dao.FavoritesDAO;
import main.java.com.houserental.dao.PropertyDAO;
import main.java.com.houserental.models.Property;
import main.java.com.houserental.models.User;
import main.java.com.houserental.views.components.PropertyCardRenderer;
import main.java.com.houserental.views.tenant.PropertyDetailsHandler;

public class BrowseProperties extends JPanel {
    private User tenant;
    private PropertyDAO propertyDAO;
    private DefaultListModel<Property> propertyListModel;
    private JList<Property> propertyList;
    private JComboBox<String> cityFilter;
    private List<String> availableCities;
    private PropertyDetailsHandler detailsHandler;

    public BrowseProperties(User tenant) {
        this.tenant = tenant;
        this.propertyDAO = new PropertyDAO();
        this.detailsHandler = new PropertyDetailsHandler(this, tenant);
        initializeUI();
        loadCities();
        loadProperties();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        // Filter panel
        JPanel filterPanel = createFilterPanel();

        // Property list
        propertyListModel = new DefaultListModel<>();
        propertyList = new JList<>(propertyListModel);
        propertyList.setCellRenderer(new PropertyCardRenderer()); // Use custom renderer

        propertyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propertyList.setFixedCellHeight(220);
        propertyList.setBackground(Color.WHITE);

        propertyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = propertyList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Property selected = propertyListModel.getElementAt(index);
                        detailsHandler.showPropertyDetails(selected);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(propertyList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        cityFilter = new JComboBox<>();
        cityFilter.addItem("All Cities");

        JComboBox<String> typeFilter = new JComboBox<>(new String[]{"All Types", "Apartment", "House", "Condo"});
        JComboBox<String> priceFilter = new JComboBox<>(new String[]{"Any Price", "₹0-₹5000", "₹5001-₹10000", "₹10001+"});
        JComboBox<String> bedroomFilter = new JComboBox<>(new String[]{"Any", "1", "2", "3", "4+"});

        JButton filterButton = new JButton("Apply Filters");
        filterButton.addActionListener(e -> applyFilters(
                cityFilter.getSelectedItem().toString(),
                typeFilter.getSelectedItem().toString(),
                priceFilter.getSelectedItem().toString(),
                bedroomFilter.getSelectedItem().toString()
        ));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadCities();
            loadProperties();
        });

        filterPanel.add(new JLabel("City:"));
        filterPanel.add(cityFilter);
        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(typeFilter);
        filterPanel.add(new JLabel("Price:"));
        filterPanel.add(priceFilter);
        filterPanel.add(new JLabel("Bedrooms:"));
        filterPanel.add(bedroomFilter);
        filterPanel.add(filterButton);
        filterPanel.add(refreshButton);

        return filterPanel;
    }

    private void loadCities() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return propertyDAO.getAvailableCities();
            }

            @Override
            protected void done() {
                try {
                    availableCities = get();
                    cityFilter.removeAllItems();
                    cityFilter.addItem("All Cities");
                    for (String city : availableCities) {
                        cityFilter.addItem(city);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BrowseProperties.this,
                            "Error loading cities: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    void loadProperties() {
        SwingWorker<List<Property>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Property> doInBackground() throws Exception {
                return propertyDAO.getAvailableProperties(null, null, null, null, null, 0, 100);
            }

            @Override
            protected void done() {
                try {
                    List<Property> properties = get();
                    propertyListModel.clear();

                    if (properties.isEmpty()) {
                        Property emptyProperty = new Property();
                        emptyProperty.setTitle("No properties available");
                        emptyProperty.setPropertyType("Check back later for new listings");
                        propertyListModel.addElement(emptyProperty);
                    } else {
                        properties.forEach(propertyListModel::addElement);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BrowseProperties.this,
                            "Error loading properties: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void applyFilters(String city, String type, String price, String bedrooms) {
        final String cityParam = city.equals("All Cities") ? null : city;
        final String typeParam = type.equals("All Types") ? null : type;

        final Double finalMinPrice;
        final Double finalMaxPrice;

        if (!price.equals("Any Price")) {
            String[] priceParts = price.replaceAll("[^0-9]", " ").trim().split("\\s+");
            finalMinPrice = Double.parseDouble(priceParts[0]);
            finalMaxPrice = priceParts.length > 1 ? Double.parseDouble(priceParts[1]) : null;
        } else {
            finalMinPrice = null;
            finalMaxPrice = null;
        }

        final Integer minBedroomsParam = bedrooms.equals("Any") ? null :
                Integer.parseInt(bedrooms.replace("+", ""));

        SwingWorker<List<Property>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Property> doInBackground() throws Exception {
                return propertyDAO.getAvailableProperties(
                        cityParam,
                        typeParam,
                        finalMinPrice,
                        finalMaxPrice,
                        minBedroomsParam,
                        0,
                        100
                );
            }

            @Override
            protected void done() {
                try {
                    List<Property> properties = get();
                    propertyListModel.clear();
                    properties.forEach(propertyListModel::addElement);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BrowseProperties.this,
                            "Error applying filters: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}