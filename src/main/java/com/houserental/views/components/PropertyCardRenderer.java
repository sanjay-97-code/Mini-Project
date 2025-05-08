package main.java.com.houserental.views.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import main.java.com.houserental.models.Property;

public class PropertyCardRenderer extends JPanel implements ListCellRenderer<Property> {
    private JLabel titleLabel, priceLabel, typeLabel, statusLabel, imageLabel;

    public PropertyCardRenderer() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(15, 15, 15, 15)
        ));
        setBackground(new Color(255, 255, 255, 220));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(180, 150));
        imageLabel.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);

        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setOpaque(false);
        imagePanel.add(imageLabel);
        add(imagePanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 30, 30));

        typeLabel = new JLabel();
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeLabel.setForeground(new Color(50, 50, 50));

        priceLabel = new JLabel();
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        priceLabel.setForeground(new Color(39, 174, 96));

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(80, 80, 80));

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(typeLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(statusLabel);
        add(infoPanel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Property> list,
                                                  Property property, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        // Set property details with null checks
        titleLabel.setText(property.getTitle() != null ? property.getTitle() : "Unknown Property");
        typeLabel.setText(property.getPropertyType() != null ?
                property.getPropertyType() + " • " +
                        property.getBedrooms() + " beds • " + property.getBathrooms() + " baths • " +
                        property.getAreaSqft() + " sqft" : "N/A");
        priceLabel.setText("₹" + property.getMonthlyRent() + "/month");

        String status = property.getStatus() != null ? property.getStatus() : "unknown";
        statusLabel.setText("Status: " + status);

        // Set status color
        switch (status.toLowerCase()) {
            case "available":
                statusLabel.setForeground(new Color(39, 174, 96));
                break;
            case "rented":
                statusLabel.setForeground(new Color(192, 57, 43));
                break;
            case "maintenance":
                statusLabel.setForeground(new Color(243, 156, 18));
                break;
            default:
                statusLabel.setForeground(new Color(80, 80, 80));
        }

        // Selection styling
        if (isSelected) {
            setBackground(new Color(230, 240, 255));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(100, 150, 255)),
                    new EmptyBorder(15, 15, 15, 15)
            ));
        } else {
            setBackground(new Color(255, 255, 255, 220));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(15, 15, 15, 15)
            ));
        }

        // Load image using Property.getImages()
        try {
            java.util.List<ImageIcon> images = property.getImages();
            ImageIcon icon = images != null && !images.isEmpty() ? images.get(0) : null;
            if (icon != null && icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(180, 150, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(img));
                System.out.println("Loaded image for property: " + property.getTitle());
            } else {
                System.err.println("No valid image found for property: " + property.getTitle() + ", using placeholder");
                imageLabel.setIcon(property.getImages().get(0)); // Use placeholder from getImages()
            }
        } catch (Exception e) {
            System.err.println("Error loading image for property: " + property.getTitle() + " - " + e.getMessage());
            imageLabel.setIcon(property.getImages().get(0)); // Use placeholder from getImages()
        }

        return this;
    }
}