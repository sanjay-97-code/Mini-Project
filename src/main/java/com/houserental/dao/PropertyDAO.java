package main.java.com.houserental.dao;

import main.java.com.houserental.config.DatabaseConnection;
import main.java.com.houserental.models.Property;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyDAO {
    private static final Logger logger = Logger.getLogger(PropertyDAO.class.getName());
    private static final List<String> VALID_PROPERTY_TYPES =
            Arrays.asList("house", "apartment", "villa", "condo", "other");

    // ========== PROPERTY CRUD OPERATIONS ========== //

    public boolean addProperty(Property property) {
        String sql = "INSERT INTO properties (owner_id, title, description, property_type, " +
                "address, city, state, pincode, bedrooms, bathrooms, area_sqft, " +
                "monthly_rent, security_deposit, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setPropertyParameters(stmt, property);

                if (stmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int propertyId = rs.getInt(1);
                        if (hasImages(property) && !savePropertyImages(conn, propertyId,
                                property.getImagePaths(), property.getPrimaryImagePath())) {
                            conn.rollback();
                            return false;
                        }
                        conn.commit();
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            handleSQLException(conn, "Error adding property", e);
        } finally {
            closeConnection(conn);
        }
        return false;
    }

    public boolean updateProperty(Property property, boolean updateImages) {
        String sql = "UPDATE properties SET " +
                "title = ?, description = ?, property_type = ?, " +
                "address = ?, city = ?, state = ?, pincode = ?, " +
                "bedrooms = ?, bathrooms = ?, area_sqft = ?, " +
                "monthly_rent = ?, security_deposit = ?, status = ? " +
                "WHERE property_id = ? AND owner_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set update parameters
                stmt.setString(1, property.getTitle());
                stmt.setString(2, property.getDescription());
                stmt.setString(3, sanitizePropertyType(property.getPropertyType()));
                stmt.setString(4, property.getAddress());
                stmt.setString(5, property.getCity());
                stmt.setString(6, property.getState());
                stmt.setString(7, property.getPincode());
                stmt.setInt(8, property.getBedrooms());
                stmt.setInt(9, property.getBathrooms());
                stmt.setInt(10, property.getAreaSqft());
                stmt.setDouble(11, property.getMonthlyRent());
                stmt.setDouble(12, property.getSecurityDeposit());
                stmt.setString(13, property.getStatus());
                // WHERE clause parameters
                stmt.setInt(14, property.getPropertyId());
                stmt.setInt(15, property.getOwnerId());

                if (stmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                if (updateImages && hasImages(property)) {
                    if (!savePropertyImages(conn, property.getPropertyId(),
                            property.getImagePaths(), property.getPrimaryImagePath())) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            handleSQLException(conn, "Error updating property ID: " + property.getPropertyId(), e);
        } finally {
            closeConnection(conn);
        }
        return false;
    }

    public boolean deleteProperty(int propertyId, int ownerId) {
        String sql = "DELETE FROM properties WHERE property_id = ? AND owner_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // First delete images
            if (!deletePropertyImages(conn, propertyId)) {
                conn.rollback();
                return false;
            }

            // Then delete property
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, propertyId);
                stmt.setInt(2, ownerId);

                if (stmt.executeUpdate() > 0) {
                    conn.commit();
                    return true;
                }
                conn.rollback();
            }
        } catch (SQLException e) {
            handleSQLException(conn, "Error deleting property", e);
        } finally {
            closeConnection(conn);
        }
        return false;
    }

    // ========== IMAGE MANAGEMENT OPERATIONS ========== //

    public boolean updatePropertyImages(int propertyId, List<String> imagePaths, String primaryImagePath) {
        if (primaryImagePath != null && !imagePaths.contains(primaryImagePath)) {
            logger.warning("Primary image must be in the image paths list");
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            if (!deletePropertyImages(conn, propertyId)) {
                conn.rollback();
                return false;
            }

            if (imagePaths != null && !imagePaths.isEmpty()) {
                if (!savePropertyImages(conn, propertyId, imagePaths, primaryImagePath)) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            handleSQLException(conn, "Error updating images for property ID: " + propertyId, e);
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public boolean addPropertyImage(int propertyId, String imagePath, boolean setAsPrimary) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            if (setAsPrimary) {
                String unsetSql = "UPDATE property_images SET is_primary = false WHERE property_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(unsetSql)) {
                    stmt.setInt(1, propertyId);
                    stmt.executeUpdate();
                }
            }

            String insertSql = "INSERT INTO property_images (property_id, image_path, is_primary) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, propertyId);
                stmt.setString(2, imagePath);
                stmt.setBoolean(3, setAsPrimary);
                if (stmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            handleSQLException(conn, "Error adding image to property ID: " + propertyId, e);
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public boolean setPrimaryImage(int propertyId, String imagePath) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // First unset any existing primary
            String unsetSql = "UPDATE property_images SET is_primary = false WHERE property_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(unsetSql)) {
                stmt.setInt(1, propertyId);
                stmt.executeUpdate();
            }

            // Set new primary
            String setSql = "UPDATE property_images SET is_primary = true WHERE property_id = ? AND image_path = ?";
            try (PreparedStatement stmt = conn.prepareStatement(setSql)) {
                stmt.setInt(1, propertyId);
                stmt.setString(2, imagePath);
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    return false; // Image not found for this property
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            handleSQLException(conn, "Error setting primary image for property ID: " + propertyId, e);
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public boolean deletePropertyImage(int propertyId, String imagePath) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // First check if this is the primary image
            boolean wasPrimary = false;
            String checkSql = "SELECT is_primary FROM property_images WHERE property_id = ? AND image_path = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setInt(1, propertyId);
                stmt.setString(2, imagePath);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    wasPrimary = rs.getBoolean("is_primary");
                } else {
                    conn.rollback();
                    return false; // Image not found
                }
            }

            // Delete the image
            String deleteSql = "DELETE FROM property_images WHERE property_id = ? AND image_path = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, propertyId);
                stmt.setString(2, imagePath);
                if (stmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // If we deleted the primary, set a new primary (first available)
            if (wasPrimary) {
                String updateSql = "UPDATE property_images SET is_primary = true " +
                        "WHERE property_id = ? AND image_path = (" +
                        "SELECT image_path FROM property_images WHERE property_id = ? LIMIT 1)";
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setInt(1, propertyId);
                    stmt.setInt(2, propertyId);
                    stmt.executeUpdate(); // OK if no rows updated (no images left)
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            handleSQLException(conn, "Error deleting image from property ID: " + propertyId, e);
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    // ========== PROPERTY QUERIES ========== //

    public List<Property> getAllPropertiesByOwner(int ownerId) {
        String sql = "SELECT p.*, pi.image_path, pi.is_primary " +
                "FROM properties p " +
                "LEFT JOIN property_images pi ON p.property_id = pi.property_id " +
                "WHERE p.owner_id = ? " +
                "ORDER BY p.property_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ownerId);
            return processPropertyResultSet(stmt.executeQuery());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting properties by owner", e);
        }
        return Collections.emptyList();
    }

    public Property getPropertyById(int propertyId) {
        String sql = "SELECT p.*, pi.image_path, pi.is_primary " +
                "FROM properties p " +
                "LEFT JOIN property_images pi ON p.property_id = pi.property_id " +
                "WHERE p.property_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, propertyId);
            List<Property> properties = processPropertyResultSet(stmt.executeQuery());
            return properties.isEmpty() ? null : properties.get(0);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting property by ID", e);
        }
        return null;
    }

    // ========== PRIVATE HELPER METHODS ========== //

    private boolean savePropertyImages(Connection conn, int propertyId,
                                       List<String> imagePaths, String primaryImagePath)
            throws SQLException {
        String sql = "INSERT INTO property_images (property_id, image_path, is_primary) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String imagePath : imagePaths) {
                stmt.setInt(1, propertyId);
                stmt.setString(2, imagePath);
                stmt.setBoolean(3, imagePath.equals(primaryImagePath));
                stmt.addBatch();
            }
            stmt.executeBatch();
            return true;
        }
    }

    private boolean deletePropertyImages(Connection conn, int propertyId) throws SQLException {
        String sql = "DELETE FROM property_images WHERE property_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, propertyId);
            return stmt.executeUpdate() >= 0;
        }
    }

    private void setPropertyParameters(PreparedStatement stmt, Property property)
            throws SQLException {
        stmt.setInt(1, property.getOwnerId());
        stmt.setString(2, property.getTitle());
        stmt.setString(3, property.getDescription());
        stmt.setString(4, sanitizePropertyType(property.getPropertyType()));
        stmt.setString(5, property.getAddress());
        stmt.setString(6, property.getCity());
        stmt.setString(7, property.getState());
        stmt.setString(8, property.getPincode());
        stmt.setInt(9, property.getBedrooms());
        stmt.setInt(10, property.getBathrooms());
        stmt.setInt(11, property.getAreaSqft());
        stmt.setDouble(12, property.getMonthlyRent());
        stmt.setDouble(13, property.getSecurityDeposit());
        stmt.setString(14, property.getStatus());
    }

    private List<Property> processPropertyResultSet(ResultSet rs) throws SQLException {
        Map<Integer, Property> propertyMap = new LinkedHashMap<>();

        while (rs.next()) {
            int propertyId = rs.getInt("property_id");

            Property property = propertyMap.get(propertyId);
            if (property == null) {
                property = new Property();
                property.setPropertyId(propertyId);
                property.setOwnerId(rs.getInt("owner_id"));
                property.setTitle(rs.getString("title"));
                property.setDescription(rs.getString("description"));
                property.setPropertyType(rs.getString("property_type"));
                property.setAddress(rs.getString("address"));
                property.setCity(rs.getString("city"));
                property.setState(rs.getString("state"));
                property.setPincode(rs.getString("pincode"));
                property.setBedrooms(rs.getInt("bedrooms"));
                property.setBathrooms(rs.getInt("bathrooms"));
                property.setAreaSqft(rs.getInt("area_sqft"));
                property.setMonthlyRent(rs.getDouble("monthly_rent"));
                property.setSecurityDeposit(rs.getDouble("security_deposit"));
                property.setStatus(rs.getString("status"));
                property.setImagePaths(new ArrayList<>());
                propertyMap.put(propertyId, property);
            }

            String imagePath = rs.getString("image_path");
            if (imagePath != null) {
                property.getImagePaths().add(imagePath);
                if (rs.getBoolean("is_primary")) {
                    property.setPrimaryImagePath(imagePath);
                }
            }
        }

        return new ArrayList<>(propertyMap.values());
    }

    private String sanitizePropertyType(String propertyType) {
        if (propertyType == null) {
            return "other";
        }
        String sanitized = propertyType.trim().toLowerCase();
        return VALID_PROPERTY_TYPES.contains(sanitized) ? sanitized : "other";
    }

    private boolean hasImages(Property property) {
        return property.getImagePaths() != null && !property.getImagePaths().isEmpty();
    }

    private void handleSQLException(Connection conn, String message, SQLException e) {
        logger.log(Level.SEVERE, message, e);
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing connection", e);
            }
        }
    }

    // ========== RENTAL MANAGEMENT ========== //

    public boolean createRental(int tenantId, int propertyId, double monthlyRent, double securityDeposit)
            throws SQLException {
        String query = "INSERT INTO rentals (property_id, tenant_id, start_date, end_date, " +
                "monthly_rent, security_deposit, status) " +
                "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), ?, ?, 'pending')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, propertyId);
            stmt.setInt(2, tenantId);
            stmt.setDouble(3, monthlyRent);
            stmt.setDouble(4, securityDeposit);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                updatePropertyStatus(propertyId, "pending");
                return true;
            }
        }
        return false;
    }

    public boolean updatePropertyStatus(int propertyId, String status) throws SQLException {
        String query = "UPDATE properties SET status = ? WHERE property_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, propertyId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ========== SEARCH & FILTER METHODS ========== //

    public List<Property> getAvailableProperties(String city, String propertyType,
                                                 Double minPrice, Double maxPrice,
                                                 Integer minBedrooms, int offset, int limit) {
        List<Property> properties = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, pi.image_path, pi.is_primary " +
                        "FROM properties p " +
                        "LEFT JOIN property_images pi ON p.property_id = pi.property_id AND pi.is_primary = true " +
                        "WHERE p.status = 'AVAILABLE'");

        List<Object> parameters = new ArrayList<>();
        if (city != null && !city.isEmpty()) {
            sql.append(" AND p.city = ?");
            parameters.add(city);
        }
        if (propertyType != null && !propertyType.isEmpty()) {
            sql.append(" AND p.property_type = ?");
            parameters.add(propertyType);
        }
        if (minPrice != null) {
            sql.append(" AND p.monthly_rent >= ?");
            parameters.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND p.monthly_rent <= ?");
            parameters.add(maxPrice);
        }
        if (minBedrooms != null) {
            sql.append(" AND p.bedrooms >= ?");
            parameters.add(minBedrooms);
        }
        sql.append(" ORDER BY p.property_id LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add(offset);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
            properties = processPropertyResultSet(stmt.executeQuery());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting available properties", e);
        }
        return properties;
    }

    // ========== FAVORITES MANAGEMENT ========== //

    public boolean addToFavorites(int userId, int propertyId) throws SQLException {
        String query = "INSERT INTO favorites (user_id, property_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, propertyId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean removeFromFavorites(int userId, int propertyId) throws SQLException {
        String query = "DELETE FROM favorites WHERE user_id = ? AND property_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, propertyId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean isFavorite(int userId, int propertyId) throws SQLException {
        String query = "SELECT 1 FROM favorites WHERE user_id = ? AND property_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, propertyId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Property> getFavoriteProperties(int userId) {
        String sql = "SELECT p.*, pi.image_path, pi.is_primary " +
                "FROM properties p " +
                "JOIN favorites f ON p.property_id = f.property_id " +
                "LEFT JOIN property_images pi ON p.property_id = pi.property_id AND pi.is_primary = true " +
                "WHERE f.user_id = ? AND p.status = 'AVAILABLE' " +
                "ORDER BY p.property_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return processPropertyResultSet(stmt.executeQuery());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting favorite properties", e);
            return Collections.emptyList();
        }
    }

    // ========== RENTAL REQUEST MANAGEMENT ========== //

    public boolean createRentalRequest(int tenantId, int propertyId,
                                       Date startDate, Date endDate,
                                       String message) throws SQLException {
        String sql = "INSERT INTO rental_requests " +
                "(property_id, tenant_id, owner_id, start_date, end_date, " +
                "message, status, request_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'PENDING', CURRENT_DATE)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int ownerId = getPropertyOwnerId(propertyId, conn);
            if (ownerId == -1) {
                return false;
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, propertyId);
                stmt.setInt(2, tenantId);
                stmt.setInt(3, ownerId);
                stmt.setDate(4, new java.sql.Date(startDate.getTime()));
                stmt.setDate(5, new java.sql.Date(endDate.getTime()));
                stmt.setString(6, message);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    conn.commit();
                    return true;
                }
                conn.rollback();
                return false;
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error closing connection", e);
                }
            }
        }
    }

    private int getPropertyOwnerId(int propertyId, Connection conn) throws SQLException {
        String sql = "SELECT owner_id FROM properties WHERE property_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("owner_id") : -1;
        }
    }

    public boolean approveRentalRequest(int requestId, int ownerId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Update rental request status
            String updateRequestSql = "UPDATE rental_requests SET status = 'APPROVED', response_date = CURRENT_DATE " +
                    "WHERE request_id = ? AND owner_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateRequestSql)) {
                stmt.setInt(1, requestId);
                stmt.setInt(2, ownerId);
                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated == 0) {
                    conn.rollback();
                    return false;
                }
            }
            int propertyId = getPropertyIdFromRequest(requestId, conn);
            if (propertyId == -1) {
                conn.rollback();
                return false;
            }
            String updatePropertySql = "UPDATE properties SET status = 'RENTED' WHERE property_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updatePropertySql)) {
                stmt.setInt(1, propertyId);
                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated == 0) {
                    conn.rollback();
                    return false;
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private int getPropertyIdFromRequest(int requestId, Connection conn) throws SQLException {
        String sql = "SELECT property_id FROM rental_requests WHERE request_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("property_id") : -1;
        }
    }

    // ========== NOTIFICATION METHODS ========== //

    public void createNotification(int userId, String title, String message,
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

    // ========== UTILITY METHODS ========== //

    public List<String> getAvailableCities() {
        String sql = "SELECT DISTINCT city FROM properties " +
                "WHERE status = 'AVAILABLE' AND city IS NOT NULL " +
                "ORDER BY city";

        List<String> cities = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cities.add(rs.getString("city"));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching available cities", e);
        }

        return cities;
    }

    public List<Property> getApprovedRentalsForTenant(int tenantId) throws SQLException {
        String sql = "SELECT p.*, pi.image_path, pi.is_primary " +
                "FROM properties p " +
                "JOIN rental_requests rr ON p.property_id = rr.property_id " +
                "LEFT JOIN property_images pi ON p.property_id = pi.property_id AND pi.is_primary = true " +
                "WHERE rr.tenant_id = ? AND rr.status = 'APPROVED' " +
                "ORDER BY rr.request_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tenantId);
            return processPropertyResultSet(stmt.executeQuery());
        }
    }
}