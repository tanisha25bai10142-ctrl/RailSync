package com.railsync.thread;

import com.railsync.manager.ReservationManager;
import com.railsync.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Executes an intense multithreaded simulation to prove thread-safety and race condition prevention.
 * Demonstrates:
 * - Thread creation, Thread.start(), Thread.join()
 * - java.util.concurrent.CountDownLatch
 * - Synchronization guarantees (Mutual Exclusion on seat allocation)
 * - Atomic state transitions
 */
public class ConcurrencySimulation {

    public static class SimulationResult {
        public int totalThreads;
        public int confirmedAllocations;
        public int racAllocations;
        public int waitingListAllocations;
        public int failedAttempts;
        public boolean zeroDuplicateSeats;
        public List<String> seatAssignedList;
        public List<String> logs;

        public SimulationResult() {
            this.seatAssignedList = new ArrayList<>();
            this.logs = new ArrayList<>();
        }
    }

    /**
     * Runs a concurrent booking simulation with the given number of threads.
     */
    public static SimulationResult runSimulation(int threadCount, Consumer<String> liveLogger) {
        SimulationResult result = new SimulationResult();
        result.totalThreads = threadCount;

        Consumer<String> dispatchLogger = msg -> {
            result.logs.add(msg);
            if (liveLogger != null) {
                liveLogger.accept(msg);
            }
        };

        dispatchLogger.accept("=== INITIATING CONCURRENT BOOKING SIMULATION ===");
        dispatchLogger.accept("Configuring isolated test train with constrained capacity...");

        // Setup an isolated sandbox manager to test pure concurrency without altering production data
        ReservationManager simManager = new ReservationManager();
        Station source = new Station("DEL", "Delhi", "Delhi", "Delhi", "NR", 0.0);
        Station dest = new Station("MUM", "Mumbai", "Mumbai", "Maharashtra", "WR", 1400.0);
        simManager.addStation(source);
        simManager.addStation(dest);

        // Train with exactly 4 Confirmed seats, 3 RAC seats, 3 Waiting list seats
        SuperfastExpress testTrain = new SuperfastExpress("99999", "Concurrency Express",
                source, dest, LocalTime.of(10, 0), LocalTime.of(20, 0), 1400.0);
        Coach testCoach = new Coach("C1", SeatClass.CHAIR_CAR, 4, 3, 3);
        testTrain.addCoach(testCoach);
        simManager.addTrain(testTrain);

        LocalDate testDate = LocalDate.now().plusDays(2);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<BookingTask> tasks = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (int i = 1; i <= threadCount; i++) {
            String passengerName = "Passenger-" + (char) ('A' + (i - 1));
            BookingTask task = new BookingTask("ThreadTask-" + i, passengerName, "99999",
                    testDate, SeatClass.CHAIR_CAR, simManager, startSignal, dispatchLogger);
            tasks.add(task);

            Thread t = new Thread(task, "BookingWorker-" + i);
            threads.add(t);
            t.start();
        }

        dispatchLogger.accept(String.format("All %d worker threads launched and primed. Releasing start latch...", threadCount));
        long wallStart = System.currentTimeMillis();
        startSignal.countDown(); // Blast all threads at the exact same instant!

        // Wait for all threads to terminate
        for (Thread t : threads) {
            try {
                t.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        long wallElapsed = System.currentTimeMillis() - wallStart;
        dispatchLogger.accept(String.format("All threads finished in %d ms. Conducting data integrity audit...", wallElapsed));

        // Audit results
        Set<String> assignedPhysicalSeats = new HashSet<>();
        boolean duplicateFound = false;

        for (BookingTask task : tasks) {
            if (task.getResultBooking() != null) {
                Ticket t = task.getResultBooking().getTickets().get(0);
                if (t.getStatus() == BookingStatus.CONFIRMED) {
                    result.confirmedAllocations++;
                    String seatCode = t.getSeat().getFormattedSeatCode();
                    result.seatAssignedList.add(t.getPassenger().getName() + " -> " + seatCode);
                    if (!assignedPhysicalSeats.add(seatCode)) {
                        duplicateFound = true;
                        dispatchLogger.accept("[ERROR: RACE CONDITION] Duplicate seat detected: " + seatCode);
                    }
                } else if (t.getStatus() == BookingStatus.RAC) {
                    result.racAllocations++;
                    result.seatAssignedList.add(t.getPassenger().getName() + " -> RAC " + t.getRacPosition());
                } else if (t.getStatus() == BookingStatus.WAITING_LIST) {
                    result.waitingListAllocations++;
                    result.seatAssignedList.add(t.getPassenger().getName() + " -> WL " + t.getWlPosition());
                }
            } else {
                result.failedAttempts++;
            }
        }

        result.zeroDuplicateSeats = !duplicateFound;
        dispatchLogger.accept("=== AUDIT SUMMARY ===");
        dispatchLogger.accept(String.format("Total Threads Attempted : %d", result.totalThreads));
        dispatchLogger.accept(String.format("Confirmed Seats Assigned: %d (Max Cap: 4)", result.confirmedAllocations));
        dispatchLogger.accept(String.format("RAC Slots Assigned     : %d (Max Cap: 3)", result.racAllocations));
        dispatchLogger.accept(String.format("Waiting List Assigned   : %d (Max Cap: 3)", result.waitingListAllocations));
        dispatchLogger.accept(String.format("Capacity Exhausted (Regret): %d", result.failedAttempts));
        dispatchLogger.accept(String.format("Thread-Safety Verified (No Duplicates): %s", result.zeroDuplicateSeats ? "PASSED (100% SAFE)" : "FAILED"));

        return result;
    }
}
