package main.java.com.houserental.models;

public class PropertyImage {
    private int imageId;
    private int propertyId;
    private String imagePath;
    private boolean isPrimary;

    // Constructors
    public PropertyImage() {}

    public PropertyImage(int imageId, int propertyId, String imagePath, boolean isPrimary) {
        this.imageId = imageId;
        this.propertyId = propertyId;
        this.imagePath = imagePath;
        this.isPrimary = isPrimary;
    }

    // Getters and setters
    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}
