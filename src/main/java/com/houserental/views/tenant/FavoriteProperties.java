package main.java.com.houserental.views.tenant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

import main.java.com.houserental.dao.PropertyDAO;
import main.java.com.houserental.models.Property;
import main.java.com.houserental.models.User;
import main.java.com.houserental.views.components.PropertyCardRenderer;

public class FavoriteProperties extends JPanel {
    private final User tenant;
    private final PropertyDAO propertyDAO;
    private final DefaultListModel<Property> favoritesListModel;
    private final JList<Property> favoritesList;

    public FavoriteProperties(User tenant) {
        this.tenant = tenant;
        this.propertyDAO = new PropertyDAO();
        this.favoritesListModel = new DefaultListModel<>();

        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(46, 204, 113));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("Favorite Properties");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton removeButton = new JButton("Remove from Favorites");
        removeButton.setBackground(new Color(231, 76, 60));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(52, 152, 219));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);

        buttonPanel.add(removeButton);
        buttonPanel.add(refreshButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Favorites list
        favoritesList = new JList<>(favoritesListModel);
        favoritesList.setCellRenderer(new PropertyCardRenderer());
        favoritesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        favoritesList.setFixedCellHeight(220);
        favoritesList.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(favoritesList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        removeButton.addActionListener(this::handleRemoveFavorite);
        refreshButton.addActionListener(e -> loadFavoriteProperties());

        // Initial load
        loadFavoriteProperties();
    }

    private void handleRemoveFavorite(ActionEvent e) {
        Property selected = favoritesList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(FavoriteProperties.this,
                    "Please select a property to remove.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to remove this property from favorites?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean removed = propertyDAO.removeFromFavorites(tenant.getUserId(), selected.getPropertyId());
                if (removed) {
                    favoritesListModel.removeElement(selected);
                    JOptionPane.showMessageDialog(null,
                            "Property removed from favorites.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                } else {
                    JOptionPane.showMessageDialog(null,
                            "Failed to remove property from favorites.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                        "Error removing favorite: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void loadFavoriteProperties() {
        SwingWorker<List<Property>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Property> doInBackground() {
                return propertyDAO.getFavoriteProperties(tenant.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<Property> favorites = get();
                    favoritesListModel.clear();

                    if (favorites.isEmpty()) {
                        Property emptyProperty = new Property();
                        emptyProperty.setTitle("No Favorites Yet");
                        emptyProperty.setPropertyType("You have not favorited any properties.");
                        emptyProperty.setDescription("Click the heart icon on properties to favorite them.");
                        favoritesListModel.addElement(emptyProperty);
                    } else {
                        favorites.forEach(favoritesListModel::addElement);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(FavoriteProperties.this,
                            "Error loading favorites: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}