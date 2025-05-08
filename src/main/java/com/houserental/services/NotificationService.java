package main.java.com.houserental.services;

import java.sql.*;
import java.util.*;
import main.java.com.houserental.config.DatabaseConnection;

public class NotificationService {

    public List<Map<String, Object>> getUnreadNotifications(int userId) {
        List<Map<String, Object>> notifications = new ArrayList<>();
        String query = "SELECT notification_id, title, message, type FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("notification_id", rs.getInt("notification_id"));
                    notification.put("title", rs.getString("title"));
                    notification.put("message", rs.getString("message"));
                    notification.put("type", rs.getString("type"));
                    notifications.add(notification);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notifications;
    }

    public boolean markNotificationAsRead(int notificationId) {
        String query = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean markAllNotificationsAsRead(int userId) {
        String query = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
