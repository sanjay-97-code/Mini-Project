package main.java.com.houserental.models;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Property {
    private int propertyId;
    private int ownerId;
    private String title;
    private String description;
    private String propertyType;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private int bedrooms;
    private int bathrooms;
    private int areaSqft;
    private double monthlyRent;
    private double securityDeposit;
    private String status = "available";
    private List<String> imagePaths; // Changed from List<byte[]>
    private String primaryImagePath;
    private boolean isFavorite;

    public Property() {}

    public int getPropertyId() {
        return propertyId;
    }
    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }
    public int getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getPropertyType() {
        return propertyType;
    }
    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getPincode() {
        return pincode;
    }
    public void setPincode(String pincode) {
        this.pincode = pincode;
    }
    public int getBedrooms() {
        return bedrooms;
    }
    public void setBedrooms(int bedrooms) {
        this.bedrooms = bedrooms;
    }
    public int getBathrooms() {
        return bathrooms;
    }
    public void setBathrooms(int bathrooms) {
        this.bathrooms = bathrooms;
    }
    public int getAreaSqft() {
        return areaSqft;
    }
    public void setAreaSqft(int areaSqft) {
        this.areaSqft = areaSqft;
    }
    public double getMonthlyRent() {
        return monthlyRent;
    }
    public void setMonthlyRent(double monthlyRent) {
        this.monthlyRent = monthlyRent;
    }
    public double getSecurityDeposit() {
        return securityDeposit;
    }
    public void setSecurityDeposit(double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public List<String> getImagePaths() {
        return imagePaths;
    }
    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }
    public String getPrimaryImagePath() {
        return primaryImagePath;
    }
    public void setPrimaryImagePath(String primaryImagePath) {
        this.primaryImagePath = primaryImagePath;
    }
    public boolean isFavorite() {
        return isFavorite;
    }
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    public void setPinCode(String pinCode) {
        this.pincode = pinCode;
    }
    public void setType(String type) {
        this.propertyType = type;
    }
    public void setPrice(double price) {
        this.monthlyRent = price;
    }
    public List<ImageIcon> getImages() {
        List<ImageIcon> images = new ArrayList<>();
        if (this.imagePaths != null) {
            for (String path : this.imagePaths) {
                try {
                    ImageIcon icon = new ImageIcon(path);
                    images.add(icon);
                } catch (Exception e) {
                    System.err.println("Error loading image: " + path);
                }
            }
        }
        if (images.isEmpty()) {
            images.add(createPlaceholderImage());
        }
        return images;
    }
    private ImageIcon createPlaceholderImage() {
        BufferedImage image = new BufferedImage(
                600, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, 600, 400);
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Arial", Font.BOLD, 24));

        String text = "No Image Available";
        FontMetrics fm = g.getFontMetrics();
        int x = (600 - fm.stringWidth(text)) / 2;
        int y = (400 - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
        g.dispose();
        return new ImageIcon(image);
    }
}
