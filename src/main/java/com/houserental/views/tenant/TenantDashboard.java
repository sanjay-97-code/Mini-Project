package main.java.com.houserental.views.tenant;

import javax.swing.*;
import java.awt.*;
import main.java.com.houserental.models.User;

public class TenantDashboard extends JFrame {
    private User tenant;
    private JTabbedPane tabbedPane;

    public TenantDashboard(User tenant) {
        this.tenant = tenant;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Tenant Dashboard - " + tenant.getFullName());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create tabbed pane with proper scrolling
        tabbedPane = new JTabbedPane();

        // Create each tab with scroll pane
        JScrollPane browseScroll = new JScrollPane(new BrowseProperties(tenant));
        browseScroll.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane.addTab("Browse Properties", browseScroll);

        JScrollPane rentalsScroll = new JScrollPane(new MyRentals(tenant));
        rentalsScroll.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane.addTab("My Rentals", rentalsScroll);

        JScrollPane favoritesScroll = new JScrollPane(new FavoriteProperties(tenant));
        favoritesScroll.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane.addTab("Favorites", favoritesScroll);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));

        // Left panel with back button and welcome
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JButton backBtn = new JButton("Back");
        styleButton(backBtn);
        backBtn.addActionListener(e -> {
            dispose();
            new main.java.com.houserental.views.auth.RoleSelectionFrame(tenant).setVisible(true);
        });

        JLabel welcomeLabel = new JLabel("Welcome, " + tenant.getFullName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);

        leftPanel.add(backBtn);
        leftPanel.add(welcomeLabel);
        headerPanel.add(leftPanel, BorderLayout.WEST);

        // Logout button
        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn);
        logoutBtn.addActionListener(e -> logout());
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // Configure tabbed pane
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new main.java.com.houserental.Main();
        }
    }
}