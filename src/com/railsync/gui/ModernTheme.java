package com.railsync.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Styling utilities and constants for the Swing GUI.
 * Provides colors, fonts, button styling, and layout helpers.
 */
public final class ModernTheme {

    // Color Palette
    public static final Color PRIMARY = new Color(26, 54, 93);       // Deep Navy
    public static final Color PRIMARY_HOVER = new Color(43, 108, 176);
    public static final Color ACCENT = new Color(13, 148, 136);       // Modern Teal
    public static final Color ACCENT_HOVER = new Color(15, 118, 110);
    public static final Color BG_DARK = new Color(15, 23, 42);        // Slate 900
    public static final Color BG_MAIN = new Color(248, 250, 252);     // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240);// Slate 200
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);   // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // Slate 500
    public static final Color SUCCESS = new Color(22, 163, 74);       // Green 600
    public static final Color WARNING = new Color(217, 119, 6);       // Amber 600
    public static final Color DANGER = new Color(220, 38, 38);        // Red 600

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    private ModernTheme() {}

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton createAccentButton(String text) {
        return createStyledButton(text, ACCENT, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = createStyledButton(text, new Color(241, 245, 249), TEXT_PRIMARY);
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1), new EmptyBorder(8, 16, 8, 16)));
        return btn;
    }

    private static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 18, 8, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SUBHEADER);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(new LineBorder(BORDER_COLOR));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Integer.class, centerRenderer);
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(FONT_SMALL);
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        return badge;
    }
}
