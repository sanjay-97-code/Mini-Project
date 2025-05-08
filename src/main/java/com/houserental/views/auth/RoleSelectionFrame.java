package main.java.com.houserental.views.auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import main.java.com.houserental.models.User;
import main.java.com.houserental.views.owner.OwnerDashboard;
import main.java.com.houserental.views.tenant.TenantDashboard;
import main.java.com.houserental.services.NotificationService;
import main.java.com.houserental.views.shared.ProfileFrame;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.Timer;

public class RoleSelectionFrame extends JFrame {
    private User user;
    private NotificationService notificationService;
    private JButton notificationBtn;
    private JButton logoutBtn;
    private JButton profileBtn;
    private JPopupMenu notificationMenu;
    private Map<JMenuItem, Integer> notificationMap;
    private Timer notificationTimer;

    public RoleSelectionFrame(User user) {
        this.user = user;
        this.notificationService = new NotificationService();
        this.notificationMap = new HashMap<>();
        initializeUI();
        setupNotificationPolling();
    }

    private void initializeUI() {
        setTitle("Select Your Role");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(52, 143, 226);
                Color color2 = new Color(86, 101, 115);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Header panel with notification button, logout button, and profile button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Profile button
        profileBtn = new JButton("Profile");
        profileBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        profileBtn.setForeground(Color.WHITE);
        profileBtn.setBackground(new Color(40, 167, 69));
        profileBtn.setFocusPainted(false);
        profileBtn.setBorderPainted(false);
        profileBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profileBtn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        profileBtn.addActionListener(e -> openProfileFrame());

        // Explicit hover effect for profile button
        profileBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                profileBtn.setBackground(new Color(72, 201, 121));
                profileBtn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                profileBtn.setBackground(new Color(40, 167, 69));
                profileBtn.repaint();
            }
        });

        // Panel for profile button (left-aligned)
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        profilePanel.setOpaque(false);
        profilePanel.add(profileBtn);

        notificationBtn = new JButton();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/icons/notification.png"));
            Image scaledIcon = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            notificationBtn.setIcon(new ImageIcon(scaledIcon));
        } catch (Exception e) {
            notificationBtn.setText("🔔");
            System.out.println("Notification icon not found, using fallback");
        }

        notificationBtn.setContentAreaFilled(false);
        notificationBtn.setBorderPainted(false);
        notificationBtn.setFocusPainted(false);
        notificationBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        notificationMenu = new JPopupMenu();
        updateNotificationMenu();

        notificationBtn.addActionListener(e -> {
            notificationMenu.show(notificationBtn, 0, notificationBtn.getHeight());
            markNotificationsAsRead();
        });

        notificationBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                notificationBtn.setBackground(new Color(255, 255, 255, 50));
                notificationBtn.setOpaque(true);
                notificationBtn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                notificationBtn.setBackground(null);
                notificationBtn.setOpaque(false);
                notificationBtn.repaint();
            }
        });

        // Logout button
        logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        logoutBtn.addActionListener(e -> handleLogout());

        // Explicit hover effect for logout button
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setBackground(new Color(255, 99, 99));
                logoutBtn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutBtn.setBackground(new Color(220, 53, 69));
                logoutBtn.repaint();
            }
        });

        // Container for notification and logout buttons (right-aligned)
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        buttonContainer.setOpaque(false);
        buttonContainer.add(notificationBtn);
        buttonContainer.add(logoutBtn);

        // Add profile panel to the west and button container to the east
        headerPanel.add(profilePanel, BorderLayout.WEST);
        headerPanel.add(buttonContainer, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 50, 50));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(100, 150, 100, 150));

        JPanel ownerPanel = createRolePanel(
                "List Properties",
                "I want to list my property for rent",
                new Color(46, 134, 222),
                "/images/house_icon.png"
        );
        ownerPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                openOwnerDashboard();
            }
        });

        JPanel tenantPanel = createRolePanel(
                "Rent Properties",
                "I want to find a place to rent",
                new Color(40, 180, 99),
                "/images/rent_icon.png"
        );
        tenantPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                openTenantDashboard();
            }
        });

        centerPanel.add(ownerPanel);
        centerPanel.add(tenantPanel);

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getFullName() + "!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBorder(new EmptyBorder(40, 0, 20, 0));

        mainPanel.add(welcomeLabel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new main.java.com.houserental.Main();
        }
    }

    private void setupNotificationPolling() {
        notificationTimer = new Timer(30000, e -> updateNotificationMenu());
        notificationTimer.start();
    }

    private void updateNotificationMenu() {
        SwingUtilities.invokeLater(() -> {
            notificationMenu.removeAll();
            notificationMap.clear();

            try {
                List<Map<String, Object>> notifications = notificationService.getUnreadNotifications(user.getUserId());

                if (notifications.isEmpty()) {
                    JMenuItem noNotifications = new JMenuItem("No new notifications");
                    noNotifications.setEnabled(false);
                    notificationMenu.add(noNotifications);
                } else {
                    addNotificationBadge(notifications.size());

                    for (Map<String, Object> notification : notifications) {
                        int notificationId = (int) notification.get("notification_id");
                        String title = (String) notification.get("title");
                        String message = (String) notification.get("message");
                        String type = (String) notification.get("type");

                        JMenuItem menuItem = new JMenuItem("<html><b>" + title + "</b><br>" + message + "</html>");
                        menuItem.addActionListener(e -> handleNotificationClick(notificationId, type));

                        notificationMenu.add(menuItem);
                        notificationMap.put(menuItem, notificationId);

                        if (notifications.indexOf(notification) < notifications.size() - 1) {
                            notificationMenu.addSeparator();
                        }
                    }
                }
            } catch (Exception ex) {
                JMenuItem errorItem = new JMenuItem("Error loading notifications");
                errorItem.setEnabled(false);
                notificationMenu.add(errorItem);
                ex.printStackTrace();
            }
        });
    }

    private void addNotificationBadge(int count) {
        JLabel badge = new JLabel(String.valueOf(count));
        badge.setOpaque(true);
        badge.setBackground(Color.RED);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Arial", Font.BOLD, 10));
        badge.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        badge.setHorizontalAlignment(SwingConstants.CENTER);

        for (Component comp : notificationBtn.getComponents()) {
            if (comp instanceof JLabel) {
                notificationBtn.remove(comp);
            }
        }

        notificationBtn.setLayout(new OverlayLayout(notificationBtn));
        notificationBtn.add(badge);
        badge.setAlignmentX(1.0f);
        badge.setAlignmentY(0.0f);
        notificationBtn.revalidate();
        notificationBtn.repaint();
    }

    private void handleNotificationClick(int notificationId, String type) {
        notificationService.markNotificationAsRead(notificationId);

        switch (type) {
            case "RENTAL_REQUEST":
                break;
            case "RENTAL_APPROVED":
                JOptionPane.showMessageDialog(this, "Your rental request has been approved!", "Rental Approved", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "RENTAL_REJECTED":
                JOptionPane.showMessageDialog(this, "Your rental request has been rejected.", "Rental Rejected", JOptionPane.WARNING_MESSAGE);
                break;
            default:
                break;
        }

        updateNotificationMenu();
    }

    private void markNotificationsAsRead() {
        try {
            notificationService.markAllNotificationsAsRead(user.getUserId());
            for (Component comp : notificationBtn.getComponents()) {
                if (comp instanceof JLabel) {
                    notificationBtn.remove(comp);
                }
            }
            notificationBtn.revalidate();
            notificationBtn.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        if (notificationTimer != null) {
            notificationTimer.stop();
        }
        super.dispose();
    }

    private JPanel createRolePanel(String title, String description, Color bgColor, String iconPath) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 2),
                new EmptyBorder(40, 20, 40, 20)
        ));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            JLabel iconLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
            iconLabel.setHorizontalAlignment(JLabel.CENTER);
            panel.add(iconLabel, BorderLayout.NORTH);
        } catch (Exception e) {
            System.out.println("Icon not found: " + iconPath);
        }

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.CENTER);

        JLabel descLabel = new JLabel(description, JLabel.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descLabel.setForeground(Color.WHITE);
        panel.add(descLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void openOwnerDashboard() {
        new OwnerDashboard(user).setVisible(true);
        dispose();
    }

    private void openTenantDashboard() {
        new TenantDashboard(user).setVisible(true);
        dispose();
    }

    private void openProfileFrame() {
        new ProfileFrame(user).setVisible(true);
    }
}