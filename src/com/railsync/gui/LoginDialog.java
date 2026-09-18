package com.railsync.gui;

import com.railsync.manager.ReservationManager;
import com.railsync.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Login dialog for user authentication and role selection.
 */
public class LoginDialog extends JDialog {

    private final ReservationManager manager;
    private User authenticatedUser;

    public LoginDialog(Frame parent, ReservationManager manager) {
        super(parent, "RailSync - User Authentication", true);
        this.manager = manager;
        this.authenticatedUser = null;

        initUI();
        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(ModernTheme.BG_MAIN);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Welcome to RailSync", SwingConstants.CENTER);
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JLabel subtitle = new JLabel("Intelligent Train Reservation & Dynamic Seat Management", SwingConstants.CENTER);
        subtitle.setFont(ModernTheme.FONT_SMALL);
        subtitle.setForeground(ModernTheme.TEXT_MUTED);

        headerPanel.add(title);
        headerPanel.add(subtitle);
        root.add(headerPanel, BorderLayout.NORTH);

        // Center card with Quick Demo and Manual Login
        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Quick Demo Section
        JLabel demoLabel = new JLabel("QUICK 1-CLICK DEMO ACCESS");
        demoLabel.setFont(ModernTheme.FONT_SMALL);
        demoLabel.setForeground(ModernTheme.TEXT_MUTED);
        demoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(demoLabel);
        card.add(Box.createVerticalStrut(10));

        JPanel demoBtnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        demoBtnRow.setOpaque(false);
        demoBtnRow.setMaximumSize(new Dimension(420, 42));

        JButton btnPassengerDemo = ModernTheme.createPrimaryButton("Passenger Demo");
        btnPassengerDemo.setToolTipText("Login as Rahul Sharma (passenger / pass123)");
        btnPassengerDemo.addActionListener(e -> {
            authenticatedUser = manager.authenticateUser("passenger", "pass123");
            dispose();
        });

        JButton btnAdminDemo = ModernTheme.createAccentButton("Admin Demo");
        btnAdminDemo.setToolTipText("Login as Railway Administrator (admin / admin123)");
        btnAdminDemo.addActionListener(e -> {
            authenticatedUser = manager.authenticateUser("admin", "admin123");
            dispose();
        });

        demoBtnRow.add(btnPassengerDemo);
        demoBtnRow.add(btnAdminDemo);
        demoBtnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(demoBtnRow);

        card.add(Box.createVerticalStrut(20));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(420, 2));
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        // Manual Login Form
        JLabel manualLabel = new JLabel("OR SIGN IN WITH CREDENTIALS");
        manualLabel.setFont(ModernTheme.FONT_SMALL);
        manualLabel.setForeground(ModernTheme.TEXT_MUTED);
        manualLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(manualLabel);
        card.add(Box.createVerticalStrut(10));

        JTextField txtUsername = new JTextField("passenger");
        txtUsername.setFont(ModernTheme.FONT_BODY);
        txtUsername.setMaximumSize(new Dimension(420, 36));

        JPasswordField txtPassword = new JPasswordField("pass123");
        txtPassword.setFont(ModernTheme.FONT_BODY);
        txtPassword.setMaximumSize(new Dimension(420, 36));

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(ModernTheme.FONT_BODY_BOLD);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(ModernTheme.FONT_BODY_BOLD);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblUser);
        card.add(Box.createVerticalStrut(4));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(12));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(4));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(16));

        JButton btnSignIn = ModernTheme.createSecondaryButton("Sign In");
        btnSignIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSignIn.setMaximumSize(new Dimension(420, 38));
        btnSignIn.addActionListener(e -> {
            String u = txtUsername.getText().trim();
            String p = new String(txtPassword.getPassword());
            User user = manager.authenticateUser(u, p);
            if (user != null) {
                authenticatedUser = user;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password. Try 'passenger'/'pass123' or 'admin'/'admin123'.",
                        "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(btnSignIn);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}
