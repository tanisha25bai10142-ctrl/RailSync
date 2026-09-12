package com.railsync.cli;

import com.railsync.exception.RailSyncException;
import com.railsync.manager.FileManager;
import com.railsync.manager.ReservationManager;
import com.railsync.model.*;
import com.railsync.service.SearchService;
import com.railsync.util.SampleDataSeeder;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Interactive Command-Line Interface (CLI) for RailSync.
 * Demonstrates:
 * - Pure terminal execution without requiring an IDE or GUI environment.
 * - Train searching and route filtering using existing services.
 * - Passenger registration and dynamic seat booking (Confirmed, RAC, WL).
 * - PNR status inquiry and ticket cancellation with automatic promotion cascade.
 * - Dynamic fare computation with age-based concessions.
 */
public class ConsoleApp {

    private static final String DEFAULT_DATA_PATH = "data/railsync_data.ser";

    private final ReservationManager manager;
    private final SearchService searchService;
    private final File dataFile;
    private final Scanner scanner;

    public ConsoleApp(ReservationManager manager, File dataFile, Scanner scanner) {
        this.manager = manager;
        this.dataFile = dataFile;
        this.scanner = scanner;
        this.searchService = new SearchService(manager.getAllTrains(), manager.getBookingMap(), manager.getFareCalculator());
    }

    public static void main(String[] args) {
        ReservationManager manager = new ReservationManager();
        File dataFile = new File(DEFAULT_DATA_PATH);

        // Load existing serialized state or seed sample data
        if (dataFile.exists()) {
            try {
                FileManager.SystemState state = FileManager.loadSystemState(dataFile);
                if (state != null) {
                    if (state.stations != null) {
                        for (Station s : state.stations.values()) manager.addStation(s);
                    }
                    if (state.trains != null) {
                        for (Train t : state.trains.values()) manager.addTrain(t);
                    }
                    if (state.bookings != null) {
                        manager.getBookingMap().putAll(state.bookings);
                    }
                    if (state.users != null) {
                        for (User u : state.users.values()) manager.registerUser(u);
                    }
                    SampleDataSeeder.seedUsers(manager);
                }
            } catch (Exception ignored) {
                SampleDataSeeder.seedAll(manager);
            }
        } else {
            SampleDataSeeder.seedAll(manager);
        }

        Scanner scanner = new Scanner(System.in);
        ConsoleApp app = new ConsoleApp(manager, dataFile, scanner);
        app.run();
    }

    public static void start(ReservationManager manager, File dataFile) {
        ConsoleApp app = new ConsoleApp(manager, dataFile, new Scanner(System.in));
        app.run();
    }

    public void run() {
        printHeader();
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = readLine("Enter your choice (1-9): ").trim();
            System.out.println();
            switch (choice) {
                case "1":
                    handleSearchTrains();
                    break;
                case "2":
                    handleViewAllTrains();
                    break;
                case "3":
                    handleBookTicket();
                    break;
                case "4":
                    handlePnrEnquiry();
                    break;
                case "5":
                    handleCancelTicket();
                    break;
                case "6":
                    handleCheckQueues();
                    break;
                case "7":
                    handleCalculateFare();
                    break;
                case "8":
                    handleSaveState();
                    break;
                case "9":
                    running = false;
                    System.out.println("Thank you for using RailSync. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please choose a number between 1 and 9.");
            }
            if (running) {
                System.out.println();
            }
        }
    }

    private void printHeader() {
        System.out.println("==================================================================");
        System.out.println("        RailSync – Interactive Command-Line Console Application   ");
        System.out.println("               Core Java Train Reservation System                 ");
        System.out.println("==================================================================");
    }

    private void printMainMenu() {
        System.out.println("------------------------- MAIN MENU ------------------------------");
        System.out.println("  1. Search Trains (by Source & Destination)");
        System.out.println("  2. View All Trains Catalog");
        System.out.println("  3. Book Train Ticket");
        System.out.println("  4. PNR Status Enquiry");
        System.out.println("  5. Cancel Ticket (with Promotion Cascade & Refund)");
        System.out.println("  6. View RAC & Waiting List Queue Status");
        System.out.println("  7. Dynamic Fare & Concession Calculator");
        System.out.println("  8. Save System State to Disk");
        System.out.println("  9. Exit");
        System.out.println("------------------------------------------------------------------");
    }

    // 1. Search Trains
    private void handleSearchTrains() {
        System.out.println("--- SEARCH TRAINS ---");
        String srcCode = readLine("Enter Source Station Code (e.g. NDLS, BCT, HWH): ").toUpperCase().trim();
        String destCode = readLine("Enter Destination Station Code (e.g. SBC, BSB, NDLS): ").toUpperCase().trim();

        Station source = manager.getStation(srcCode);
        Station destination = manager.getStation(destCode);

        if (source == null) {
            System.out.println("Error: Source station '" + srcCode + "' not found.");
            return;
        }
        if (destination == null) {
            System.out.println("Error: Destination station '" + destCode + "' not found.");
            return;
        }

        try {
            List<Train> trains = searchService.searchTrains(source, destination, LocalDate.now());
            if (trains.isEmpty()) {
                System.out.println("No direct trains found running between " + source.getName() + " and " + destination.getName() + ".");
            } else {
                System.out.println("\nFound " + trains.size() + " train(s) on route " + source.getCode() + " -> " + destination.getCode() + ":");
                System.out.printf("%-8s | %-24s | %-18s | %-8s | %-8s | %-8s%n",
                        "Train No", "Name", "Type", "Departs", "Arrives", "Classes");
                System.out.println("-------------------------------------------------------------------------------------");
                for (Train t : trains) {
                    StringBuilder classes = new StringBuilder();
                    for (SeatClass sc : t.getAvailableClasses()) {
                        classes.append(sc.getCode()).append(" ");
                    }
                    System.out.printf("%-8s | %-24s | %-18s | %-8s | %-8s | %-8s%n",
                            t.getTrainNumber(), t.getTrainName(), t.getTrainType(),
                            t.getDepartureTime(), t.getArrivalTime(), classes.toString().trim());
                }
            }
        } catch (Exception e) {
            System.out.println("Search error: " + e.getMessage());
        }
    }

    // 2. View All Trains
    private void handleViewAllTrains() {
        System.out.println("--- TRAIN CATALOG (" + manager.getAllTrains().size() + " Trains Registered) ---");
        System.out.printf("%-8s | %-22s | %-18s | %-16s | %-16s | %-8s%n",
                "Train No", "Name", "Type", "Source", "Destination", "Distance");
        System.out.println("------------------------------------------------------------------------------------------------");
        for (Train t : manager.getAllTrains()) {
            System.out.printf("%-8s | %-22s | %-18s | %-16s | %-16s | %-8.1f km%n",
                    t.getTrainNumber(), t.getTrainName(), t.getTrainType(),
                    t.getSource().getCode() + " (" + t.getSource().getName() + ")",
                    t.getDestination().getCode() + " (" + t.getDestination().getName() + ")",
                    t.getDistanceKm());
        }
    }

    // 3. Book Ticket
    private void handleBookTicket() {
        System.out.println("--- BOOK TICKET ---");
        String trainNo = readLine("Enter Train Number (e.g. 12952, 22436, 12628): ").trim();
        Train train = manager.getTrain(trainNo);
        if (train == null) {
            System.out.println("Error: Train '" + trainNo + "' not found.");
            return;
        }

        System.out.println("Selected Train: " + train.getTrainNumber() + " - " + train.getTrainName() +
                " (" + train.getSource().getCode() + " -> " + train.getDestination().getCode() + ")");

        // Date selection
        LocalDate journeyDate = promptJourneyDate();
        if (journeyDate == null) return;

        // Class selection
        System.out.println("\nAvailable Classes on this train:");
        List<SeatClass> availableClasses = new ArrayList<>(train.getAvailableClasses());
        for (int i = 0; i < availableClasses.size(); i++) {
            SeatClass sc = availableClasses.get(i);
            System.out.println("  " + (i + 1) + ". " + sc.getCode() + " - " + sc.getDisplayName() +
                    " (Confirmed seats: " + train.getAvailableConfirmedSeats(sc) + ", RAC cap: " + train.getRacCapacity(sc) + ")");
        }
        int classIdx = readInt("Select Class (1-" + availableClasses.size() + "): ", 1, availableClasses.size());
        if (classIdx < 1) return;
        SeatClass selectedClass = availableClasses.get(classIdx - 1);

        // Passengers
        int passengerCount = readInt("Enter number of passengers (1-6): ", 1, 6);
        if (passengerCount < 1) return;

        List<Passenger> passengers = new ArrayList<>();
        for (int i = 1; i <= passengerCount; i++) {
            System.out.println("\nPassenger #" + i + " Details:");
            String name = readLine("  Full Name: ").trim();
            int age = readInt("  Age (0-125): ", 0, 125);
            String gender = readLine("  Gender (M/F/O): ").trim().toUpperCase();
            String phone = readLine("  Mobile Number (10 digits): ").trim();

            BerthType preference = promptBerthPreference();
            try {
                Passenger p = new Passenger(name, age, gender, phone, preference);
                passengers.add(p);
            } catch (Exception e) {
                System.out.println("Validation Error: " + e.getMessage());
                return;
            }
        }

        // Execute Booking
        try {
            System.out.println("\nProcessing reservation transaction...");
            Booking booking = manager.bookTicket(train.getTrainNumber(), journeyDate, selectedClass, passengers, "ConsoleUser");
            System.out.println("\n================ BOOKING CONFIRMATION ================");
            System.out.println("PNR Number        : " + booking.getPnr());
            System.out.println("Booking ID        : " + booking.getBookingId());
            System.out.println("Train             : " + booking.getTrainNumber() + " - " + booking.getTrainName());
            System.out.println("Route             : " + booking.getSource().getCode() + " -> " + booking.getDestination().getCode());
            System.out.println("Journey Date      : " + booking.getJourneyDate());
            System.out.println("Travel Class      : " + booking.getSeatClass().getDisplayName());
            System.out.println("Total Amount Paid : ₹" + String.format("%.2f", booking.getTotalFare()));
            System.out.println("\nAllocated Passenger Tickets:");
            System.out.printf("%-14s | %-16s | %-4s | %-14s | %-12s | %-10s%n",
                    "Ticket ID", "Passenger", "Age", "Status", "Seat/Berth", "Fare");
            System.out.println("--------------------------------------------------------------------------------");
            for (Ticket t : booking.getTickets()) {
                String seatInfo = t.getSeat() != null ?
                        (t.getSeat().getCoachId() + "-" + t.getSeat().getSeatNumber() + " (" + t.getSeat().getBerthType().getLabel() + ")") :
                        (t.getStatus() == BookingStatus.RAC ? "RAC" : "WL");
                System.out.printf("%-14s | %-16s | %-4d | %-14s | %-12s | ₹%-9.2f%n",
                        t.getTicketId(), t.getPassenger().getName(), t.getPassenger().getAge(),
                        t.getStatus().getDisplayStatus(), seatInfo, t.getIndividualFare());
            }
            System.out.println("======================================================");
        } catch (RailSyncException e) {
            System.out.println("\n[BOOKING REJECTED] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n[ERROR] An unexpected error occurred: " + e.getMessage());
        }
    }

    // 4. PNR Status Enquiry
    private void handlePnrEnquiry() {
        System.out.println("--- PNR STATUS ENQUIRY ---");
        String pnr = readLine("Enter PNR Number (e.g. RS861402): ").toUpperCase().trim();
        Booking booking = manager.getBookingMap().get(pnr);
        if (booking == null) {
            System.out.println("No booking found with PNR: " + pnr);
            return;
        }

        System.out.println("\n================ PNR RECORD ================");
        System.out.println("PNR Number   : " + booking.getPnr());
        System.out.println("Train        : " + booking.getTrainNumber() + " - " + booking.getTrainName());
        System.out.println("Route        : " + booking.getSource().getCode() + " -> " + booking.getDestination().getCode());
        System.out.println("Date         : " + booking.getJourneyDate());
        System.out.println("Class        : " + booking.getSeatClass().getDisplayName());
        System.out.println("Booked By    : " + booking.getBookedByUserId());
        System.out.println("Overall State: " + (booking.isCancelled() ? "CANCELLED" : "ACTIVE"));
        System.out.println("\nPassenger List:");
        for (Ticket t : booking.getTickets()) {
            String seatStr = t.getSeat() != null ?
                    ("Coach " + t.getSeat().getCoachId() + ", Berth " + t.getSeat().getSeatNumber() + " (" + t.getSeat().getBerthType() + ")") :
                    "Queue: " + t.getStatus();
            String cancelledStr = t.isCancelled() ? " [CANCELLED]" : "";
            System.out.println("  • " + t.getTicketId() + " | " + t.getPassenger().getName() + " (" +
                    t.getPassenger().getAge() + " yrs) | Status: " + t.getStatus() + cancelledStr + " | " + seatStr);
        }
        System.out.println("============================================");
    }

    // 5. Cancel Ticket
    private void handleCancelTicket() {
        System.out.println("--- CANCEL TICKET ---");
        String pnr = readLine("Enter PNR Number to cancel: ").toUpperCase().trim();
        Booking booking = manager.getBookingMap().get(pnr);
        if (booking == null) {
            System.out.println("No booking found with PNR: " + pnr);
            return;
        }

        System.out.println("\nTickets under PNR " + pnr + ":");
        List<Ticket> tickets = booking.getTickets();
        for (int i = 0; i < tickets.size(); i++) {
            Ticket t = tickets.get(i);
            System.out.println("  " + (i + 1) + ". " + t.getTicketId() + " - " + t.getPassenger().getName() +
                    " [Status: " + t.getStatus() + (t.isCancelled() ? ", ALREADY CANCELLED" : "") + "] Fare: ₹" + t.getIndividualFare());
        }

        int choice = readInt("Select ticket number to cancel (1-" + tickets.size() + ", or 0 to abort): ", 0, tickets.size());
        if (choice <= 0) {
            System.out.println("Cancellation aborted.");
            return;
        }

        Ticket targetTicket = tickets.get(choice - 1);
        if (targetTicket.isCancelled()) {
            System.out.println("This ticket is already cancelled.");
            return;
        }

        String confirm = readLine("Are you sure you want to cancel ticket " + targetTicket.getTicketId() + " for " +
                targetTicket.getPassenger().getName() + "? (y/n): ").trim();
        if (!"y".equalsIgnoreCase(confirm) && !"yes".equalsIgnoreCase(confirm)) {
            System.out.println("Cancellation aborted.");
            return;
        }

        try {
            RefundReceipt receipt = manager.cancelTicket(pnr, targetTicket.getTicketId());
            System.out.println("\n" + receipt.getFormattedReceipt());
        } catch (RailSyncException e) {
            System.out.println("[CANCELLATION FAILED] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // 6. Check Queues
    private void handleCheckQueues() {
        System.out.println("--- RAC & WAITING LIST QUEUE STATUS ---");
        String trainNo = readLine("Enter Train Number: ").trim();
        Train train = manager.getTrain(trainNo);
        if (train == null) {
            System.out.println("Train not found.");
            return;
        }

        LocalDate journeyDate = promptJourneyDate();
        if (journeyDate == null) return;

        System.out.println("\nQueue status for Train " + train.getTrainNumber() + " on " + journeyDate + ":");
        for (SeatClass sc : train.getAvailableClasses()) {
            String queueKey = manager.getWaitingListManager().makeQueueKey(trainNo, sc, journeyDate);
            int racCount = manager.getWaitingListManager().getRacCount(queueKey);
            int racCap = train.getRacCapacity(sc);
            int wlCount = manager.getWaitingListManager().getWaitingListCount(queueKey);
            int wlCap = train.getWaitingListCapacity(sc);
            int confirmedAvail = train.getAvailableConfirmedSeats(sc);

            System.out.printf("  • %-18s: Available Confirmed: %2d | RAC Queue: %2d/%2d | WL Queue: %2d/%2d%n",
                    sc.getDisplayName(), confirmedAvail, racCount, racCap, wlCount, wlCap);
        }
    }

    // 7. Calculate Fare
    private void handleCalculateFare() {
        System.out.println("--- DYNAMIC FARE & CONCESSION CALCULATOR ---");
        String trainNo = readLine("Enter Train Number (or press Enter for default 12952): ").trim();
        if (trainNo.isEmpty()) trainNo = "12952";
        Train train = manager.getTrain(trainNo);
        if (train == null) {
            System.out.println("Train not found.");
            return;
        }

        System.out.println("Select Class: 1) 1A  2) 2A  3) 3A  4) CC  5) SL");
        String classChoice = readLine("Choice (1-5): ").trim();
        SeatClass sc = SeatClass.SLEEPER;
        switch (classChoice) {
            case "1": sc = SeatClass.FIRST_AC; break;
            case "2": sc = SeatClass.SECOND_AC; break;
            case "3": sc = SeatClass.THIRD_AC; break;
            case "4": sc = SeatClass.CHAIR_CAR; break;
            case "5": sc = SeatClass.SLEEPER; break;
        }

        int age = readInt("Enter passenger age: ", 0, 125);
        String gender = readLine("Enter gender (M/F/O): ").trim().toUpperCase();

        Passenger dummy = new Passenger("EnquiryPassenger", age, gender, "9876543210", BerthType.LOWER);
        double totalFare = manager.getFareCalculator().calculateFare(train, sc, dummy);

        System.out.println("\nFare Calculation Breakdown:");
        System.out.println("  Train             : " + train.getTrainNumber() + " - " + train.getTrainName() + " (" + train.getTrainType() + ")");
        System.out.println("  Distance          : " + train.getDistanceKm() + " km");
        System.out.println("  Base Fare Rate    : ₹" + train.getBaseFareRatePerKm() + " per km");
        System.out.println("  Class Selected    : " + sc.getDisplayName() + " (Multiplier: " + sc.getFareMultiplier() + "x)");
        System.out.println("  Reservation Fee   : ₹" + sc.getReservationFee());
        System.out.println("  Applied Concession: " + dummy.getConcession().getDescription());
        System.out.println("  Total Fare        : ₹" + String.format("%.2f", totalFare));
    }

    // 8. Save State
    private void handleSaveState() {
        System.out.println("--- SAVE SYSTEM STATE ---");
        try {
            FileManager.saveSystemState(manager, dataFile);
            System.out.println("System state successfully persisted to: " + dataFile.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Failed to save state: " + e.getMessage());
        }
    }

    // Helper input methods
    private String readLine(String prompt) {
        System.out.print(prompt);
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return "";
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer format. Please try again.");
            }
        }
    }

    private LocalDate promptJourneyDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String dateStr = readLine("Enter Journey Date (YYYY-MM-DD, press Enter for " + tomorrow + "): ").trim();
        if (dateStr.isEmpty()) {
            return tomorrow;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr);
            if (date.isBefore(LocalDate.now())) {
                System.out.println("Error: Journey date cannot be in the past.");
                return null;
            }
            return date;
        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date format. Please use YYYY-MM-DD (e.g. 2026-09-15).");
            return null;
        }
    }

    private BerthType promptBerthPreference() {
        System.out.println("  Berth Preference: 1) Lower  2) Middle  3) Upper  4) Side Lower  5) Side Upper  6) Window  7) Aisle");
        String pref = readLine("  Choice (1-7, default 1): ").trim();
        switch (pref) {
            case "1": return BerthType.LOWER;
            case "2": return BerthType.MIDDLE;
            case "3": return BerthType.UPPER;
            case "4": return BerthType.SIDE_LOWER;
            case "5": return BerthType.SIDE_UPPER;
            case "6": return BerthType.WINDOW;
            case "7": return BerthType.AISLE;
            default: return BerthType.LOWER;
        }
    }
}
