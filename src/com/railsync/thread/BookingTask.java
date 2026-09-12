package com.railsync.thread;

import com.railsync.manager.ReservationManager;
import com.railsync.model.BerthType;
import com.railsync.model.Booking;
import com.railsync.model.Passenger;
import com.railsync.model.SeatClass;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Runnable task simulating an individual concurrent passenger booking attempt.
 * Demonstrates:
 * - java.lang.Runnable interface
 * - Thread lifecycle & concurrency
 * - CountDownLatch for synchronized thread blast start
 * - Thread-safe logging callbacks
 */
public class BookingTask implements Runnable {

    private final String taskName;
    private final String passengerName;
    private final String trainNumber;
    private final LocalDate journeyDate;
    private final SeatClass seatClass;
    private final ReservationManager manager;
    private final CountDownLatch startSignal;
    private final Consumer<String> eventLogger;
    private Booking resultBooking;
    private Exception error;

    public BookingTask(String taskName, String passengerName, String trainNumber,
                       LocalDate journeyDate, SeatClass seatClass, ReservationManager manager,
                       CountDownLatch startSignal, Consumer<String> eventLogger) {
        this.taskName = taskName;
        this.passengerName = passengerName;
        this.trainNumber = trainNumber;
        this.journeyDate = journeyDate;
        this.seatClass = seatClass;
        this.manager = manager;
        this.startSignal = startSignal;
        this.eventLogger = eventLogger;
    }

    @Override
    public void run() {
        try {
            // Wait for all threads to align at the starting gate for true concurrency
            if (startSignal != null) {
                startSignal.await();
            }

            log(String.format("[THREAD START] %s (%s) acquiring lock on Train %s...",
                    taskName, Thread.currentThread().getName(), trainNumber));

            long startTime = System.currentTimeMillis();
            List<Passenger> passengers = Collections.singletonList(
                    new Passenger(passengerName, 30, "Male", "9876543210", BerthType.LOWER));

            resultBooking = manager.bookTicket(trainNumber, journeyDate, seatClass, passengers, taskName);

            long elapsed = System.currentTimeMillis() - startTime;
            String status = resultBooking.getTickets().get(0).getStatusDescription();

            log(String.format("[SUCCESS - %dms] %s secured booking: PNR=%s | Status=%s",
                    elapsed, taskName, resultBooking.getPnr(), status));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.error = e;
            log("[ABORTED] " + taskName + " was interrupted.");
        } catch (Exception e) {
            this.error = e;
            log(String.format("[FAILED] %s booking failed: %s", taskName, e.getMessage()));
        }
    }

    private void log(String message) {
        if (eventLogger != null) {
            eventLogger.accept(message);
        }
    }

    public String getTaskName() {
        return taskName;
    }

    public Booking getResultBooking() {
        return resultBooking;
    }

    public Exception getError() {
        return error;
    }
}
