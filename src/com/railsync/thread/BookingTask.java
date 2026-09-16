package com.railsync.thread;

import com.railsync.manager.ReservationManager;
import com.railsync.model.BerthType;
import com.railsync.model.Booking;
import com.railsync.model.Passenger;
import com.railsync.model.SeatClass;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Task executed by a thread to simulate concurrent ticket booking.
 */
public class BookingTask implements Runnable {

    private final String taskName;
    private final String passengerName;
    private final String trainNumber;
    private final LocalDate journeyDate;
    private final SeatClass seatClass;
    private final ReservationManager manager;
    private final Consumer<String> eventLogger;
    private Booking resultBooking;
    private Exception error;

    public BookingTask(String taskName, String passengerName, String trainNumber,
                       LocalDate journeyDate, SeatClass seatClass, ReservationManager manager,
                       Consumer<String> eventLogger) {
        this.taskName = taskName;
        this.passengerName = passengerName;
        this.trainNumber = trainNumber;
        this.journeyDate = journeyDate;
        this.seatClass = seatClass;
        this.manager = manager;
        this.eventLogger = eventLogger;
    }

    @Override
    public void run() {
        try {
            log(taskName + " starting booking for " + passengerName + " on train " + trainNumber);

            List<Passenger> passengers = Collections.singletonList(
                    new Passenger(passengerName, 28, "Male", "9876543210", BerthType.LOWER));

            resultBooking = manager.bookTicket(trainNumber, journeyDate, seatClass, passengers, taskName);

            String status = resultBooking.getTickets().get(0).getStatusDescription();
            log(taskName + " booked successfully: PNR=" + resultBooking.getPnr() + " (" + status + ")");

        } catch (Exception e) {
            this.error = e;
            log(taskName + " could not book: " + e.getMessage());
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
