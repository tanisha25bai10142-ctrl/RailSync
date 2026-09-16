package com.railsync.gui;

import com.railsync.manager.ReservationManager;
import com.railsync.model.*;
import com.railsync.service.AnalyticsService;
import com.railsync.service.ReportGenerator;
import com.railsync.service.SearchService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Admin panel for viewing statistics, managing bookings, inspecting RAC/WL queues,
 * and managing trains.
 */
public class AdminPanel extends JPanel {

    private final ReservationManager manager;
    private final SearchService searchService;
    private AnalyticsService analyticsService;

    // Train management
    private JTable trainTable;
    private DefaultTableModel trainModel;

    // Global Bookings & Queues
    private JTable masterBookingsTable;
    private DefaultTableModel masterBookingsModel;
    private JTextField txtGlobalSearch;
    private JComboBox<Train> cbQueueTrain;
    private JComboBox<SeatClass> cbQueueClass;
    private JTextArea txtQueueInspector;

    // Analytics components
    private JLabel lblMetricTrains;
    private JLabel lblMetricBookings;
    private JLabel lblMetricConfirmed;
    private JLabel lblMetricRac;
    private JLabel lblMetricWl;
    private JLabel lblMetricCancelled;
    private JLabel lblMetricGrossRevenue;
    private JLabel lblMetricAvgOccupancy;
    private JLabel lblMetricPopularRoute;
    private JLabel lblMetricTopTrain;
    private DefaultTableModel classOccupancyModel;

    public AdminPanel(ReservationManager manager) {
        this.manager = manager;
        this.searchService = new SearchService(manager.getAllTrains(), manager.getBookingMap(), manager.getFareCalculator());
        this.analyticsService = new AnalyticsService(manager.getAllTrains(), manager.getBookingMap().values());

        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_MAIN);
        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernTheme.FONT_SUBHEADER);

        tabbedPane.addTab("Overview & Statistics", createAnalyticsTab());
        tabbedPane.addTab("Bookings & Search", createMasterBookingsTab());
        tabbedPane.addTab("RAC & Waiting List Queues", createQueueMonitorTab());
        tabbedPane.addTab("Train Management", createFleetManagementTab());
        tabbedPane.addTab("Multithreading Simulation", new ConcurrencySimulationPanel());

        tabbedPane.addChangeListener(e -> refreshAllData());
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void refreshAllData() {
        analyticsService = new AnalyticsService(manager.getAllTrains(), manager.getBookingMap().values());
        refreshAnalyticsTab();
        refreshMasterBookings();
        refreshFleetTable();
        refreshQueueInspector();
    }

    // ================= 1. Analytics Tab =================

    private JPanel createAnalyticsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Metric Cards Grid
        JPanel metricsGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        metricsGrid.setOpaque(false);

        lblMetricTrains = new JLabel("0", SwingConstants.CENTER);
        lblMetricBookings = new JLabel("0", SwingConstants.CENTER);
        lblMetricConfirmed = new JLabel("0", SwingConstants.CENTER);
        lblMetricGrossRevenue = new JLabel("₹ 0.00", SwingConstants.CENTER);

        lblMetricRac = new JLabel("0", SwingConstants.CENTER);
        lblMetricWl = new JLabel("0", SwingConstants.CENTER);
        lblMetricCancelled = new JLabel("0", SwingConstants.CENTER);
        lblMetricAvgOccupancy = new JLabel("0.0%", SwingConstants.CENTER);

        metricsGrid.add(createMetricCard("TOTAL TRAINS", lblMetricTrains, ModernTheme.PRIMARY));
        metricsGrid.add(createMetricCard("TOTAL BOOKINGS", lblMetricBookings, ModernTheme.PRIMARY));
        metricsGrid.add(createMetricCard("CONFIRMED PASSENGERS", lblMetricConfirmed, ModernTheme.SUCCESS));
        metricsGrid.add(createMetricCard("GROSS REVENUE", lblMetricGrossRevenue, ModernTheme.ACCENT));

        metricsGrid.add(createMetricCard("ACTIVE RAC QUEUE", lblMetricRac, ModernTheme.WARNING));
        metricsGrid.add(createMetricCard("ACTIVE WAITING LIST", lblMetricWl, ModernTheme.WARNING));
        metricsGrid.add(createMetricCard("TOTAL CANCELLATIONS", lblMetricCancelled, ModernTheme.DANGER));
        metricsGrid.add(createMetricCard("AVG FLEET OCCUPANCY", lblMetricAvgOccupancy, ModernTheme.PRIMARY));

        panel.add(metricsGrid, BorderLayout.NORTH);

        // Lower split: Class Occupancy on Left, Train & Route Highlights on Right
        JPanel detailsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        detailsPanel.setOpaque(false);

        // Class Occupancy Table
        JPanel classCard = ModernTheme.createCardPanel();
        classCard.setLayout(new BorderLayout(0, 8));
        JLabel lblClassTitle = new JLabel("CLASS-WISE PASSENGER OCCUPANCY BREAKDOWN");
        lblClassTitle.setFont(ModernTheme.FONT_SUBHEADER);
        lblClassTitle.setForeground(ModernTheme.PRIMARY);
        classCard.add(lblClassTitle, BorderLayout.NORTH);

        String[] classCols = {"Class Code", "Travel Class", "Active Booked Passengers"};
        classOccupancyModel = new DefaultTableModel(classCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable classTable = new JTable(classOccupancyModel);
        ModernTheme.styleTable(classTable);
        classCard.add(new JScrollPane(classTable), BorderLayout.CENTER);
        detailsPanel.add(classCard);

        // Highlights & Export Card
        JPanel reportCard = ModernTheme.createCardPanel();
        reportCard.setLayout(new BoxLayout(reportCard, BoxLayout.Y_AXIS));

        JLabel lblRepTitle = new JLabel("SYSTEM HIGHLIGHTS");
        lblRepTitle.setFont(ModernTheme.FONT_SUBHEADER);
        lblRepTitle.setForeground(ModernTheme.PRIMARY);
        lblRepTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        reportCard.add(lblRepTitle);
        reportCard.add(Box.createVerticalStrut(16));

        lblMetricTopTrain = new JLabel("Most Occupied Train: N/A");
        lblMetricTopTrain.setFont(ModernTheme.FONT_BODY_BOLD);
        lblMetricTopTrain.setAlignmentX(Component.LEFT_ALIGNMENT);
        reportCard.add(lblMetricTopTrain);
        reportCard.add(Box.createVerticalStrut(12));

        lblMetricPopularRoute = new JLabel("Most Popular Route: N/A");
        lblMetricPopularRoute.setFont(ModernTheme.FONT_BODY_BOLD);
        lblMetricPopularRoute.setAlignmentX(Component.LEFT_ALIGNMENT);
        reportCard.add(lblMetricPopularRoute);
        reportCard.add(Box.createVerticalStrut(24));

        JButton btnExportAudit = ModernTheme.createAccentButton("Export Summary Report (.txt)");
        btnExportAudit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExportAudit.addActionListener(e -> exportAdminReport());
        reportCard.add(btnExportAudit);

        detailsPanel.add(reportCard);
        panel.add(detailsPanel, BorderLayout.CENTER);

        refreshAnalyticsTab();
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 4));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(ModernTheme.FONT_SMALL);
        titleLbl.setForeground(ModernTheme.TEXT_MUTED);

        valueLabel.setFont(ModernTheme.FONT_TITLE);
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void refreshAnalyticsTab() {
        if (lblMetricTrains == null) return;
        lblMetricTrains.setText(String.valueOf(analyticsService.getTotalTrains()));
        lblMetricBookings.setText(String.valueOf(analyticsService.getTotalBookings()));
        lblMetricConfirmed.setText(String.valueOf(analyticsService.getConfirmedTicketsCount()));
        lblMetricGrossRevenue.setText(String.format("₹ %.2f", analyticsService.getTotalGrossRevenue()));

        lblMetricRac.setText(String.valueOf(analyticsService.getRacTicketsCount()));
        lblMetricWl.setText(String.valueOf(analyticsService.getWaitingListTicketsCount()));
        lblMetricCancelled.setText(String.valueOf(analyticsService.getCancelledTicketsCount()));
        lblMetricAvgOccupancy.setText(String.format("%.1f%%", analyticsService.getAverageOccupancyPercentage()));

        lblMetricTopTrain.setText("Most Occupied Train: " + analyticsService.getMostOccupiedTrain());
        lblMetricPopularRoute.setText("Most Popular Route:  " + analyticsService.getMostPopularRoute());

        classOccupancyModel.setRowCount(0);
        Map<SeatClass, Integer> map = analyticsService.getClassWiseOccupancy();
        for (Map.Entry<SeatClass, Integer> entry : map.entrySet()) {
            classOccupancyModel.addRow(new Object[]{
                    entry.getKey().getCode(),
                    entry.getKey().getDisplayName(),
                    entry.getValue()
            });
        }
    }

    private void exportAdminReport() {
        try {
            String report = ReportGenerator.generateAdminReport(analyticsService);
            File dest = new File("data/Railway_Summary_Report.txt");
            com.railsync.manager.FileManager.writeTextFile(report, dest);

            JTextArea area = new JTextArea(report);
            area.setFont(ModernTheme.FONT_MONO);
            area.setEditable(false);
            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(650, 420));

            JOptionPane.showMessageDialog(this, sp, "Summary Report Exported to " + dest.getName(), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= 2. Master Bookings & Passenger Search =================

    private JPanel createMasterBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Filter Bar
        JPanel filterCard = ModernTheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        JLabel lblSearch = new JLabel("Search by PNR, Passenger Name, Phone, or Train No:");
        lblSearch.setFont(ModernTheme.FONT_BODY_BOLD);
        txtGlobalSearch = new JTextField(20);
        txtGlobalSearch.setFont(ModernTheme.FONT_BODY);

        JButton btnFilter = ModernTheme.createPrimaryButton("Search");
        btnFilter.addActionListener(e -> executeMasterFilter());

        JButton btnReset = ModernTheme.createSecondaryButton("Show All");
        btnReset.addActionListener(e -> {
            txtGlobalSearch.setText("");
            refreshMasterBookings();
        });

        filterCard.add(lblSearch);
        filterCard.add(txtGlobalSearch);
        filterCard.add(btnFilter);
        filterCard.add(btnReset);
        panel.add(filterCard, BorderLayout.NORTH);

        // Master Bookings Table
        JPanel tableCard = ModernTheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        String[] cols = {"PNR", "Booking ID", "Train", "Route", "Date", "Class", "Passengers", "Status", "Total Fare (₹)", "Booked By"};
        masterBookingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        masterBookingsTable = new JTable(masterBookingsModel);
        ModernTheme.styleTable(masterBookingsTable);
        tableCard.add(new JScrollPane(masterBookingsTable), BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        refreshMasterBookings();
        return panel;
    }

    private void refreshMasterBookings() {
        if (masterBookingsModel == null) return;
        masterBookingsModel.setRowCount(0);
        for (Booking b : manager.getBookingMap().values()) {
            masterBookingsModel.addRow(new Object[]{
                    b.getPnr(),
                    b.getBookingId(),
                    b.getTrainNumber() + " - " + b.getTrainName(),
                    b.getSource().getCode() + " ➔ " + b.getDestination().getCode(),
                    b.getFormattedJourneyDate(),
                    b.getSeatClass().getCode(),
                    b.getPassengerCount(),
                    b.isCancelled() ? "CANCELLED" : "ACTIVE",
                    String.format("%.2f", b.getTotalFare()),
                    b.getBookedByUserId()
            });
        }
    }

    private void executeMasterFilter() {
        String q = txtGlobalSearch.getText().trim();
        if (q.isEmpty()) {
            refreshMasterBookings();
            return;
        }

        masterBookingsModel.setRowCount(0);
        for (Booking b : manager.getBookingMap().values()) {
            boolean match = b.getPnr().equalsIgnoreCase(q) ||
                    b.getBookingId().equalsIgnoreCase(q) ||
                    b.getTrainNumber().equalsIgnoreCase(q) ||
                    b.getBookedByUserId().equalsIgnoreCase(q) ||
                    b.getTickets().stream().anyMatch(t ->
                            t.getPassenger().getName().toLowerCase().contains(q.toLowerCase()) ||
                                    t.getPassenger().getPhone().contains(q));

            if (match) {
                masterBookingsModel.addRow(new Object[]{
                        b.getPnr(),
                        b.getBookingId(),
                        b.getTrainNumber() + " - " + b.getTrainName(),
                        b.getSource().getCode() + " ➔ " + b.getDestination().getCode(),
                        b.getFormattedJourneyDate(),
                        b.getSeatClass().getCode(),
                        b.getPassengerCount(),
                        b.isCancelled() ? "CANCELLED" : "ACTIVE",
                        String.format("%.2f", b.getTotalFare()),
                        b.getBookedByUserId()
                });
            }
        }
    }

    // ================= 3. Queue Monitor Tab =================

    private JPanel createQueueMonitorTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel selectorCard = ModernTheme.createCardPanel();
        selectorCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 6));

        JLabel lblTrn = new JLabel("Select Train:");
        lblTrn.setFont(ModernTheme.FONT_BODY_BOLD);
        cbQueueTrain = new JComboBox<>(manager.getAllTrains().toArray(new Train[0]));
        cbQueueTrain.setFont(ModernTheme.FONT_BODY);

        JLabel lblCls = new JLabel("Select Class:");
        lblCls.setFont(ModernTheme.FONT_BODY_BOLD);
        cbQueueClass = new JComboBox<>(SeatClass.values());
        cbQueueClass.setFont(ModernTheme.FONT_BODY);

        JButton btnInspect = ModernTheme.createPrimaryButton("Inspect Queues");
        btnInspect.addActionListener(e -> refreshQueueInspector());

        selectorCard.add(lblTrn);
        selectorCard.add(cbQueueTrain);
        selectorCard.add(lblCls);
        selectorCard.add(cbQueueClass);
        selectorCard.add(btnInspect);

        panel.add(selectorCard, BorderLayout.NORTH);

        txtQueueInspector = new JTextArea();
        txtQueueInspector.setFont(ModernTheme.FONT_MONO);
        txtQueueInspector.setEditable(false);
        txtQueueInspector.setBackground(Color.WHITE);

        JScrollPane sp = new JScrollPane(txtQueueInspector);
        sp.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR));
        panel.add(sp, BorderLayout.CENTER);

        refreshQueueInspector();
        return panel;
    }

    private void refreshQueueInspector() {
        if (txtQueueInspector == null || cbQueueTrain.getSelectedItem() == null) return;

        Train train = (Train) cbQueueTrain.getSelectedItem();
        SeatClass sc = (SeatClass) cbQueueClass.getSelectedItem();
        java.time.LocalDate tomorrow = java.time.LocalDate.now().plusDays(1);

        String key = manager.getWaitingListManager().makeQueueKey(train.getTrainNumber(), sc, tomorrow);
        Queue<Ticket> racQueue = manager.getWaitingListManager().getRacQueue(key);
        Queue<Ticket> wlQueue = manager.getWaitingListManager().getWaitingListQueue(key);

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append(String.format(" LIVE QUEUE INSPECTOR: %s (%s) - Class: %s (Date: %s)\n",
                train.getTrainNumber(), train.getTrainName(), sc.getDisplayName(), tomorrow));
        sb.append("========================================================================================\n\n");

        sb.append("1. RESERVATION AGAINST CANCELLATION (RAC) QUEUE [Length: " + racQueue.size() + "]\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        if (racQueue.isEmpty()) {
            sb.append(" [No passengers currently waiting in RAC queue for this date/class]\n");
        } else {
            int pos = 1;
            for (Ticket t : racQueue) {
                sb.append(String.format("  Position RAC %-2d | PNR: %-8s | Ticket: %-12s | Passenger: %-20s (Age: %d)\n",
                        pos++, t.getPnr(), t.getTicketId(), t.getPassenger().getName(), t.getPassenger().getAge()));
            }
        }

        sb.append("\n2. WAITING LIST (WL) QUEUE [Length: " + wlQueue.size() + "]\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        if (wlQueue.isEmpty()) {
            sb.append(" [No passengers currently waiting in Waiting List queue for this date/class]\n");
        } else {
            int pos = 1;
            for (Ticket t : wlQueue) {
                sb.append(String.format("  Position WL %-2d  | PNR: %-8s | Ticket: %-12s | Passenger: %-20s (Age: %d)\n",
                        pos++, t.getPnr(), t.getTicketId(), t.getPassenger().getName(), t.getPassenger().getAge()));
            }
        }

        sb.append("\n========================================================================================\n");
        txtQueueInspector.setText(sb.toString());
        txtQueueInspector.setCaretPosition(0);
    }

    // ================= 4. Fleet & Route Management =================

    private JPanel createFleetManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top Action Bar
        JPanel topBar = ModernTheme.createCardPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        JButton btnAddTrain = ModernTheme.createPrimaryButton("+ Add New Train");
        btnAddTrain.addActionListener(e -> showAddTrainDialog());

        JButton btnUpdateRate = ModernTheme.createSecondaryButton("Configure Base Fare Rate");
        btnUpdateRate.addActionListener(e -> showUpdateRateDialog());

        JButton btnRemoveTrain = ModernTheme.createDangerButton("Remove Train");
        btnRemoveTrain.addActionListener(e -> executeRemoveTrain());

        topBar.add(btnAddTrain);
        topBar.add(btnUpdateRate);
        topBar.add(btnRemoveTrain);
        panel.add(topBar, BorderLayout.NORTH);

        // Train Table
        JPanel tableCard = ModernTheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        String[] cols = {"Train No", "Train Name", "Category", "Source", "Destination", "Dep Time", "Arr Time", "Distance (km)", "Base Rate/km", "Total Coaches"};
        trainModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        trainTable = new JTable(trainModel);
        ModernTheme.styleTable(trainTable);
        tableCard.add(new JScrollPane(trainTable), BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        refreshFleetTable();
        return panel;
    }

    private void refreshFleetTable() {
        if (trainModel == null) return;
        trainModel.setRowCount(0);
        for (Train t : manager.getAllTrains()) {
            trainModel.addRow(new Object[]{
                    t.getTrainNumber(),
                    t.getTrainName(),
                    t.getTrainType(),
                    t.getSource().getCode(),
                    t.getDestination().getCode(),
                    t.getDepartureTime().toString(),
                    t.getArrivalTime().toString(),
                    String.format("%.0f", t.getDistanceKm()),
                    String.format("₹ %.2f", t.getBaseFareRatePerKm()),
                    t.getCoaches().size()
            });
        }
    }

    private void showAddTrainDialog() {
        JTextField txtNo = new JTextField("12999");
        JTextField txtName = new JTextField("Deccan Superfast");
        JComboBox<Station> cbSrc = new JComboBox<>(manager.getAllStations().toArray(new Station[0]));
        JComboBox<Station> cbDst = new JComboBox<>(manager.getAllStations().toArray(new Station[0]));
        if (cbDst.getItemCount() > 2) cbDst.setSelectedIndex(2);
        JTextField txtDep = new JTextField("08:00");
        JTextField txtArr = new JTextField("18:30");
        JTextField txtDist = new JTextField("850");

        Object[] fields = {
                "Train Number:", txtNo,
                "Train Name:", txtName,
                "Source Station:", cbSrc,
                "Destination Station:", cbDst,
                "Departure Time (HH:mm):", txtDep,
                "Arrival Time (HH:mm):", txtArr,
                "Distance (km):", txtDist
        };

        int res = JOptionPane.showConfirmDialog(this, fields, "Add New Train", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Station s1 = (Station) cbSrc.getSelectedItem();
                Station s2 = (Station) cbDst.getSelectedItem();
                if (s1.equals(s2)) {
                    JOptionPane.showMessageDialog(this, "Source and destination cannot be the same.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                SuperfastExpress newTrain = new SuperfastExpress(
                        txtNo.getText().trim(),
                        txtName.getText().trim(),
                        s1, s2,
                        LocalTime.parse(txtDep.getText().trim()),
                        LocalTime.parse(txtArr.getText().trim()),
                        Double.parseDouble(txtDist.getText().trim())
                );
                // Add standard coach complement
                newTrain.addCoach(new Coach("S1", SeatClass.SLEEPER, 60, 12, 20));
                newTrain.addCoach(new Coach("B1", SeatClass.THIRD_AC, 48, 8, 15));
                manager.addTrain(newTrain);

                refreshFleetTable();
                JOptionPane.showMessageDialog(this, "Train " + newTrain.getTrainNumber() + " added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to add train: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showUpdateRateDialog() {
        int sel = trainTable.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Select a train to configure its base fare rate.");
            return;
        }
        String trainNo = String.valueOf(trainTable.getValueAt(sel, 0));
        Train t = manager.getTrain(trainNo);
        if (t == null) return;

        String input = JOptionPane.showInputDialog(this,
                "Enter new base fare rate per km for " + t.getTrainName() + ":",
                String.valueOf(t.getBaseFareRatePerKm()));

        if (input != null && !input.trim().isEmpty()) {
            try {
                double rate = Double.parseDouble(input.trim());
                t.setBaseFareRatePerKm(rate);
                refreshFleetTable();
                JOptionPane.showMessageDialog(this, "Fare rate updated successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void executeRemoveTrain() {
        int sel = trainTable.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Select a train to remove.");
            return;
        }
        String trainNo = String.valueOf(trainTable.getValueAt(sel, 0));
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove train " + trainNo + "?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            manager.removeTrain(trainNo);
            refreshFleetTable();
        }
    }
}
