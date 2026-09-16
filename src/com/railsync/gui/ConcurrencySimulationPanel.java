package com.railsync.gui;

import com.railsync.thread.ConcurrencySimulation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel for demonstrating multithreaded ticket booking.
 * Allows running multiple concurrent booking threads to verify synchronized seat allocation.
 */
public class ConcurrencySimulationPanel extends JPanel {

    private JSpinner spinThreads;
    private JTextArea txtLogs;
    private JButton btnRun;
    private JLabel lblVerdictBadge;
    private JLabel lblStats;

    public ConcurrencySimulationPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        initUI();
    }

    private void initUI() {
        // Control Header Card
        JPanel headerCard = ModernTheme.createCardPanel();
        headerCard.setLayout(new BorderLayout(16, 8));

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        leftControls.setOpaque(false);

        JLabel lblTitle = new JLabel("Multithreaded Booking Simulation");
        lblTitle.setFont(ModernTheme.FONT_SUBHEADER);
        lblTitle.setForeground(ModernTheme.PRIMARY);

        JLabel lblCount = new JLabel("Simultaneous Booking Threads:");
        lblCount.setFont(ModernTheme.FONT_BODY_BOLD);

        spinThreads = new JSpinner(new SpinnerNumberModel(10, 2, 30, 1));
        spinThreads.setFont(ModernTheme.FONT_BODY);
        ((JSpinner.DefaultEditor) spinThreads.getEditor()).getTextField().setColumns(3);

        btnRun = ModernTheme.createAccentButton("▶ Run Simulation");
        btnRun.addActionListener(e -> startSimulationWorker());

        JButton btnClear = ModernTheme.createSecondaryButton("Clear Console");
        btnClear.addActionListener(e -> txtLogs.setText(""));

        leftControls.add(lblTitle);
        leftControls.add(Box.createHorizontalStrut(12));
        leftControls.add(lblCount);
        leftControls.add(spinThreads);
        leftControls.add(btnRun);
        leftControls.add(btnClear);

        headerCard.add(leftControls, BorderLayout.NORTH);

        // Subheader Explanation Banner
        JLabel lblExplain = new JLabel("<html><i>Simulates multiple passenger threads booking tickets simultaneously to demonstrate " +
                "thread synchronization and safe seat allocation.</i></html>");
        lblExplain.setFont(ModernTheme.FONT_SMALL);
        lblExplain.setForeground(ModernTheme.TEXT_MUTED);
        headerCard.add(lblExplain, BorderLayout.SOUTH);

        add(headerCard, BorderLayout.NORTH);

        // Center: Real-time Terminal Log Console
        JPanel consoleCard = ModernTheme.createCardPanel();
        consoleCard.setLayout(new BorderLayout(0, 8));

        JLabel lblConsole = new JLabel("THREAD EXECUTION LOGS");
        lblConsole.setFont(ModernTheme.FONT_SMALL);
        lblConsole.setForeground(ModernTheme.TEXT_MUTED);
        consoleCard.add(lblConsole, BorderLayout.NORTH);

        txtLogs = new JTextArea();
        txtLogs.setFont(ModernTheme.FONT_MONO);
        txtLogs.setEditable(false);
        txtLogs.setBackground(ModernTheme.BG_DARK);
        txtLogs.setForeground(new Color(148, 163, 184)); // Slate 400
        txtLogs.setCaretColor(Color.WHITE);
        txtLogs.setText("Select number of threads and click 'Run Simulation'.\n");

        JScrollPane scroll = new JScrollPane(txtLogs);
        scroll.setBorder(null);
        consoleCard.add(scroll, BorderLayout.CENTER);

        add(consoleCard, BorderLayout.CENTER);

        // Bottom Results & Verdict Bar
        JPanel footerCard = ModernTheme.createCardPanel();
        footerCard.setLayout(new BorderLayout(16, 0));

        lblStats = new JLabel("No simulation executed yet.");
        lblStats.setFont(ModernTheme.FONT_BODY_BOLD);
        lblStats.setForeground(ModernTheme.TEXT_PRIMARY);

        lblVerdictBadge = ModernTheme.createBadge("STATUS: READY", ModernTheme.BORDER_COLOR, ModernTheme.TEXT_MUTED);

        footerCard.add(lblStats, BorderLayout.WEST);
        footerCard.add(lblVerdictBadge, BorderLayout.EAST);
        add(footerCard, BorderLayout.SOUTH);
    }

    private void startSimulationWorker() {
        int threadCount = (Integer) spinThreads.getValue();
        btnRun.setEnabled(false);
        txtLogs.setText("");
        lblVerdictBadge.setText("SIMULATION RUNNING...");
        lblVerdictBadge.setBackground(ModernTheme.WARNING);
        lblVerdictBadge.setForeground(Color.WHITE);
        lblStats.setText("Spawning " + threadCount + " simultaneous booking threads...");

        // Run simulation in background SwingWorker to avoid freezing the GUI
        SwingWorker<ConcurrencySimulation.SimulationResult, String> worker = new SwingWorker<>() {
            @Override
            protected ConcurrencySimulation.SimulationResult doInBackground() {
                return ConcurrencySimulation.runSimulation(threadCount, this::publish);
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String line : chunks) {
                    txtLogs.append(line + "\n");
                }
                txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
            }

            @Override
            protected void done() {
                try {
                    ConcurrencySimulation.SimulationResult res = get();
                    btnRun.setEnabled(true);
                    lblStats.setText(String.format("Result: %d Threads | %d Confirmed | %d RAC | %d WL | %d Exhausted",
                            res.totalThreads, res.confirmedAllocations, res.racAllocations,
                            res.waitingListAllocations, res.failedAttempts));

                    if (res.zeroDuplicateSeats) {
                        lblVerdictBadge.setText("PASSED: Thread-Safe (No Duplicates)");
                        lblVerdictBadge.setBackground(ModernTheme.SUCCESS);
                        lblVerdictBadge.setForeground(Color.WHITE);
                    } else {
                        lblVerdictBadge.setText("FAILED: Duplicate Seats Found");
                        lblVerdictBadge.setBackground(ModernTheme.DANGER);
                        lblVerdictBadge.setForeground(Color.WHITE);
                    }
                } catch (Exception ex) {
                    btnRun.setEnabled(true);
                    txtLogs.append("Simulation execution error: " + ex.getMessage() + "\n");
                }
            }
        };

        worker.execute();
    }
}
