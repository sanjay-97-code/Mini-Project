package main.java.com.houserental.services;

import main.java.com.houserental.config.DatabaseConnection;
import main.java.com.houserental.dao.PropertyDAO;
import main.java.com.houserental.models.Property;
import main.java.com.houserental.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PropertyService {
    private PropertyDAO propertyDAO;

    public PropertyService() {
        this.propertyDAO = new PropertyDAO();
    }

    public boolean addProperty(Property property) {
        return propertyDAO.addProperty(property);
    }

    public List<Property> getOwnerProperties(int ownerId) {
        return propertyDAO.getAllPropertiesByOwner(ownerId);
    }

    public boolean updateProperty(Property property) {
        return propertyDAO.updateProperty(property, false); // Explicitly avoid updating images
    }

    public boolean updatePropertyImages(int propertyId, List<String> imagePaths, String primaryImagePath) {
        return propertyDAO.updatePropertyImages(propertyId, imagePaths != null ? imagePaths : new ArrayList<>(), primaryImagePath);
    }

    public boolean deleteProperty(int propertyId, int ownerId) {
        return propertyDAO.deleteProperty(propertyId, ownerId);
    }

    public boolean toggleFavorite(int userId, int propertyId) throws SQLException {
        if (propertyDAO.isFavorite(userId, propertyId)) {
            return propertyDAO.removeFromFavorites(userId, propertyId);
        } else {
            return propertyDAO.addToFavorites(userId, propertyId);
        }
    }

    public void checkFavoriteStatus(User user, Property property) throws SQLException {
        property.setFavorite(propertyDAO.isFavorite(user.getUserId(), property.getPropertyId()));
    }

    public boolean rentProperty(int tenantId, int propertyId) throws SQLException {
        Date startDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.MONTH, 1);
        Date endDate = cal.getTime();

        return sendRentalRequest(tenantId, propertyId, startDate, endDate,
                "I would like to rent this property");
    }

    public boolean sendRentalRequest(int tenantId, int propertyId,
                                     Date startDate, Date endDate,
                                     String message) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int ownerId = getPropertyOwnerId(propertyId, conn);
                if (ownerId == -1) {
                    return false;
                }

                String sql = "INSERT INTO rental_requests " +
                        "(property_id, tenant_id, owner_id, start_date, end_date, " +
                        "message, status, request_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 'PENDING', CURRENT_DATE)";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, propertyId);
                    stmt.setInt(2, tenantId);
                    stmt.setInt(3, ownerId);
                    stmt.setDate(4, new java.sql.Date(startDate.getTime()));
                    stmt.setDate(5, new java.sql.Date(endDate.getTime()));
                    stmt.setString(6, message);
                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        createNotification(ownerId,
                                "New Rental Request",
                                "You have a new rental request for your property #" + propertyId,
                                "RENTAL_REQUEST",
                                conn);
                        conn.commit();
                        return true;
                    }
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private int getPropertyOwnerId(int propertyId, Connection conn) throws SQLException {
        String sql = "SELECT owner_id FROM properties WHERE property_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, propertyId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("owner_id") : -1;
            }
        }
    }

    private void createNotification(int userId, String title, String message,
                                    String type, Connection conn) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, type, is_read, created_at) " +
                "VALUES (?, ?, ?, ?, false, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, title);
            stmt.setString(3, message);
            stmt.setString(4, type);
            stmt.executeUpdate();
        }
    }

    public List<Property> searchProperties(String city, String propertyType,
                                           double minPrice, double maxPrice,
                                           int bedrooms, int bathrooms) {
        // Assuming PropertyDAO has a searchProperties method; update if needed
        return propertyDAO.getAvailableProperties(
                city, propertyType, minPrice, maxPrice, bedrooms, 0, Integer.MAX_VALUE);
    }

    public Property getPropertyById(int propertyId) throws SQLException {
        return propertyDAO.getPropertyById(propertyId);
    }

    public boolean approveRentalRequest(int requestId, int ownerId) throws SQLException {
        return propertyDAO.approveRentalRequest(requestId, ownerId);
    }

    public List<Property> getFavoriteProperties(int userId) {
        return propertyDAO.getFavoriteProperties(userId);
    }
}