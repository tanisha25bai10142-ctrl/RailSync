package com.railsync;

import com.railsync.exception.*;
import com.railsync.manager.FileManager;
import com.railsync.manager.ReservationManager;
import com.railsync.model.*;
import com.railsync.service.AnalyticsService;
import com.railsync.service.SearchService;
import com.railsync.thread.ConcurrencySimulation;
import com.railsync.util.SampleDataSeeder;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Automated Verification Suite validating all 15 required core scenarios:
 * 1. Successful booking
 * 2. Booking when confirmed seats are full
 * 3. Dynamic RAC allocation
 * 4. Dynamic Waiting-list allocation
 * 5. Cancellation logic
 * 6. RAC to Confirmed promotion cascade
 * 7. Waiting-list to RAC promotion cascade
 * 8. Invalid passenger details validation
 * 9. Invalid PNR handling
 * 10. Concurrent booking thread-safety
 * 11. Data serialization / saving
 * 12. Data deserialization / loading
 * 13. Dynamic fare calculation & age concessions
 * 14. Train search, filtering, and comparator sorting
 * 15. Dynamic administrative analytics computation
 */
public class TestRunner {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("            RAILSYNC AUTOMATED CORE JAVA VERIFICATION SUITE               ");
        System.out.println("==========================================================================\n");

        runTest("1. Successful Booking Allocation", TestRunner::testSuccessfulBooking);
        runTest("2. Booking When Confirmed Full -> Automatic RAC", TestRunner::testBookingWhenConfirmedFull);
        runTest("3. RAC Allocation Tracking", TestRunner::testRacAllocation);
        runTest("4. Waiting List Allocation When RAC Full", TestRunner::testWaitingListAllocation);
        runTest("5. Ticket Cancellation and Refund Receipt", TestRunner::testCancellationAndRefund);
        runTest("6. Automatic RAC to Confirmed Promotion Cascade", TestRunner::testRacPromotionCascade);
        runTest("7. Automatic Waiting-List to RAC Promotion Cascade", TestRunner::testWaitingListPromotionCascade);
        runTest("8. Invalid Passenger Input Validation", TestRunner::testInvalidPassengerValidation);
        runTest("9. Invalid PNR Exception Handling", TestRunner::testInvalidPNRHandling);
        runTest("10. Concurrent Booking Simulation & Race Condition Prevention", TestRunner::testConcurrencySafety);
        runTest("11. System State Serialization (Saving)", TestRunner::testDataSaving);
        runTest("12. System State Deserialization (Loading)", TestRunner::testDataLoading);
        runTest("13. Dynamic Fare Calculation & Concession Discounts", TestRunner::testFareCalculationAndDiscounts);
        runTest("14. Train Search, Route Filtering & Custom Comparators", TestRunner::testSearchAndComparators);
        runTest("15. Real-Time Dynamic Administrative Analytics", TestRunner::testAdminAnalytics);

        System.out.println("\n==========================================================================");
        System.out.println(String.format(" VERIFICATION COMPLETE: %d PASSED, %d FAILED (TOTAL %d)",
                testsPassed, testsFailed, (testsPassed + testsFailed)));
        System.out.println("==========================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, TestTask task) {
        System.out.print(String.format("%-64s : ", testName));
        try {
            task.execute();
            System.out.println("[PASS]");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("[FAIL] -> " + t.getMessage());
            t.printStackTrace(System.out);
            testsFailed++;
        }
    }

    @FunctionalInterface
    interface TestTask {
        void execute() throws Exception;
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(message + " (Expected: " + expected + ", Actual: " + actual + ")");
        }
    }

    // ================= Test Cases =================

    private static void testSuccessfulBooking() throws Exception {
        ReservationManager manager = new ReservationManager();
        SampleDataSeeder.seedStations(manager);
        SampleDataSeeder.seedTrains(manager);

        LocalDate date = LocalDate.now().plusDays(5);
        List<Passenger> pass = Collections.singletonList(
                new Passenger("Aditya Sharma", 25, "Male", "9876543210", BerthType.LOWER));

        Booking booking = manager.bookTicket("12952", date, SeatClass.SECOND_AC, pass, "testUser");
        assertTrue(booking != null, "Booking should not be null");
        assertTrue(booking.getPnr() != null && booking.getPnr().startsWith("RS"), "PNR must start with RS");
        assertEquals(1, booking.getTickets().size(), "Must have 1 ticket");
        assertEquals(BookingStatus.CONFIRMED, booking.getTickets().get(0).getStatus(), "Status must be CONFIRMED");
        assertTrue(booking.getTickets().get(0).getSeat() != null, "Seat must be assigned for confirmed ticket");
    }

    private static void testBookingWhenConfirmedFull() throws Exception {
        ReservationManager manager = new ReservationManager();
        Station s1 = new Station("T1", "Station 1", "City", "State", "NR", 0);
        Station s2 = new Station("T2", "Station 2", "City", "State", "NR", 500);
        manager.addStation(s1);
        manager.addStation(s2);

        SuperfastExpress train = new SuperfastExpress("T100", "Mini Express", s1, s2,
                LocalTime.of(8, 0), LocalTime.of(12, 0), 500.0);
        // Only 1 confirmed seat, 1 RAC, 1 WL
        train.addCoach(new Coach("C1", SeatClass.CHAIR_CAR, 1, 1, 1));
        manager.addTrain(train);

        LocalDate date = LocalDate.now().plusDays(2);

        // 1st booking -> Should get Confirmed
        List<Passenger> p1 = Collections.singletonList(new Passenger("Passenger One", 30, "Male", "9876543210", BerthType.WINDOW));
        Booking b1 = manager.bookTicket("T100", date, SeatClass.CHAIR_CAR, p1, "u1");
        assertEquals(BookingStatus.CONFIRMED, b1.getTickets().get(0).getStatus(), "First booking must be CONFIRMED");

        // 2nd booking -> Confirmed full -> Should get RAC 1
        List<Passenger> p2 = Collections.singletonList(new Passenger("Passenger Two", 32, "Female", "9876543210", BerthType.AISLE));
        Booking b2 = manager.bookTicket("T100", date, SeatClass.CHAIR_CAR, p2, "u2");
        assertEquals(BookingStatus.RAC, b2.getTickets().get(0).getStatus(), "Second booking must be RAC");
        assertEquals(1, b2.getTickets().get(0).getRacPosition(), "RAC position must be 1");
    }

    private static void testRacAllocation() throws Exception {
        ReservationManager manager = new ReservationManager();
        Station s1 = new Station("A", "Alpha", "City", "State", "NR", 0);
        Station s2 = new Station("B", "Beta", "City", "State", "NR", 300);
        manager.addStation(s1);
        manager.addStation(s2);

        SuperfastExpress train = new SuperfastExpress("R101", "RAC Train", s1, s2,
                LocalTime.of(10, 0), LocalTime.of(14, 0), 300.0);
        train.addCoach(new Coach("C1", SeatClass.CHAIR_CAR, 1, 2, 2));
        manager.addTrain(train);
        LocalDate date = LocalDate.now().plusDays(3);

        manager.bookTicket("R101", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P1", 20, "Male", "9876543210", BerthType.WINDOW)), "u");
        Booking b2 = manager.bookTicket("R101", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P2", 21, "Female", "9876543210", BerthType.AISLE)), "u");
        Booking b3 = manager.bookTicket("R101", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P3", 22, "Male", "9876543210", BerthType.WINDOW)), "u");

        assertEquals(1, b2.getTickets().get(0).getRacPosition(), "b2 should be RAC 1");
        assertEquals(2, b3.getTickets().get(0).getRacPosition(), "b3 should be RAC 2");
    }

    private static void testWaitingListAllocation() throws Exception {
        ReservationManager manager = new ReservationManager();
        Station s1 = new Station("A", "Alpha", "City", "State", "NR", 0);
        Station s2 = new Station("B", "Beta", "City", "State", "NR", 300);
        manager.addStation(s1);
        manager.addStation(s2);

        SuperfastExpress train = new SuperfastExpress("WL101", "WL Train", s1, s2,
                LocalTime.of(10, 0), LocalTime.of(14, 0), 300.0);
        // 1 Confirmed, 1 RAC, 1 WL
        train.addCoach(new Coach("C1", SeatClass.CHAIR_CAR, 1, 1, 1));
        manager.addTrain(train);
        LocalDate date = LocalDate.now().plusDays(3);

        manager.bookTicket("WL101", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P1", 20, "Male", "9876543210", BerthType.WINDOW)), "u"); // CNF
        manager.bookTicket("WL101", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P2", 21, "Male", "9876543210", BerthType.AISLE)), "u"); // RAC
        Booking bWL = manager.bookTicket("WL101", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P3", 22, "Male", "9876543210", BerthType.WINDOW)), "u"); // WL

        assertEquals(BookingStatus.WAITING_LIST, bWL.getTickets().get(0).getStatus(), "Must be WAITING_LIST");
        assertEquals(1, bWL.getTickets().get(0).getWlPosition(), "Must be WL 1");

        // 4th booking should throw SeatUnavailableException (Capacities exhausted)
        boolean caught = false;
        try {
            manager.bookTicket("WL101", date, SeatClass.CHAIR_CAR,
                    Collections.singletonList(new Passenger("P4", 23, "Male", "9876543210", BerthType.AISLE)), "u");
        } catch (SeatUnavailableException e) {
            caught = true;
        }
        assertTrue(caught, "Exhausted train must throw SeatUnavailableException");
    }

    private static void testCancellationAndRefund() throws Exception {
        ReservationManager manager = new ReservationManager();
        SampleDataSeeder.seedStations(manager);
        SampleDataSeeder.seedTrains(manager);

        LocalDate date = LocalDate.now().plusDays(5);
        List<Passenger> pass = Collections.singletonList(
                new Passenger("Rohan Das", 30, "Male", "9876543210", BerthType.LOWER));

        Booking booking = manager.bookTicket("12952", date, SeatClass.SECOND_AC, pass, "testUser");
        Ticket t = booking.getTickets().get(0);

        RefundReceipt receipt = manager.cancelTicket(booking.getPnr(), t.getTicketId());
        assertTrue(receipt != null, "Refund receipt should be generated");
        assertTrue(t.isCancelled(), "Ticket should be marked cancelled");
        assertTrue(booking.isCancelled(), "Booking should be marked cancelled");
        assertTrue(receipt.getNetRefundAmount() > 0, "Refund amount must be greater than zero");
    }

    private static void testRacPromotionCascade() throws Exception {
        ReservationManager manager = new ReservationManager();
        Station s1 = new Station("A", "Alpha", "City", "State", "NR", 0);
        Station s2 = new Station("B", "Beta", "City", "State", "NR", 300);
        manager.addStation(s1);
        manager.addStation(s2);

        SuperfastExpress train = new SuperfastExpress("PROMO1", "Promo Train", s1, s2,
                LocalTime.of(10, 0), LocalTime.of(14, 0), 300.0);
        train.addCoach(new Coach("C1", SeatClass.CHAIR_CAR, 1, 1, 1));
        manager.addTrain(train);
        LocalDate date = LocalDate.now().plusDays(3);

        Booking b1 = manager.bookTicket("PROMO1", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("Confirmed Pass", 25, "Male", "9876543210", BerthType.WINDOW)), "u");
        Booking b2 = manager.bookTicket("PROMO1", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("RAC Pass", 26, "Female", "9876543210", BerthType.AISLE)), "u");

        Ticket t1 = b1.getTickets().get(0);
        Ticket t2 = b2.getTickets().get(0);

        assertEquals(BookingStatus.CONFIRMED, t1.getStatus(), "t1 must be Confirmed");
        assertEquals(BookingStatus.RAC, t2.getStatus(), "t2 must be RAC");

        // Cancel confirmed ticket t1 -> t2 must be promoted to CONFIRMED!
        manager.cancelTicket(b1.getPnr(), t1.getTicketId());

        assertEquals(BookingStatus.CONFIRMED, t2.getStatus(), "RAC passenger must be promoted to CONFIRMED");
        assertTrue(t2.getSeat() != null, "Promoted passenger must be assigned physical seat");
        assertEquals(0, t2.getRacPosition(), "Promoted passenger RAC position must reset to 0");
    }

    private static void testWaitingListPromotionCascade() throws Exception {
        ReservationManager manager = new ReservationManager();
        Station s1 = new Station("A", "Alpha", "City", "State", "NR", 0);
        Station s2 = new Station("B", "Beta", "City", "State", "NR", 300);
        manager.addStation(s1);
        manager.addStation(s2);

        SuperfastExpress train = new SuperfastExpress("PROMO2", "Promo Train 2", s1, s2,
                LocalTime.of(10, 0), LocalTime.of(14, 0), 300.0);
        // 1 Confirmed, 1 RAC, 1 WL
        train.addCoach(new Coach("C1", SeatClass.CHAIR_CAR, 1, 1, 1));
        manager.addTrain(train);
        LocalDate date = LocalDate.now().plusDays(3);

        Booking b1 = manager.bookTicket("PROMO2", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P1", 25, "Male", "9876543210", BerthType.WINDOW)), "u");
        Booking b2 = manager.bookTicket("PROMO2", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P2", 26, "Female", "9876543210", BerthType.AISLE)), "u");
        Booking b3 = manager.bookTicket("PROMO2", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P3", 27, "Male", "9876543210", BerthType.WINDOW)), "u");

        Ticket t1 = b1.getTickets().get(0); // CNF
        Ticket t2 = b2.getTickets().get(0); // RAC 1
        Ticket t3 = b3.getTickets().get(0); // WL 1

        assertEquals(BookingStatus.CONFIRMED, t1.getStatus(), "t1 is CNF");
        assertEquals(BookingStatus.RAC, t2.getStatus(), "t2 is RAC 1");
        assertEquals(BookingStatus.WAITING_LIST, t3.getStatus(), "t3 is WL 1");

        // Cancel t1 -> t2 promotes to CNF, and t3 promotes to RAC 1!
        manager.cancelTicket(b1.getPnr(), t1.getTicketId());

        assertEquals(BookingStatus.CONFIRMED, t2.getStatus(), "t2 must now be CONFIRMED");
        assertEquals(BookingStatus.RAC, t3.getStatus(), "t3 must now be PROMOTED to RAC");
        assertEquals(1, t3.getRacPosition(), "t3 must be RAC 1");
        assertEquals(0, t3.getWlPosition(), "t3 WL position must reset to 0");
    }

    private static void testInvalidPassengerValidation() {
        // Invalid name (too short or symbols)
        boolean caughtName = false;
        try {
            new Passenger("X", 25, "Male", "9876543210", BerthType.LOWER);
        } catch (IllegalArgumentException e) {
            caughtName = true;
        }
        assertTrue(caughtName, "Name with < 2 characters must be rejected");

        // Invalid age (>125)
        boolean caughtAge = false;
        try {
            new Passenger("Valid Name", 140, "Male", "9876543210", BerthType.LOWER);
        } catch (IllegalArgumentException e) {
            caughtAge = true;
        }
        assertTrue(caughtAge, "Age > 125 must be rejected");
    }

    private static void testInvalidPNRHandling() {
        ReservationManager manager = new ReservationManager();
        boolean caught = false;
        try {
            manager.cancelTicket("RS999999", "TK-NONEXISTENT");
        } catch (InvalidPNRException e) {
            caught = true;
        } catch (Exception ignored) {}
        assertTrue(caught, "Non-existent PNR must trigger InvalidPNRException");
    }

    private static void testConcurrencySafety() {
        // Run 10 simultaneous threads on 4 seats
        ConcurrencySimulation.SimulationResult result = ConcurrencySimulation.runSimulation(10, null);
        assertTrue(result.zeroDuplicateSeats, "Concurrency simulation must have ZERO duplicate seats");
        assertEquals(4, result.confirmedAllocations, "Exactly 4 confirmed seats should be assigned");
        assertEquals(3, result.racAllocations, "Exactly 3 RAC slots should be assigned");
        assertEquals(3, result.waitingListAllocations, "Exactly 3 WL slots should be assigned");
    }

    private static void testDataSaving() throws Exception {
        ReservationManager manager = new ReservationManager();
        SampleDataSeeder.seedStations(manager);
        SampleDataSeeder.seedTrains(manager);
        SampleDataSeeder.seedUsers(manager);

        File tempFile = new File("data/test_state.ser");
        FileManager.saveSystemState(manager, tempFile);
        assertTrue(tempFile.exists(), "State file must be created on disk");
        assertTrue(tempFile.length() > 0, "State file must not be empty");
    }

    private static void testDataLoading() throws Exception {
        File tempFile = new File("data/test_state.ser");
        FileManager.SystemState state = FileManager.loadSystemState(tempFile);
        assertTrue(state != null, "Loaded state must not be null");
        assertTrue(state.trains != null && !state.trains.isEmpty(), "Loaded trains must not be empty");
        assertTrue(state.stations != null && !state.stations.isEmpty(), "Loaded stations must not be empty");
        // Clean up test file
        tempFile.delete();
    }

    private static void testFareCalculationAndDiscounts() {
        ReservationManager manager = new ReservationManager();
        SampleDataSeeder.seedStations(manager);
        SampleDataSeeder.seedTrains(manager);

        Train rajdhani = manager.getTrain("12952"); // 1384 km
        Passenger adult = new Passenger("Adult", 30, "Male", "9876543210", BerthType.LOWER);
        Passenger senior = new Passenger("Senior", 65, "Male", "9876543210", BerthType.LOWER); // 40% discount
        Passenger infant = new Passenger("Infant", 2, "Female", "9876543210", BerthType.LOWER); // Free

        double adultFare = manager.getFareCalculator().calculateFare(rajdhani, SeatClass.SECOND_AC, adult);
        double seniorFare = manager.getFareCalculator().calculateFare(rajdhani, SeatClass.SECOND_AC, senior);
        double infantFare = manager.getFareCalculator().calculateFare(rajdhani, SeatClass.SECOND_AC, infant);

        assertTrue(adultFare > seniorFare, "Adult fare must be higher than senior citizen fare");
        assertEquals(0.0, infantFare, "Infant fare must be 0.0");
    }

    private static void testSearchAndComparators() throws Exception {
        ReservationManager manager = new ReservationManager();
        SampleDataSeeder.seedStations(manager);
        SampleDataSeeder.seedTrains(manager);

        SearchService service = new SearchService(manager.getAllTrains(), manager.getBookingMap(), manager.getFareCalculator());
        Station ndls = manager.getStation("NDLS");
        Station bct = manager.getStation("BCT");

        List<Train> found = service.searchTrains(ndls, bct, LocalDate.now().plusDays(2));
        assertTrue(!found.isEmpty(), "Must find trains between NDLS and BCT");

        // Test custom comparator sorting by duration
        List<Train> sortedByDuration = service.sortTrains(found, SearchService.sortByDuration());
        for (int i = 0; i < sortedByDuration.size() - 1; i++) {
            assertTrue(sortedByDuration.get(i).getJourneyDuration().compareTo(
                    sortedByDuration.get(i + 1).getJourneyDuration()) <= 0, "Trains must be sorted in non-decreasing duration order");
        }
    }

    private static void testAdminAnalytics() throws Exception {
        ReservationManager manager = new ReservationManager();
        SampleDataSeeder.seedAll(manager);

        AnalyticsService analytics = new AnalyticsService(manager.getAllTrains(), manager.getBookingMap().values());
        assertTrue(analytics.getTotalTrains() >= 8, "Total trains must be at least 8");
        assertTrue(analytics.getTotalBookings() >= 3, "Total bookings must be at least 3");
        assertTrue(analytics.getConfirmedTicketsCount() > 0, "Confirmed tickets must be > 0");
        assertTrue(analytics.getTotalGrossRevenue() > 0, "Gross revenue must be > 0");
        assertTrue(!analytics.getMostOccupiedTrain().equals("N/A"), "Most occupied train must be calculated");
    }
}
