package com.railsync.thread;

import com.railsync.manager.ReservationManager;
import com.railsync.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;

/**
 * Simulates concurrent ticket booking across multiple threads.
 * Demonstrates thread creation, synchronization, and prevention of race conditions.
 */
public class ConcurrencySimulation {

    public static class SimulationResult {
        public int totalThreads;
        public int confirmedAllocations;
        public int racAllocations;
        public int waitingListAllocations;
        public int failedAttempts;
        public boolean zeroDuplicateSeats;
        public List<String> seatAssignedList = new ArrayList<>();
        public List<String> logs = new ArrayList<>();
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

        dispatchLogger.accept("=== Multithreaded Booking Simulation ===");
        dispatchLogger.accept("Creating test train with limited capacity (4 Confirmed, 3 RAC, 3 Waiting List)...");

        ReservationManager simManager = new ReservationManager();
        Station source = new Station("DEL", "Delhi", "Delhi", "Delhi", "NR", 0.0);
        Station dest = new Station("MUM", "Mumbai", "Mumbai", "Maharashtra", "WR", 1400.0);
        simManager.addStation(source);
        simManager.addStation(dest);

        SuperfastExpress testTrain = new SuperfastExpress("99999", "Concurrency Express",
                source, dest, LocalTime.of(10, 0), LocalTime.of(20, 0), 1400.0);
        Coach testCoach = new Coach("C1", SeatClass.CHAIR_CAR, 4, 3, 3);
        testTrain.addCoach(testCoach);
        simManager.addTrain(testTrain);

        LocalDate testDate = LocalDate.now().plusDays(2);
        List<BookingTask> tasks = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (int i = 1; i <= threadCount; i++) {
            String passengerName = "Passenger-" + (char) ('A' + (i - 1));
            BookingTask task = new BookingTask("Thread-" + i, passengerName, "99999",
                    testDate, SeatClass.CHAIR_CAR, simManager, dispatchLogger);
            tasks.add(task);

            Thread t = new Thread(task, "BookingWorker-" + i);
            threads.add(t);
        }

        dispatchLogger.accept("Starting " + threadCount + " booking threads simultaneously...");
        for (Thread t : threads) {
            t.start();
        }

        // Wait for all threads to finish
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        dispatchLogger.accept("All threads finished. Checking seat allocations...");

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
                        dispatchLogger.accept("[ERROR] Duplicate seat assigned: " + seatCode);
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
        dispatchLogger.accept("=== Simulation Results ===");
        dispatchLogger.accept("Total threads: " + result.totalThreads);
        dispatchLogger.accept("Confirmed seats assigned: " + result.confirmedAllocations + " (Max: 4)");
        dispatchLogger.accept("RAC seats assigned: " + result.racAllocations + " (Max: 3)");
        dispatchLogger.accept("Waiting list assigned: " + result.waitingListAllocations + " (Max: 3)");
        dispatchLogger.accept("Failed / Exhausted: " + result.failedAttempts);
        dispatchLogger.accept("Thread safety check: " + (result.zeroDuplicateSeats ? "PASSED (No duplicates)" : "FAILED (Duplicate seats detected)"));

        return result;
    }
}
