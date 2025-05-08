package main.java.com.houserental.views.tenant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PropertyGalleryPanel extends JPanel {
    private List<String> imagePaths;
    private int currentImageIndex = 0;
    private JLabel imageLabel;
    private JButton prevButton, nextButton;
    private JPanel thumbnailsPanel;
    private double scaleFactor = 1.0;

    public PropertyGalleryPanel(List<String> imagePaths) {
        this.imagePaths = imagePaths != null ? imagePaths : List.of();
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        // Main image display
        imageLabel = new JLabel("", JLabel.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.WHITE);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(imageLabel, BorderLayout.CENTER);

        // Navigation controls
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        navPanel.setBackground(new Color(240, 240, 240));

        // Compact arrow buttons
        prevButton = createNavButton("◄");
        nextButton = createNavButton("►");
        prevButton.addActionListener(e -> showPreviousImage());
        nextButton.addActionListener(e -> showNextImage());

        JPanel arrowPanel = new JPanel(new BorderLayout());
        arrowPanel.add(prevButton, BorderLayout.WEST);
        arrowPanel.add(nextButton, BorderLayout.EAST);
        arrowPanel.setOpaque(false);

        // Thumbnails
        thumbnailsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        thumbnailsPanel.setOpaque(false);

        navPanel.add(arrowPanel, BorderLayout.NORTH);
        navPanel.add(thumbnailsPanel, BorderLayout.CENTER);
        add(navPanel, BorderLayout.SOUTH);

        updateDisplay();
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(40, 30));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void updateDisplay() {
        if (imagePaths.isEmpty()) {
            imageLabel.setIcon(null);
            imageLabel.setText("No images available");
            imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            return;
        }

        String currentPath = imagePaths.get(currentImageIndex);
        try {
            ImageIcon icon = new ImageIcon(currentPath);
            Image image = icon.getImage();

            // Calculate available space
            Dimension containerSize = getSize();
            if (containerSize.width == 0 || containerSize.height == 0) {
                containerSize = getPreferredSize();
            }

            int maxWidth = containerSize.width - 40; // Account for padding
            int maxHeight = (int)(containerSize.height * 0.8); // 80% of height for image

            // Scale to fit while maintaining aspect ratio
            Image scaledImage = scaleImageToFit(image, maxWidth, maxHeight);
            imageLabel.setIcon(new ImageIcon(scaledImage));

            updateThumbnails();
        } catch (Exception e) {
            imageLabel.setIcon(null);
            imageLabel.setText("Error loading image");
            imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        }
    }

    private Image scaleImageToFit(Image image, int maxWidth, int maxHeight) {
        int origWidth = image.getWidth(null);
        int origHeight = image.getHeight(null);

        // Calculate scaling ratio while maintaining aspect ratio
        double widthRatio = (double)maxWidth / origWidth;
        double heightRatio = (double)maxHeight / origHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int)(origWidth * ratio);
        int newHeight = (int)(origHeight * ratio);

        return image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }

    private void updateThumbnails() {
        thumbnailsPanel.removeAll();

        for (int i = 0; i < imagePaths.size(); i++) {
            String path = imagePaths.get(i);
            try {
                ImageIcon thumbIcon = new ImageIcon(path);
                Image scaledThumb = thumbIcon.getImage().getScaledInstance(50, 35, Image.SCALE_SMOOTH);
                JButton thumbBtn = new JButton(new ImageIcon(scaledThumb));
                thumbBtn.setPreferredSize(new Dimension(50, 35));
                thumbBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                i == currentImageIndex ? Color.BLUE : Color.LIGHT_GRAY,
                                i == currentImageIndex ? 2 : 1
                        ),
                        BorderFactory.createEmptyBorder(2, 2, 2, 2)
                ));
                thumbBtn.setContentAreaFilled(false);

                int index = i;
                thumbBtn.addActionListener(e -> {
                    currentImageIndex = index;
                    updateDisplay();
                });

                thumbnailsPanel.add(thumbBtn);
            } catch (Exception e) {
                JButton thumbBtn = new JButton(String.valueOf(i+1));
                thumbBtn.setPreferredSize(new Dimension(50, 35));
                thumbnailsPanel.add(thumbBtn);
            }
        }

        prevButton.setEnabled(currentImageIndex > 0);
        nextButton.setEnabled(currentImageIndex < imagePaths.size() - 1);

        revalidate();
        repaint();
    }

    private void showNextImage() {
        if (currentImageIndex < imagePaths.size() - 1) {
            currentImageIndex++;
            updateDisplay();
        }
    }

    private void showPreviousImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            updateDisplay();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600); // Default size
    }

    public void setImagePaths(List<String> newImagePaths) {
        this.imagePaths = newImagePaths != null ? newImagePaths : List.of();
        this.currentImageIndex = 0;
        updateDisplay();
    }
}