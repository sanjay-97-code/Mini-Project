package main.java.com.houserental.views.owner;

import main.java.com.houserental.dao.RentalRequestDAO;
import main.java.com.houserental.models.RentalRequest;
import main.java.com.houserental.models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import main.java.com.houserental.config.DatabaseConnection;

public class RentalRequestView extends JPanel {
    private JList<RentalRequest> requestList;
    private DefaultListModel<RentalRequest> listModel;
    private User owner;
    private RentalRequestDAO rentalRequestDAO;
    private SimpleDateFormat dateFormat;

    public RentalRequestView(User owner) {
        this.owner = owner;
        this.rentalRequestDAO = new RentalRequestDAO();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        setLayout(new BorderLayout());
        initializeUI();
        loadRequests();
    }

    private void initializeUI() {
        listModel = new DefaultListModel<>();
        requestList = new JList<>(listModel);
        requestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestList.setCellRenderer(new RentalRequestCellRenderer());

        JScrollPane scrollPane = new JScrollPane(requestList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton approveButton = createStyledButton("Approve", new Color(76, 175, 80));
        approveButton.addActionListener(this::approveRequest);

        JButton rejectButton = createStyledButton("Reject", new Color(244, 67, 54));
        rejectButton.addActionListener(this::rejectRequest);

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return button;
    }

    private String formatDate(Date date) {
        return date != null ? dateFormat.format(date) : "N/A";
    }

    private void loadRequests() {
        SwingWorker<List<RentalRequest>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RentalRequest> doInBackground() throws Exception {
                return rentalRequestDAO.getRequestsByOwner(owner.getUserId());
            }

            @Override
            protected void done() {
                try {
                    listModel.clear();
                    List<RentalRequest> requests = get();

                    if (requests.isEmpty()) {
                        listModel.addElement(createEmptyRequest());
                        requestList.setEnabled(false);
                    } else {
                        requests.forEach(listModel::addElement);
                        requestList.setEnabled(true);
                    }
                } catch (Exception e) {
                    showError("Error loading requests: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private RentalRequest createEmptyRequest() {
        RentalRequest empty = new RentalRequest();
        empty.setMessage("No rental requests found");
        return empty;
    }

    private void approveRequest(ActionEvent e) {
        RentalRequest selected = requestList.getSelectedValue();
        if (!isValidRequest(selected)) {
            showWarning("Please select a valid rental request first");
            return;
        }

        String message = buildConfirmationMessage("APPROVE", selected);
        if (showConfirmationDialog("Confirm Approval", message) == JOptionPane.YES_OPTION) {
            processRequest(selected, "APPROVED", true);
        }
    }

    private void rejectRequest(ActionEvent e) {
        RentalRequest selected = requestList.getSelectedValue();
        if (!isValidRequest(selected)) {
            showWarning("Please select a valid rental request first");
            return;
        }

        String message = buildConfirmationMessage("REJECT", selected);
        if (showConfirmationDialog("Confirm Rejection", message) == JOptionPane.YES_OPTION) {
            processRequest(selected, "REJECTED", false);
        }
    }

    private boolean isValidRequest(RentalRequest request) {
        return request != null && request.getRequestId() != 0;
    }

    private String buildConfirmationMessage(String action, RentalRequest request) {
        return String.format(
                "Confirm %s\n\nProperty: %s\nTenant: %s\nPeriod: %s to %s",
                action,
                safeGet(request.getPropertyTitle()),
                safeGet(request.getTenantName()),
                formatDate(request.getStartDate()),
                formatDate(request.getEndDate())
        );
    }

    private int showConfirmationDialog(String title, String message) {
        return JOptionPane.showConfirmDialog(
                this,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    private void processRequest(RentalRequest request, String status, boolean updateProperty) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Connection conn = null;
                try {
                    conn = DatabaseConnection.getConnection();
                    conn.setAutoCommit(false);

                    // Update request status
                    if (!updateRequestStatus(conn, request, status)) {
                        conn.rollback();
                        return false;
                    }

                    // Update property status if approving
                    if (updateProperty && !updatePropertyStatus(conn, request)) {
                        conn.rollback();
                        return false;
                    }

                    // Send notification
                    if (!sendNotification(conn, request, status)) {
                        conn.rollback();
                        return false;
                    }

                    conn.commit();
                    return true;
                } catch (SQLException ex) {
                    if (conn != null) conn.rollback();
                    throw ex;
                } finally {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        showSuccess(String.format(
                                "Rental request %s successfully!%s",
                                status.toLowerCase(),
                                updateProperty ? "\nProperty status updated to RENTED." : ""
                        ));
                        loadRequests();
                    } else {
                        showError("Failed to process rental request");
                    }
                } catch (Exception ex) {
                    showError("Error processing request: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private boolean updateRequestStatus(Connection conn, RentalRequest request, String status) throws SQLException {
        String sql = "UPDATE rental_requests SET status = ?, response_date = CURRENT_DATE WHERE request_id = ? AND owner_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, request.getRequestId());
            stmt.setInt(3, owner.getUserId());
            return stmt.executeUpdate() > 0;
        }
    }

    private boolean updatePropertyStatus(Connection conn, RentalRequest request) throws SQLException {
        String sql = "UPDATE properties SET status = 'RENTED' WHERE property_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, request.getPropertyId());
            return stmt.executeUpdate() > 0;
        }
    }

    private boolean sendNotification(Connection conn, RentalRequest request, String status) throws SQLException {
        String title = "Rental " + status;
        String message = String.format(
                "Your request for %s has been %s",
                safeGet(request.getPropertyTitle()),
                status.toLowerCase()
        );

        String sql = "INSERT INTO notifications (user_id, title, message, type, is_read, created_at) " +
                "VALUES (?, ?, ?, ?, false, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, request.getTenantId());
            stmt.setString(2, title);
            stmt.setString(3, message);
            stmt.setString(4, "RENTAL_" + status);
            stmt.executeUpdate();
            return true;
        }
    }

    private String safeGet(String value) {
        return value != null ? value : "N/A";
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private class RentalRequestCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof RentalRequest) {
                RentalRequest request = (RentalRequest) value;

                if (request.getRequestId() == 0) {
                    setEnabled(false);
                    setBackground(list.getBackground());
                    setForeground(Color.GRAY);
                    setText(request.getMessage());
                    return this;
                }

                String status = safeGet(request.getStatus()).toUpperCase();
                Color statusColor = getStatusColor(status);

                String text = String.format(
                        "Property: %s\nTenant: %s\nPeriod: %s to %s\nStatus: %s\nMessage: %s",
                        safeGet(request.getPropertyTitle()),
                        safeGet(request.getTenantName()),
                        formatDate(request.getStartDate()),
                        formatDate(request.getEndDate()),
                        status,
                        safeGet(request.getMessage())
                );

                setText(text);
                setForeground(statusColor);
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            }

            return this;
        }

        private Color getStatusColor(String status) {
            switch (status) {
                case "PENDING": return Color.ORANGE;
                case "APPROVED": return Color.GREEN;
                case "REJECTED": return Color.RED;
                default: return Color.GRAY;
            }
        }
    }
}