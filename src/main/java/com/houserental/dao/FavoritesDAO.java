package main.java.com.houserental.dao;

import main.java.com.houserental.config.DatabaseConnection;
import main.java.com.houserental.models.Property;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoritesDAO {

    public static List<Property> getFavoritesByUserId(int userId) {
        List<Property> favorites = new ArrayList<>();
        String query = "SELECT p.property_id, p.owner_id, p.title, p.description, " +
                "p.property_type, p.address, p.city, p.state, p.pincode, " +
                "p.bedrooms, p.bathrooms, p.area_sqft as area, p.monthly_rent as price, " +
                "p.security_deposit, p.status, p.created_at, p.updated_at " +
                "FROM properties p " +
                "JOIN favorites f ON p.property_id = f.property_id " +
                "WHERE f.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Property property = new Property();
                property.setPropertyId(rs.getInt("property_id"));
                property.setTitle(rs.getString("title"));
                property.setDescription(rs.getString("description"));
                property.setAddress(rs.getString("address"));
                property.setPrice(rs.getDouble("price"));
                property.setBedrooms(rs.getInt("bedrooms"));
                property.setBathrooms(rs.getInt("bathrooms"));
                property.setAreaSqft((int) rs.getDouble("area"));
                property.setOwnerId(rs.getInt("owner_id"));
                // Set other property fields as needed

                favorites.add(property);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Consider using a logger instead
        }
        return favorites;
    }

    public static boolean addFavorite(int userId, int propertyId) {
        String query = "INSERT INTO favorites (user_id, property_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, propertyId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeFavorite(int userId, int propertyId) {
        String query = "DELETE FROM favorites WHERE user_id = ? AND property_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, propertyId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isFavorite(int userId, int propertyId) {
        String query = "SELECT 1 FROM favorites WHERE user_id = ? AND property_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, propertyId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}