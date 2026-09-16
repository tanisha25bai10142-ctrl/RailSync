package com.railsync;

import com.railsync.exception.*;
import com.railsync.manager.FileManager;
import com.railsync.manager.ReservationManager;
import com.railsync.model.*;
import com.railsync.thread.ConcurrencySimulation;
import com.railsync.util.SampleDataSeeder;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * Automated test suite verifying core RailSync features:
 * 1. Ticket booking and seat allocation
 * 2. RAC allocation when confirmed seats are full
 * 3. Waiting list allocation when RAC is full
 * 4. Ticket cancellation, refund, and queue cascade promotion
 * 5. Dynamic fare calculation and passenger concessions
 * 6. Multithreaded booking thread safety
 * 7. File persistence (serialization and deserialization)
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("      RailSync Automated Test Suite       ");
        System.out.println("==========================================\n");

        run("Test 1: Ticket Booking & Seat Allocation", TestRunner::testBooking);
        run("Test 2: RAC Allocation when Confirmed Full", TestRunner::testRacAllocation);
        run("Test 3: Waiting List Allocation & Limit Check", TestRunner::testWaitingListAllocation);
        run("Test 4: Cancellation, Refund & Cascade Promotion", TestRunner::testCancellationAndPromotion);
        run("Test 5: Fare Calculation & Concessions", TestRunner::testFareCalculation);
        run("Test 6: Multithreaded Booking Thread Safety", TestRunner::testMultithreading);
        run("Test 7: File Persistence & State Restoration", TestRunner::testDataPersistence);

        System.out.println("\n==========================================");
        System.out.println("Result: " + passed + " passed, " + failed + " failed (Total: " + (passed + failed) + ")");
        System.out.println("==========================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void run(String testName, TestCase test) {
        System.out.printf("%-52s : ", testName);
        try {
            test.run();
            System.out.println("[PASS]");
            passed++;
        } catch (Throwable t) {
            System.out.println("[FAIL] -> " + t.getMessage());
            failed++;
        }
    }

    interface TestCase {
        void run() throws Exception;
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(message + " (Expected: " + expected + ", Actual: " + actual + ")");
        }
    }

    // ================= Test Cases =================

    /**
     * Test 1: Successful booking and seat allocation
     */
    private static void testBooking() throws Exception {
        ReservationManager manager = new ReservationManager();
        SampleDataSeeder.seedStations(manager);
        SampleDataSeeder.seedTrains(manager);

        LocalDate date = LocalDate.now().plusDays(5);
        List<Passenger> passengers = Collections.singletonList(
                new Passenger("Aditya Sharma", 25, "Male", "9876543210", BerthType.LOWER));

        Booking booking = manager.bookTicket("12952", date, SeatClass.SECOND_AC, passengers, "studentUser");
        assertTrue(booking != null, "Booking should not be null");
        assertTrue(booking.getPnr() != null && booking.getPnr().startsWith("RS"), "PNR must start with RS");
        assertEquals(1, booking.getTickets().size(), "Must have 1 ticket");

        Ticket ticket = booking.getTickets().get(0);
        assertEquals(BookingStatus.CONFIRMED, ticket.getStatus(), "Status must be CONFIRMED");
        assertTrue(ticket.getSeat() != null, "Seat must be assigned for confirmed ticket");
    }

    /**
     * Test 2: When confirmed seats are exhausted, booking goes to RAC
     */
    private static void testRacAllocation() throws Exception {
        ReservationManager manager = new ReservationManager();
        Station s1 = new Station("DEL", "Delhi", "Delhi", "Delhi", "NR", 0);
        Station s2 = new Station("BCT", "Mumbai", "Mumbai", "Maharashtra", "WR", 500);
        manager.addStation(s1);
        manager.addStation(s2);

        SuperfastExpress train = new SuperfastExpress("T100", "Mini Express", s1, s2,
                LocalTime.of(8, 0), LocalTime.of(12, 0), 500.0);
        // 1 Confirmed, 2 RAC, 1 WL
        train.addCoach(new Coach("C1", SeatClass.CHAIR_CAR, 1, 2, 1));
        manager.addTrain(train);

        LocalDate date = LocalDate.now().plusDays(2);

        // 1st booking -> gets confirmed seat
        Booking b1 = manager.bookTicket("T100", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("User One", 25, "Male", "9876543210", BerthType.WINDOW)), "u1");
        assertEquals(BookingStatus.CONFIRMED, b1.getTickets().get(0).getStatus(), "First booking must be CONFIRMED");

        // 2nd booking -> gets RAC 1
        Booking b2 = manager.bookTicket("T100", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("User Two", 26, "Female", "9876543210", BerthType.AISLE)), "u2");
        assertEquals(BookingStatus.RAC, b2.getTickets().get(0).getStatus(), "Second booking must be RAC");
        assertEquals(1, b2.getTickets().get(0).getRacPosition(), "RAC position must be 1");
    }

    /**
     * Test 3: When RAC is exhausted, booking goes to Waiting List, and stops when WL is full
     */
    private static void testWaitingListAllocation() throws Exception {
        ReservationManager manager = new ReservationManager();
        Station s1 = new Station("DEL", "Delhi", "Delhi", "Delhi", "NR", 0);
        Station s2 = new Station("BCT", "Mumbai", "Mumbai", "Maharashtra", "WR", 300);
        manager.addStation(s1);
        manager.addStation(s2);

        SuperfastExpress train = new SuperfastExpress("WL101", "WL Express", s1, s2,
                LocalTime.of(10, 0), LocalTime.of(14, 0), 300.0);
        // 1 Confirmed, 1 RAC, 1 WL
        train.addCoach(new Coach("C1", SeatClass.CHAIR_CAR, 1, 1, 1));
        manager.addTrain(train);

        LocalDate date = LocalDate.now().plusDays(3);

        manager.bookTicket("WL101", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P1", 20, "Male", "9876543210", BerthType.WINDOW)), "u"); // CNF
        manager.bookTicket("WL101", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P2", 21, "Male", "9876543210", BerthType.AISLE)), "u"); // RAC 1

        // 3rd booking -> Waiting List 1
        Booking b3 = manager.bookTicket("WL101", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("P3", 22, "Male", "9876543210", BerthType.WINDOW)), "u");
        assertEquals(BookingStatus.WAITING_LIST, b3.getTickets().get(0).getStatus(), "Must be WAITING_LIST");
        assertEquals(1, b3.getTickets().get(0).getWlPosition(), "Must be WL 1");

        // 4th booking -> Capacity exhausted -> SeatUnavailableException
        boolean rejected = false;
        try {
            manager.bookTicket("WL101", date, SeatClass.CHAIR_CAR,
                    Collections.singletonList(new Passenger("P4", 23, "Male", "9876543210", BerthType.AISLE)), "u");
        } catch (SeatUnavailableException e) {
            rejected = true;
        }
        assertTrue(rejected, "Exhausted train must throw SeatUnavailableException");
    }

    /**
     * Test 4: Cancellation, refund calculation, and RAC-to-Confirmed / WL-to-RAC cascade promotion
     */
    private static void testCancellationAndPromotion() throws Exception {
        ReservationManager manager = new ReservationManager();
        Station s1 = new Station("A", "Station A", "City A", "State", "NR", 0);
        Station s2 = new Station("B", "Station B", "City B", "State", "NR", 300);
        manager.addStation(s1);
        manager.addStation(s2);

        SuperfastExpress train = new SuperfastExpress("PROMO1", "Promo Train", s1, s2,
                LocalTime.of(10, 0), LocalTime.of(14, 0), 300.0);
        // 1 Confirmed, 1 RAC, 1 WL
        train.addCoach(new Coach("C1", SeatClass.CHAIR_CAR, 1, 1, 1));
        manager.addTrain(train);

        LocalDate date = LocalDate.now().plusDays(3);

        Booking b1 = manager.bookTicket("PROMO1", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("Confirmed Pass", 25, "Male", "9876543210", BerthType.WINDOW)), "u");
        Booking b2 = manager.bookTicket("PROMO1", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("RAC Pass", 26, "Female", "9876543210", BerthType.AISLE)), "u");
        Booking b3 = manager.bookTicket("PROMO1", date, SeatClass.CHAIR_CAR,
                Collections.singletonList(new Passenger("WL Pass", 27, "Male", "9876543210", BerthType.WINDOW)), "u");

        Ticket t1 = b1.getTickets().get(0); // CNF
        Ticket t2 = b2.getTickets().get(0); // RAC 1
        Ticket t3 = b3.getTickets().get(0); // WL 1

        assertEquals(BookingStatus.CONFIRMED, t1.getStatus(), "t1 must be Confirmed");
        assertEquals(BookingStatus.RAC, t2.getStatus(), "t2 must be RAC");
        assertEquals(BookingStatus.WAITING_LIST, t3.getStatus(), "t3 must be WL");

        // Cancel confirmed ticket t1
        RefundReceipt receipt = manager.cancelTicket(b1.getPnr(), t1.getTicketId());
        assertTrue(receipt != null, "Refund receipt should be generated");
        assertTrue(receipt.getNetRefundAmount() > 0, "Refund amount must be greater than zero");
        assertTrue(t1.isCancelled(), "t1 must be marked cancelled");

        // t2 should be promoted to CONFIRMED with a seat
        assertEquals(BookingStatus.CONFIRMED, t2.getStatus(), "RAC passenger must be promoted to CONFIRMED");
        assertTrue(t2.getSeat() != null, "Promoted passenger must be assigned physical seat");

        // t3 should be promoted from WL to RAC 1
        assertEquals(BookingStatus.RAC, t3.getStatus(), "WL passenger must be promoted to RAC");
        assertEquals(1, t3.getRacPosition(), "Promoted passenger must be RAC 1");
    }

    /**
     * Test 5: Dynamic fare calculation and passenger age concessions
     */
    private static void testFareCalculation() {
        ReservationManager manager = new ReservationManager();
        SampleDataSeeder.seedStations(manager);
        SampleDataSeeder.seedTrains(manager);

        Train rajdhani = manager.getTrain("12952");
        Passenger adult = new Passenger("Adult", 30, "Male", "9876543210", BerthType.LOWER);
        Passenger senior = new Passenger("Senior", 65, "Male", "9876543210", BerthType.LOWER);
        Passenger infant = new Passenger("Infant", 2, "Female", "9876543210", BerthType.LOWER);

        double adultFare = manager.getFareCalculator().calculateFare(rajdhani, SeatClass.SECOND_AC, adult);
        double seniorFare = manager.getFareCalculator().calculateFare(rajdhani, SeatClass.SECOND_AC, senior);
        double infantFare = manager.getFareCalculator().calculateFare(rajdhani, SeatClass.SECOND_AC, infant);

        assertTrue(adultFare > seniorFare, "Adult fare must be higher than senior citizen fare");
        assertEquals(0.0, infantFare, "Infant fare must be free (0.0)");
    }

    /**
     * Test 6: Concurrent booking across threads with synchronization
     */
    private static void testMultithreading() {
        // Run 10 threads on a train with 4 confirmed, 3 RAC, 3 WL seats
        ConcurrencySimulation.SimulationResult result = ConcurrencySimulation.runSimulation(10, null);
        assertTrue(result.zeroDuplicateSeats, "Concurrency simulation must have zero duplicate seats");
        assertEquals(4, result.confirmedAllocations, "Exactly 4 confirmed seats should be assigned");
        assertEquals(3, result.racAllocations, "Exactly 3 RAC slots should be assigned");
        assertEquals(3, result.waitingListAllocations, "Exactly 3 WL slots should be assigned");
    }

    /**
     * Test 7: State serialization to file and restoration
     */
    private static void testDataPersistence() throws Exception {
        ReservationManager manager = new ReservationManager();
        SampleDataSeeder.seedStations(manager);
        SampleDataSeeder.seedTrains(manager);
        SampleDataSeeder.seedUsers(manager);

        File tempFile = new File("data/test_state.ser");
        FileManager.saveSystemState(manager, tempFile);
        assertTrue(tempFile.exists(), "State file must exist on disk");
        assertTrue(tempFile.length() > 0, "State file must not be empty");

        FileManager.SystemState state = FileManager.loadSystemState(tempFile);
        assertTrue(state != null, "Loaded state must not be null");
        assertTrue(state.trains != null && !state.trains.isEmpty(), "Loaded trains must not be empty");
        assertTrue(state.stations != null && !state.stations.isEmpty(), "Loaded stations must not be empty");

        // Clean up temporary test file
        tempFile.delete();
    }
}
