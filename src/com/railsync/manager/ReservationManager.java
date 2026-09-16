package com.railsync.manager;

import com.railsync.exception.*;
import com.railsync.model.*;
import com.railsync.service.DynamicFareCalculator;
import com.railsync.service.FareCalculator;
import com.railsync.util.PNRGenerator;
import com.railsync.util.ValidationUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages train reservations, seat allocations, cancellations, and user sessions.
 */
public class ReservationManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Train> trainMap;
    private final Map<String, Station> stationMap;
    private final Map<String, Booking> bookingMap;
    private final Map<String, User> userMap;
    private final Set<String> existingPnrs;
    private final WaitingListManager waitingListManager;
    private final FareCalculator fareCalculator;

    public ReservationManager() {
        this.trainMap = new ConcurrentHashMap<>();
        this.stationMap = new ConcurrentHashMap<>();
        this.bookingMap = new ConcurrentHashMap<>();
        this.userMap = new ConcurrentHashMap<>();
        this.existingPnrs = Collections.synchronizedSet(new HashSet<>());
        this.waitingListManager = new WaitingListManager();
        this.fareCalculator = new DynamicFareCalculator();
    }

    // ================= Train & Station Registries =================

    public void addStation(Station station) {
        if (station != null) {
            stationMap.put(station.getCode().toUpperCase(), station);
        }
    }

    public void removeStation(String code) {
        if (code != null) {
            stationMap.remove(code.toUpperCase());
        }
    }

    public Station getStation(String code) {
        if (code == null) return null;
        return stationMap.get(code.toUpperCase());
    }

    public Collection<Station> getAllStations() {
        return Collections.unmodifiableCollection(stationMap.values());
    }

    public void addTrain(Train train) {
        if (train != null) {
            trainMap.put(train.getTrainNumber(), train);
        }
    }

    public void removeTrain(String trainNumber) {
        if (trainNumber != null) {
            trainMap.remove(trainNumber);
        }
    }

    public Train getTrain(String trainNumber) {
        if (trainNumber == null) return null;
        return trainMap.get(trainNumber);
    }

    public Collection<Train> getAllTrains() {
        return Collections.unmodifiableCollection(trainMap.values());
    }

    public Map<String, Booking> getBookingMap() {
        return bookingMap;
    }

    public WaitingListManager getWaitingListManager() {
        return waitingListManager;
    }

    public FareCalculator getFareCalculator() {
        return fareCalculator;
    }

    // ================= User Management =================

    public void registerUser(User user) {
        if (user != null) {
            userMap.put(user.getUsername().toLowerCase(), user);
        }
    }

    public User authenticateUser(String username, String password) {
        if (username == null || password == null) return null;
        User u = userMap.get(username.trim().toLowerCase());
        if (u != null && u.verifyPassword(password)) {
            return u;
        }
        return null;
    }

    public User getUser(String username) {
        return username != null ? userMap.get(username.trim().toLowerCase()) : null;
    }

    // ================= Dynamic Seat Allocation (Thread-Safe) =================

    /**
     * Books a ticket for one or more passengers in a single transaction.
     * Synchronized on the train instance to guarantee absolute thread safety
     * during concurrent booking requests.
     */
    public Booking bookTicket(String trainNumber, LocalDate journeyDate, SeatClass seatClass,
                              List<Passenger> passengers, String userId)
            throws TrainNotFoundException, InvalidBookingException, InvalidPassengerException, SeatUnavailableException {

        Train train = getTrain(trainNumber);
        if (train == null) {
            throw new TrainNotFoundException("Train with number '" + trainNumber + "' was not found.");
        }

        ValidationUtils.validateJourneyDate(journeyDate);

        if (passengers == null || passengers.isEmpty()) {
            throw new InvalidBookingException("A booking must contain at least one passenger.");
        }

        if (passengers.size() > 6) {
            throw new InvalidBookingException("Maximum 6 passengers allowed per booking transaction.");
        }

        for (Passenger p : passengers) {
            ValidationUtils.validatePassenger(p.getName(), p.getAge(), p.getPhone());
        }

        // Synchronize on the target train to prevent concurrent race conditions
        synchronized (train) {
            String queueKey = waitingListManager.makeQueueKey(trainNumber, seatClass, journeyDate);
            String pnr = PNRGenerator.generateUniquePNR(existingPnrs);
            String bookingId = "BK" + (10000 + bookingMap.size() + 1);

            Booking booking = new Booking(bookingId, pnr, train.getTrainNumber(), train.getTrainName(),
                    train.getSource(), train.getDestination(), journeyDate, seatClass, userId);

            int racCapacity = train.getRacCapacity(seatClass);
            int wlCapacity = train.getWaitingListCapacity(seatClass);

            int ticketIndex = 1;
            for (Passenger passenger : passengers) {
                String ticketId = "TK-" + pnr + "-" + (ticketIndex++);
                double fare = fareCalculator.calculateFare(train, seatClass, passenger);

                // 1. Try assigning Confirmed seat
                Seat availableSeat = findSeatForClass(train, seatClass);
                if (availableSeat != null) {
                    availableSeat.setBooked(true);
                    Ticket ticket = new Ticket(ticketId, pnr, passenger, seatClass,
                            BookingStatus.CONFIRMED, availableSeat, 0, 0, fare);
                    booking.addTicket(ticket);
                    continue;
                }

                // 2. Confirmed seats full -> check RAC queue
                int currentRacCount = waitingListManager.getRacCount(queueKey);
                if (currentRacCount < racCapacity) {
                    Ticket ticket = new Ticket(ticketId, pnr, passenger, seatClass,
                            BookingStatus.RAC, null, 0, 0, fare);
                    waitingListManager.addToRac(queueKey, ticket);
                    booking.addTicket(ticket);
                    continue;
                }

                // 3. RAC full -> check Waiting List queue
                int currentWlCount = waitingListManager.getWaitingListCount(queueKey);
                if (currentWlCount < wlCapacity) {
                    Ticket ticket = new Ticket(ticketId, pnr, passenger, seatClass,
                            BookingStatus.WAITING_LIST, null, 0, 0, fare);
                    waitingListManager.addToWaitingList(queueKey, ticket);
                    booking.addTicket(ticket);
                    continue;
                }

                // 4. All capacities exhausted -> REGRET
                throw new SeatUnavailableException("Booking failed: All Confirmed, RAC, and Waiting List seats " +
                        "are completely exhausted for " + seatClass.getDisplayName() + " on train " + train.getTrainNumber());
            }

            bookingMap.put(pnr, booking);
            return booking;
        }
    }

    private Seat findSeatForClass(Train train, SeatClass seatClass) {
        for (Coach coach : train.getCoaches()) {
            if (coach.getSeatClass() == seatClass) {
                Seat s = coach.findNextAvailableSeat();
                if (s != null) {
                    return s;
                }
            }
        }
        return null;
    }

    // ================= Cancellation & Promotion =================

    /**
     * Cancels a specific ticket within a booking, computes refunds, and executes
     * the automatic RAC / Waiting List promotion cascade.
     */
    public synchronized RefundReceipt cancelTicket(String pnr, String ticketId)
            throws InvalidPNRException, CancellationNotAllowedException, TrainNotFoundException {

        if (pnr == null || !bookingMap.containsKey(pnr.trim().toUpperCase())) {
            throw new InvalidPNRException("No booking found with PNR: " + pnr);
        }

        Booking booking = bookingMap.get(pnr.trim().toUpperCase());
        Train train = getTrain(booking.getTrainNumber());
        if (train == null) {
            throw new TrainNotFoundException("Train not found for booking: " + booking.getTrainNumber());
        }

        Ticket targetTicket = null;
        for (Ticket t : booking.getTickets()) {
            if (t.getTicketId().equalsIgnoreCase(ticketId)) {
                targetTicket = t;
                break;
            }
        }

        if (targetTicket == null) {
            throw new CancellationNotAllowedException("Ticket ID '" + ticketId + "' not found under PNR " + pnr);
        }

        if (targetTicket.isCancelled()) {
            throw new CancellationNotAllowedException("Ticket '" + ticketId + "' is already cancelled.");
        }

        synchronized (train) {
            String queueKey = waitingListManager.makeQueueKey(
                    train.getTrainNumber(), booking.getSeatClass(), booking.getJourneyDate());

            boolean isRacOrWl = (targetTicket.getStatus() == BookingStatus.RAC ||
                    targetTicket.getStatus() == BookingStatus.WAITING_LIST);

            // Execute automatic promotion cascade
            String promotionLog = waitingListManager.handleCancellationPromotion(queueKey, targetTicket, train);

            // Mark cancelled
            targetTicket.setCancelled(true);

            // Calculate refund
            LocalDateTime departureDateTime = LocalDateTime.of(booking.getJourneyDate(), train.getDepartureTime());
            double[] refundBreakdown = fareCalculator.calculateRefund(
                    targetTicket.getIndividualFare(), departureDateTime, LocalDateTime.now(), isRacOrWl);

            double netRefund = refundBreakdown[0];
            double cancellationFee = refundBreakdown[1];

            RefundReceipt receipt = new RefundReceipt(
                    booking.getPnr(),
                    targetTicket.getPassenger().getName(),
                    train.getTrainNumber(),
                    targetTicket.getIndividualFare(),
                    cancellationFee,
                    netRefund,
                    promotionLog.isEmpty() ? "Cancellation processed successfully." : promotionLog
            );

            // Check if all tickets cancelled
            if (booking.isAllCancelled()) {
                booking.setCancelled(true);
            }

            return receipt;
        }
    }
}
