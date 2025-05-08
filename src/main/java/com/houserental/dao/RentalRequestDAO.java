package main.java.com.houserental.dao;

import main.java.com.houserental.config.DatabaseConnection;
import main.java.com.houserental.models.RentalRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalRequestDAO {
    public List<RentalRequest> getRequestsByOwner(int ownerId) throws SQLException {
        List<RentalRequest> requests = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as tenant_name, p.title as property_title " +
                "FROM rental_requests r " +
                "JOIN users u ON r.tenant_id = u.user_id " +
                "JOIN properties p ON r.property_id = p.property_id " +
                "WHERE r.owner_id = ? AND r.status = 'PENDING' " +
                "ORDER BY r.request_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ownerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                RentalRequest request = new RentalRequest();
                request.setRequestId(rs.getInt("request_id"));
                request.setPropertyId(rs.getInt("property_id"));
                request.setTenantId(rs.getInt("tenant_id"));
                request.setTenantName(rs.getString("tenant_name"));
                request.setPropertyTitle(rs.getString("property_title"));
                request.setStartDate(rs.getDate("start_date"));
                request.setEndDate(rs.getDate("end_date"));
                request.setMessage(rs.getString("message"));
                request.setStatus(rs.getString("status"));
                request.setRequestDate(rs.getDate("request_date"));

                requests.add(request);
            }
        }
        return requests;
    }
    public boolean updateRequestStatus(int requestId, String status, int ownerId) throws SQLException {
        String sql = "UPDATE rental_requests SET status = ?, response_date = CURRENT_DATE " +
                "WHERE request_id = ? AND owner_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, requestId);
            stmt.setInt(3, ownerId);

            return stmt.executeUpdate() > 0;
        }
    }
    public boolean approveRequestAndUpdateProperty(int requestId, int ownerId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
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
}