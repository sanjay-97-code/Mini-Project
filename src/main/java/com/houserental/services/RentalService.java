package main.java.com.houserental.services;

import main.java.com.houserental.config.DatabaseConnection;
import main.java.com.houserental.models.Rental;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalService {
    public boolean createRental(int propertyId, int tenantId, LocalDate startDate,
                                LocalDate endDate, double monthlyRent, double securityDeposit) {
        String sql = "INSERT INTO rentals (property_id, tenant_id, start_date, end_date, " +
                "monthly_rent, security_deposit, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'pending', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, propertyId);
            pstmt.setInt(2, tenantId);
            pstmt.setDate(3, Date.valueOf(startDate));
            pstmt.setDate(4, Date.valueOf(endDate));
            pstmt.setDouble(5, monthlyRent);
            pstmt.setDouble(6, securityDeposit);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating rental: " + e.getMessage());
            return false;
        }
    }
    public List<Rental> getRentalsByTenant(int tenantId) {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals WHERE tenant_id = ? ORDER BY start_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tenantId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                rentals.add(mapResultSetToRental(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tenant rentals: " + e.getMessage());
        }

        return rentals;
    }
    public List<Rental> getRentalsByProperty(int propertyId) {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals WHERE property_id = ? ORDER BY start_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, propertyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                rentals.add(mapResultSetToRental(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching property rentals: " + e.getMessage());
        }

        return rentals;
    }
    public boolean updateRentalStatus(int rentalId, String newStatus) {
        String sql = "UPDATE rentals SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE rental_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, rentalId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating rental status: " + e.getMessage());
            return false;
        }
    }
    public boolean isPropertyAvailable(int propertyId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) FROM rentals WHERE property_id = ? AND status = 'active' " +
                "AND ((start_date BETWEEN ? AND ?) OR (end_date BETWEEN ? AND ?) " +
                "OR (? BETWEEN start_date AND end_date) OR (? BETWEEN start_date AND end_date))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, propertyId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            pstmt.setDate(4, Date.valueOf(startDate));
            pstmt.setDate(5, Date.valueOf(endDate));
            pstmt.setDate(6, Date.valueOf(startDate));
            pstmt.setDate(7, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            System.err.println("Error checking property availability: " + e.getMessage());
            return false;
        }
    }
    public Rental getRentalById(int rentalId) {
        String sql = "SELECT * FROM rentals WHERE rental_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rentalId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRental(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching rental by ID: " + e.getMessage());
        }
        return null;
    }
    private Rental mapResultSetToRental(ResultSet rs) throws SQLException {
        Rental rental = new Rental();
        rental.setRentalId(rs.getInt("rental_id"));
        rental.setPropertyId(rs.getInt("property_id"));
        rental.setTenantId(rs.getInt("tenant_id"));
        rental.setStartDate(rs.getDate("start_date").toLocalDate());
        rental.setEndDate(rs.getDate("end_date").toLocalDate());
        rental.setMonthlyRent(rs.getDouble("monthly_rent"));
        rental.setSecurityDeposit(rs.getDouble("security_deposit"));
        rental.setStatus(rs.getString("status"));
        rental.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        rental.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return rental;
    }
}
