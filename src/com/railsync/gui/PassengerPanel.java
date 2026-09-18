package com.railsync.gui;

import com.railsync.exception.RailSyncException;
import com.railsync.manager.ReservationManager;
import com.railsync.model.*;
import com.railsync.service.DynamicFareCalculator;
import com.railsync.service.ReportGenerator;
import com.railsync.service.SearchService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Passenger panel for searching trains, booking tickets,
 * checking PNR status, and cancelling bookings.
 */
public class PassengerPanel extends JPanel {

    private final ReservationManager manager;
    private final SearchService searchService;
    private User currentUser;

    // Search & Book Components
    private JComboBox<Station> cbSource;
    private JComboBox<Station> cbDestination;
    private JComboBox<SeatClass> cbClass;
    private JTextField txtJourneyDate;
    private JTable trainTable;
    private DefaultTableModel trainTableModel;
    private List<Train> currentSearchResults;

    // Multi-passenger table
    private JTable passengerTable;
    private DefaultTableModel passengerTableModel;
    private JLabel lblTotalFare;

    // PNR Enquiry Components
    private JTextField txtPnrInput;
    private JTextArea txtTicketPreview;
    private Booking currentEnquiredBooking;

    // My Bookings Components
    private JTable myBookingsTable;
    private DefaultTableModel myBookingsModel;
    private JTable bookingTicketsTable;
    private DefaultTableModel bookingTicketsModel;
    private List<Booking> userBookings;

    public PassengerPanel(ReservationManager manager, User currentUser) {
        this.manager = manager;
        this.currentUser = currentUser;
        this.searchService = new SearchService(manager.getAllTrains(), manager.getBookingMap(), manager.getFareCalculator());
        this.currentSearchResults = new ArrayList<>();
        this.userBookings = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_MAIN);
        initUI();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        refreshMyBookings();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernTheme.FONT_SUBHEADER);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Train Search & Book", createSearchAndBookPanel());
        tabbedPane.addTab("PNR Status Enquiry", createPnrEnquiryPanel());
        tabbedPane.addTab("My Bookings & Cancellation", createMyBookingsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ================= 1. Search & Book Panel =================

    private JPanel createSearchAndBookPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top Search Card
        JPanel searchCard = ModernTheme.createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 8));

        JLabel lblFrom = new JLabel("From:");
        lblFrom.setFont(ModernTheme.FONT_BODY_BOLD);
        cbSource = new JComboBox<>(manager.getAllStations().toArray(new Station[0]));
        cbSource.setFont(ModernTheme.FONT_BODY);

        JLabel lblTo = new JLabel("To:");
        lblTo.setFont(ModernTheme.FONT_BODY_BOLD);
        cbDestination = new JComboBox<>(manager.getAllStations().toArray(new Station[0]));
        cbDestination.setFont(ModernTheme.FONT_BODY);
        if (cbDestination.getItemCount() > 1) {
            cbDestination.setSelectedIndex(1);
        }

        JLabel lblDate = new JLabel("Journey Date (YYYY-MM-DD):");
        lblDate.setFont(ModernTheme.FONT_BODY_BOLD);
        txtJourneyDate = new JTextField(LocalDate.now().plusDays(1).toString(), 10);
        txtJourneyDate.setFont(ModernTheme.FONT_BODY);

        JLabel lblCls = new JLabel("Class:");
        lblCls.setFont(ModernTheme.FONT_BODY_BOLD);
        cbClass = new JComboBox<>(SeatClass.values());
        cbClass.setFont(ModernTheme.FONT_BODY);

        JButton btnSearch = ModernTheme.createPrimaryButton("Search Trains");
        btnSearch.addActionListener(e -> performTrainSearch());

        searchCard.add(lblFrom);
        searchCard.add(cbSource);
        searchCard.add(lblTo);
        searchCard.add(cbDestination);
        searchCard.add(lblDate);
        searchCard.add(txtJourneyDate);
        searchCard.add(lblCls);
        searchCard.add(cbClass);
        searchCard.add(btnSearch);

        panel.add(searchCard, BorderLayout.NORTH);

        // Center SplitPane: Trains Table on Top, Passenger details & booking on Bottom
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);

        // Trains Table Panel
        JPanel trainsPanel = ModernTheme.createCardPanel();
        trainsPanel.setLayout(new BorderLayout(0, 8));
        JLabel lblAvailableTrains = new JLabel("AVAILABLE TRAINS ON ROUTE");
        lblAvailableTrains.setFont(ModernTheme.FONT_SUBHEADER);
        lblAvailableTrains.setForeground(ModernTheme.PRIMARY);
        trainsPanel.add(lblAvailableTrains, BorderLayout.NORTH);

        String[] trainCols = {"Train No", "Train Name", "Category", "Departure", "Arrival", "Duration", "Confirmed Seats", "RAC Capacity", "WL Capacity", "Fare (₹)"};
        trainTableModel = new DefaultTableModel(trainCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        trainTable = new JTable(trainTableModel);
        ModernTheme.styleTable(trainTable);
        trainTable.getSelectionModel().addListSelectionListener(e -> updateFarePreview());
        trainsPanel.add(new JScrollPane(trainTable), BorderLayout.CENTER);

        splitPane.setTopComponent(trainsPanel);

        // Bottom Booking & Passenger Details Panel
        JPanel bookingPanel = ModernTheme.createCardPanel();
        bookingPanel.setLayout(new BorderLayout(0, 10));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel lblPassManifest = new JLabel("PASSENGERS TO BOOK (Max 6 per booking)");
        lblPassManifest.setFont(ModernTheme.FONT_SUBHEADER);
        lblPassManifest.setForeground(ModernTheme.PRIMARY);

        JPanel passBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        passBtnRow.setOpaque(false);
        JButton btnAddPass = ModernTheme.createSecondaryButton("+ Add Passenger");
        JButton btnRemPass = ModernTheme.createSecondaryButton("- Remove Selected");
        passBtnRow.add(btnAddPass);
        passBtnRow.add(btnRemPass);

        headerRow.add(lblPassManifest, BorderLayout.WEST);
        headerRow.add(passBtnRow, BorderLayout.EAST);
        bookingPanel.add(headerRow, BorderLayout.NORTH);

        String[] passCols = {"S.No", "Name", "Age", "Gender", "Phone", "Berth Preference", "Concession"};
        passengerTableModel = new DefaultTableModel(passCols, 0);
        // Default with current user
        passengerTableModel.addRow(new Object[]{1, currentUser != null ? currentUser.getFullName() : "Passenger", 30, "Male", "9876543210", BerthType.LOWER, Passenger.Concession.NONE});

        passengerTable = new JTable(passengerTableModel);
        ModernTheme.styleTable(passengerTable);
        bookingPanel.add(new JScrollPane(passengerTable), BorderLayout.CENTER);

        btnAddPass.addActionListener(e -> {
            if (passengerTableModel.getRowCount() >= 6) {
                JOptionPane.showMessageDialog(this, "Maximum 6 passengers allowed per booking.", "Limit Reached", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int next = passengerTableModel.getRowCount() + 1;
            passengerTableModel.addRow(new Object[]{next, "Passenger " + next, 28, "Female", "9876543210", BerthType.MIDDLE, Passenger.Concession.NONE});
            updateFarePreview();
        });

        btnRemPass.addActionListener(e -> {
            int sel = passengerTable.getSelectedRow();
            if (sel >= 0 && passengerTableModel.getRowCount() > 1) {
                passengerTableModel.removeRow(sel);
                // Re-number
                for (int i = 0; i < passengerTableModel.getRowCount(); i++) {
                    passengerTableModel.setValueAt(i + 1, i, 0);
                }
                updateFarePreview();
            } else if (passengerTableModel.getRowCount() <= 1) {
                JOptionPane.showMessageDialog(this, "Booking must have at least one passenger.", "Validation", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Bottom Action Bar
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);

        lblTotalFare = new JLabel("Estimated Total Fare: ₹ 0.00");
        lblTotalFare.setFont(ModernTheme.FONT_HEADER);
        lblTotalFare.setForeground(ModernTheme.PRIMARY);

        JButton btnBookNow = ModernTheme.createSuccessButton("Confirm & Book Ticket");
        btnBookNow.setFont(ModernTheme.FONT_HEADER);
        btnBookNow.addActionListener(e -> executeBooking());

        actionBar.add(lblTotalFare, BorderLayout.WEST);
        actionBar.add(btnBookNow, BorderLayout.EAST);
        bookingPanel.add(actionBar, BorderLayout.SOUTH);

        splitPane.setBottomComponent(bookingPanel);
        panel.add(splitPane, BorderLayout.CENTER);

        // Perform initial search
        performTrainSearch();
        return panel;
    }

    private void performTrainSearch() {
        Station src = (Station) cbSource.getSelectedItem();
        Station dst = (Station) cbDestination.getSelectedItem();
        SeatClass sc = (SeatClass) cbClass.getSelectedItem();

        if (src == null || dst == null) return;
        if (src.equals(dst)) {
            JOptionPane.showMessageDialog(this, "Source and Destination cannot be the same station!", "Invalid Search", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(txtJourneyDate.getText().trim());
            trainTableModel.setRowCount(0);
            currentSearchResults = searchService.searchTrains(src, dst, date, sc);

            for (Train t : currentSearchResults) {
                int avail = t.getAvailableConfirmedSeats(sc);
                int rac = t.getRacCapacity(sc);
                int wl = t.getWaitingListCapacity(sc);
                double baseFare = manager.getFareCalculator().calculateFare(t, sc,
                        new Passenger("Sample", 30, "Male", "9876543210", BerthType.LOWER));

                trainTableModel.addRow(new Object[]{
                        t.getTrainNumber(),
                        t.getTrainName(),
                        t.getTrainType(),
                        t.getDepartureTime().toString(),
                        t.getArrivalTime().toString(),
                        t.getFormattedDuration(),
                        avail > 0 ? avail + " Available" : "Full (RAC available)",
                        rac,
                        wl,
                        String.format("%.2f", baseFare)
                });
            }

            if (trainTableModel.getRowCount() > 0) {
                trainTable.setRowSelectionInterval(0, 0);
            }
            updateFarePreview();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error in search: " + ex.getMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFarePreview() {
        int sel = trainTable.getSelectedRow();
        if (sel < 0 || sel >= currentSearchResults.size()) {
            lblTotalFare.setText("Estimated Total Fare: ₹ 0.00");
            return;
        }
        Train train = currentSearchResults.get(sel);
        SeatClass sc = (SeatClass) cbClass.getSelectedItem();

        double total = 0.0;
        for (int i = 0; i < passengerTableModel.getRowCount(); i++) {
            try {
                String name = String.valueOf(passengerTableModel.getValueAt(i, 1));
                int age = Integer.parseInt(String.valueOf(passengerTableModel.getValueAt(i, 2)));
                String gender = String.valueOf(passengerTableModel.getValueAt(i, 3));
                Passenger.Concession conc = Passenger.determineAutoConcession(age, gender);
                Passenger p = new Passenger(name, age, gender, "9876543210", conc, BerthType.LOWER);
                total += manager.getFareCalculator().calculateFare(train, sc, p);
            } catch (Exception ignored) {}
        }
        lblTotalFare.setText(String.format("Estimated Total Fare: ₹ %.2f (%d Passengers)", total, passengerTableModel.getRowCount()));
    }

    private void executeBooking() {
        int sel = trainTable.getSelectedRow();
        if (sel < 0 || sel >= currentSearchResults.size()) {
            JOptionPane.showMessageDialog(this, "Please select a train from the search table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Train train = currentSearchResults.get(sel);
        SeatClass sc = (SeatClass) cbClass.getSelectedItem();

        List<Passenger> passengers = new ArrayList<>();
        for (int i = 0; i < passengerTableModel.getRowCount(); i++) {
            try {
                String name = String.valueOf(passengerTableModel.getValueAt(i, 1)).trim();
                int age = Integer.parseInt(String.valueOf(passengerTableModel.getValueAt(i, 2)).trim());
                String gender = String.valueOf(passengerTableModel.getValueAt(i, 3)).trim();
                String phone = String.valueOf(passengerTableModel.getValueAt(i, 4)).trim();
                BerthType bPref = (BerthType) passengerTableModel.getValueAt(i, 5);
                Passenger.Concession conc = Passenger.determineAutoConcession(age, gender);

                passengers.add(new Passenger(name, age, gender, phone, conc, bPref));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid passenger at row " + (i + 1) + ": " + ex.getMessage(),
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            LocalDate date = LocalDate.parse(txtJourneyDate.getText().trim());
            String userId = currentUser != null ? currentUser.getUsername() : "guest";

            Booking booking = manager.bookTicket(train.getTrainNumber(), date, sc, passengers, userId);

            // Refresh UI
            performTrainSearch();
            refreshMyBookings();

            // Display success modal
            String summaryMsg = String.format("<html><body><h3>Booking Successful!</h3>" +
                            "<p><b>PNR:</b> <font color='#16A34A' size='+1'>%s</font></p>" +
                            "<p><b>Train:</b> %s (%s)</p>" +
                            "<p><b>Confirmed:</b> %d | <b>RAC:</b> %d | <b>Waiting List:</b> %d</p>" +
                            "<p><b>Total Fare Paid:</b> ₹ %.2f</p>" +
                            "<p>Would you like to view the full Electronic Ticket slip now?</p></body></html>",
                    booking.getPnr(), booking.getTrainName(), booking.getTrainNumber(),
                    booking.getConfirmedCount(), booking.getRacCount(), booking.getWaitingListCount(),
                    booking.getTotalFare());

            int choice = JOptionPane.showConfirmDialog(this, summaryMsg, "Booking Confirmed",
                    JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                displayTicketInEnquiry(booking);
            }

        } catch (RailSyncException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= 2. PNR Status Enquiry Panel =================

    private JPanel createPnrEnquiryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = ModernTheme.createCardPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        JLabel lblPrompt = new JLabel("Enter PNR Number (e.g. RS482731):");
        lblPrompt.setFont(ModernTheme.FONT_BODY_BOLD);
        txtPnrInput = new JTextField(12);
        txtPnrInput.setFont(ModernTheme.FONT_HEADER);

        JButton btnEnquire = ModernTheme.createPrimaryButton("Get Current Status");
        btnEnquire.addActionListener(e -> checkPnrStatus());

        JButton btnExport = ModernTheme.createSecondaryButton("Export Ticket to File (.txt)");
        btnExport.addActionListener(e -> exportCurrentTicket());

        topBar.add(lblPrompt);
        topBar.add(txtPnrInput);
        topBar.add(btnEnquire);
        topBar.add(btnExport);

        panel.add(topBar, BorderLayout.NORTH);

        txtTicketPreview = new JTextArea();
        txtTicketPreview.setFont(ModernTheme.FONT_MONO);
        txtTicketPreview.setEditable(false);
        txtTicketPreview.setBackground(Color.WHITE);
        txtTicketPreview.setText("\n  Enter a valid 8-character PNR number above to query the reservation manifest.");

        JScrollPane scroll = new JScrollPane(txtTicketPreview);
        scroll.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void checkPnrStatus() {
        String pnr = txtPnrInput.getText().trim().toUpperCase();
        if (pnr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid PNR.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Booking b = manager.getBookingMap().get(pnr);
        if (b != null) {
            displayTicketInEnquiry(b);
        } else {
            txtTicketPreview.setText("\n  NO RECORD FOUND FOR PNR: " + pnr + "\n  Please verify the number and try again.");
            currentEnquiredBooking = null;
        }
    }

    private void displayTicketInEnquiry(Booking booking) {
        this.currentEnquiredBooking = booking;
        this.txtPnrInput.setText(booking.getPnr());
        String ticketText = ReportGenerator.generateTicketText(booking);
        txtTicketPreview.setText(ticketText);
        txtTicketPreview.setCaretPosition(0);
    }

    private void exportCurrentTicket() {
        if (currentEnquiredBooking == null) {
            JOptionPane.showMessageDialog(this, "Please search and load a valid ticket first.", "No Ticket Loaded", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            File exportDir = new File("data/tickets");
            File savedFile = ReportGenerator.saveTicketToFile(currentEnquiredBooking, exportDir);
            JOptionPane.showMessageDialog(this, "Ticket slip successfully saved to:\n" + savedFile.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export ticket: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= 3. My Bookings & Cancellation Panel =================

    private JPanel createMyBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);

        // Top: User's Bookings
        JPanel bookingsCard = ModernTheme.createCardPanel();
        bookingsCard.setLayout(new BorderLayout(0, 8));
        JLabel lblTitle = new JLabel("MY BOOKING TRANSACTIONS");
        lblTitle.setFont(ModernTheme.FONT_SUBHEADER);
        lblTitle.setForeground(ModernTheme.PRIMARY);
        bookingsCard.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"PNR", "Booking ID", "Train", "Route", "Date", "Class", "Passengers", "Status", "Fare (₹)"};
        myBookingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        myBookingsTable = new JTable(myBookingsModel);
        ModernTheme.styleTable(myBookingsTable);
        myBookingsTable.getSelectionModel().addListSelectionListener(e -> loadSelectedBookingTickets());
        bookingsCard.add(new JScrollPane(myBookingsTable), BorderLayout.CENTER);
        splitPane.setTopComponent(bookingsCard);

        // Bottom: Selected Booking's Passengers & Cancel Button
        JPanel ticketDetailCard = ModernTheme.createCardPanel();
        ticketDetailCard.setLayout(new BorderLayout(0, 8));

        JPanel detailHeader = new JPanel(new BorderLayout());
        detailHeader.setOpaque(false);
        JLabel lblPassTitle = new JLabel("PASSENGERS & STATUS IN SELECTED BOOKING");
        lblPassTitle.setFont(ModernTheme.FONT_SUBHEADER);
        lblPassTitle.setForeground(ModernTheme.PRIMARY);

        JButton btnCancelTicket = ModernTheme.createDangerButton("Cancel Selected Passenger Ticket");
        btnCancelTicket.addActionListener(e -> executeCancellation());

        detailHeader.add(lblPassTitle, BorderLayout.WEST);
        detailHeader.add(btnCancelTicket, BorderLayout.EAST);
        ticketDetailCard.add(detailHeader, BorderLayout.NORTH);

        String[] tCols = {"Ticket ID", "Passenger Name", "Age", "Gender", "Current Status", "Berth / Queue", "Fare (₹)"};
        bookingTicketsModel = new DefaultTableModel(tCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingTicketsTable = new JTable(bookingTicketsModel);
        ModernTheme.styleTable(bookingTicketsTable);
        ticketDetailCard.add(new JScrollPane(bookingTicketsTable), BorderLayout.CENTER);

        splitPane.setBottomComponent(ticketDetailCard);
        panel.add(splitPane, BorderLayout.CENTER);

        refreshMyBookings();
        return panel;
    }

    public void refreshMyBookings() {
        if (myBookingsModel == null) return;
        myBookingsModel.setRowCount(0);
        bookingTicketsModel.setRowCount(0);

        String userId = currentUser != null ? currentUser.getUsername() : "passenger";
        userBookings = searchService.searchBookingsByUser(userId);

        for (Booking b : userBookings) {
            myBookingsModel.addRow(new Object[]{
                    b.getPnr(),
                    b.getBookingId(),
                    b.getTrainNumber() + " - " + b.getTrainName(),
                    b.getSource().getCode() + " ➔ " + b.getDestination().getCode(),
                    b.getFormattedJourneyDate(),
                    b.getSeatClass().getCode(),
                    b.getPassengerCount(),
                    b.isCancelled() ? "CANCELLED" : "ACTIVE",
                    String.format("%.2f", b.getTotalFare())
            });
        }

        if (myBookingsModel.getRowCount() > 0) {
            myBookingsTable.setRowSelectionInterval(0, 0);
        }
    }

    private void loadSelectedBookingTickets() {
        int sel = myBookingsTable.getSelectedRow();
        if (sel < 0 || sel >= userBookings.size()) {
            bookingTicketsModel.setRowCount(0);
            return;
        }
        Booking b = userBookings.get(sel);
        bookingTicketsModel.setRowCount(0);

        for (Ticket t : b.getTickets()) {
            bookingTicketsModel.addRow(new Object[]{
                    t.getTicketId(),
                    t.getPassenger().getName(),
                    t.getPassenger().getAge(),
                    t.getPassenger().getGender(),
                    t.isCancelled() ? "CANCELLED" : t.getStatus().name(),
                    t.getStatusDescription(),
                    String.format("%.2f", t.getIndividualFare())
            });
        }
    }

    private void executeCancellation() {
        int bSel = myBookingsTable.getSelectedRow();
        int tSel = bookingTicketsTable.getSelectedRow();

        if (bSel < 0 || bSel >= userBookings.size()) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the top table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tSel < 0) {
            JOptionPane.showMessageDialog(this, "Please select the passenger ticket to cancel from the bottom table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Booking booking = userBookings.get(bSel);
        Ticket ticket = booking.getTickets().get(tSel);

        if (ticket.isCancelled()) {
            JOptionPane.showMessageDialog(this, "This passenger ticket is already cancelled.", "Already Cancelled", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel the ticket for '" + ticket.getPassenger().getName() + "'?\n" +
                        "Seat/Status: " + ticket.getStatusDescription() + "\n" +
                        "This will automatically promote the next passenger in the RAC/Waiting List queue.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            RefundReceipt receipt = manager.cancelTicket(booking.getPnr(), ticket.getTicketId());

            // Refresh views
            refreshMyBookings();
            performTrainSearch();

            // Display formal refund receipt
            JTextArea receiptArea = new JTextArea(receipt.getFormattedReceipt());
            receiptArea.setFont(ModernTheme.FONT_MONO);
            receiptArea.setEditable(false);
            JScrollPane scroll = new JScrollPane(receiptArea);
            scroll.setPreferredSize(new Dimension(520, 360));

            JOptionPane.showMessageDialog(this, scroll, "Official Refund Receipt", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cancellation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
