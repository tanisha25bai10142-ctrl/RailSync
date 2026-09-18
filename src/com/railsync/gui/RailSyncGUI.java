package com.railsync.gui;

import com.railsync.manager.FileManager;
import com.railsync.manager.ReservationManager;
import com.railsync.model.User;
import com.railsync.util.SampleDataSeeder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main application window for RailSync.
 * Handles user sessions, panel switching (Passenger vs Admin),
 * and menu actions.
 */
public class RailSyncGUI extends JFrame {

    private final ReservationManager manager;
    private User currentUser;
    private final File dataFile;

    private JPanel contentContainer;
    private CardLayout cardLayout;
    private PassengerPanel passengerPanel;
    private AdminPanel adminPanel;

    private JLabel lblUserBadge;
    private JLabel lblStatusTime;

    public RailSyncGUI(ReservationManager manager, File dataFile) {
        super("RailSync – Train Reservation System");
        this.manager = manager;
        this.dataFile = dataFile;

        // Default to Passenger
        this.currentUser = manager.getUser("passenger");

        initLookAndFeel();
        initMenuBar();
        initUI();

        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(ModernTheme.FONT_BODY);

        // File Menu
        JMenu menuFile = new JMenu("File");
        JMenuItem itemSave = new JMenuItem("Save System State (.ser)");
        itemSave.addActionListener(e -> saveState());
        JMenuItem itemExportReport = new JMenuItem("Export Summary Report (.txt)");
        itemExportReport.addActionListener(e -> adminPanel.refreshAllData());
        JMenuItem itemExit = new JMenuItem("Exit");
        itemExit.addActionListener(e -> {
            saveState();
            System.exit(0);
        });
        menuFile.add(itemSave);
        menuFile.add(itemExportReport);
        menuFile.addSeparator();
        menuFile.add(itemExit);

        // Account Menu
        JMenu menuAccount = new JMenu("Session");
        JMenuItem itemSwitch = new JMenuItem("Switch User / Role...");
        itemSwitch.addActionListener(e -> promptLogin());
        JMenuItem itemReseed = new JMenuItem("Re-seed Sample Data");
        itemReseed.addActionListener(e -> reseedData());
        menuAccount.add(itemSwitch);
        menuAccount.add(itemReseed);

        // Tools Menu
        JMenu menuTools = new JMenu("Tools");
        JMenuItem itemSim = new JMenuItem("Open Multithreaded Booking Simulation");
        itemSim.addActionListener(e -> {
            switchRole(true); // Switch to admin view where simulation lab is located
        });
        menuTools.add(itemSim);

        // Help Menu
        JMenu menuHelp = new JMenu("Help");
        JMenuItem itemAbout = new JMenuItem("About RailSync");
        itemAbout.addActionListener(e -> showAboutDialog());
        menuHelp.add(itemAbout);

        menuBar.add(menuFile);
        menuBar.add(menuAccount);
        menuBar.add(menuTools);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ModernTheme.BG_MAIN);

        // Top Banner
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setBackground(ModernTheme.PRIMARY);
        topBanner.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel lblLogo = new JLabel("🚂");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        lblLogo.setForeground(Color.WHITE);

        JPanel textStack = new JPanel(new GridLayout(2, 1, 0, 2));
        textStack.setOpaque(false);
        JLabel lblTitle = new JLabel("RailSync");
        lblTitle.setFont(ModernTheme.FONT_HEADER);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Train Reservation System");
        lblSub.setFont(ModernTheme.FONT_SMALL);
        lblSub.setForeground(new Color(203, 213, 225)); // Slate 300

        textStack.add(lblTitle);
        textStack.add(lblSub);
        titlePanel.add(lblLogo);
        titlePanel.add(textStack);

        // Right side user badge & switcher
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        lblUserBadge = ModernTheme.createBadge(
                currentUser != null ? currentUser.getFullName() + " [" + currentUser.getRole() + "]" : "Guest",
                ModernTheme.ACCENT, Color.WHITE);

        JButton btnSwitchRole = ModernTheme.createSecondaryButton("Switch Role");
        btnSwitchRole.addActionListener(e -> promptLogin());

        JButton btnQuickSave = ModernTheme.createSecondaryButton("💾 Save");
        btnQuickSave.addActionListener(e -> saveState());

        rightPanel.add(lblUserBadge);
        rightPanel.add(btnSwitchRole);
        rightPanel.add(btnQuickSave);

        topBanner.add(titlePanel, BorderLayout.WEST);
        topBanner.add(rightPanel, BorderLayout.EAST);
        root.add(topBanner, BorderLayout.NORTH);

        // Center Card Container
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);

        passengerPanel = new PassengerPanel(manager, currentUser);
        adminPanel = new AdminPanel(manager);

        contentContainer.add(passengerPanel, "PASSENGER");
        contentContainer.add(adminPanel, "ADMIN");

        root.add(contentContainer, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel lblSystemStatus = new JLabel("RailSync | Ready");
        lblSystemStatus.setFont(ModernTheme.FONT_SMALL);
        lblSystemStatus.setForeground(ModernTheme.TEXT_MUTED);

        lblStatusTime = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")));
        lblStatusTime.setFont(ModernTheme.FONT_SMALL);
        lblStatusTime.setForeground(ModernTheme.TEXT_MUTED);

        statusBar.add(lblSystemStatus, BorderLayout.WEST);
        statusBar.add(lblStatusTime, BorderLayout.EAST);
        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);
        updateUserDisplay();
    }

    public void promptLogin() {
        LoginDialog dialog = new LoginDialog(this, manager);
        dialog.setVisible(true);
        User user = dialog.getAuthenticatedUser();
        if (user != null) {
            this.currentUser = user;
            updateUserDisplay();
        }
    }

    private void updateUserDisplay() {
        if (currentUser == null) return;
        lblUserBadge.setText(currentUser.getFullName() + " [" + currentUser.getRole() + "]");
        if (currentUser.isAdmin()) {
            lblUserBadge.setBackground(ModernTheme.DANGER);
            switchRole(true);
        } else {
            lblUserBadge.setBackground(ModernTheme.ACCENT);
            switchRole(false);
        }
    }

    private void switchRole(boolean isAdmin) {
        if (isAdmin) {
            adminPanel.refreshAllData();
            cardLayout.show(contentContainer, "ADMIN");
        } else {
            passengerPanel.setCurrentUser(currentUser);
            cardLayout.show(contentContainer, "PASSENGER");
        }
    }

    private void saveState() {
        try {
            FileManager.saveSystemState(manager, dataFile);
            JOptionPane.showMessageDialog(this, "System state successfully persisted to:\n" + dataFile.getAbsolutePath(),
                    "Data Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to persist state: " + ex.getMessage(),
                    "Persistence Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reseedData() {
        int c = JOptionPane.showConfirmDialog(this, "Reset all trains and bookings to default sample data?",
                "Re-seed Sample Data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            SampleDataSeeder.seedAll(manager);
            adminPanel.refreshAllData();
            passengerPanel.refreshMyBookings();
            JOptionPane.showMessageDialog(this, "Sample data reloaded successfully!");
        }
    }

    private void showAboutDialog() {
        String info = "RailSync – Train Reservation System\n" +
                "Programming in Java Course Project\n\n" +
                "Key Features:\n" +
                "• Train search and seat reservation\n" +
                "• Confirmed, RAC, and Waiting List booking cascade\n" +
                "• Ticket cancellation with automatic promotion & refund calculation\n" +
                "• Multithreaded booking simulation demonstrating thread safety\n" +
                "• Object serialization and file persistence\n" +
                "• Java Swing graphical interface and interactive CLI";
        JOptionPane.showMessageDialog(this, info, "About RailSync", JOptionPane.INFORMATION_MESSAGE);
    }
}
