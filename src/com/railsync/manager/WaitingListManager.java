package com.railsync.manager;

import com.railsync.model.*;

import java.io.Serializable;
import java.util.*;

/**
 * Manages FIFO queues for Reservation Against Cancellation (RAC) and Waiting List (WL),
 * including automatic promotion when tickets are cancelled.
 */
public class WaitingListManager implements Serializable {
    private static final long serialVersionUID = 1L;

    // Key format: trainNumber + "_" + seatClassCode + "_" + journeyDate
    private final Map<String, Queue<Ticket>> racQueues;
    private final Map<String, Queue<Ticket>> waitingListQueues;

    public WaitingListManager() {
        this.racQueues = new HashMap<>();
        this.waitingListQueues = new HashMap<>();
    }

    public synchronized String makeQueueKey(String trainNumber, SeatClass seatClass, java.time.LocalDate date) {
        return trainNumber + "_" + seatClass.getCode() + "_" + date.toString();
    }

    public synchronized Queue<Ticket> getRacQueue(String key) {
        return racQueues.computeIfAbsent(key, k -> new LinkedList<>());
    }

    public synchronized Queue<Ticket> getWaitingListQueue(String key) {
        return waitingListQueues.computeIfAbsent(key, k -> new LinkedList<>());
    }

    public synchronized int getRacCount(String key) {
        Queue<Ticket> q = racQueues.get(key);
        return q != null ? q.size() : 0;
    }

    public synchronized int getWaitingListCount(String key) {
        Queue<Ticket> q = waitingListQueues.get(key);
        return q != null ? q.size() : 0;
    }

    public synchronized void addToRac(String key, Ticket ticket) {
        Queue<Ticket> q = getRacQueue(key);
        ticket.setStatus(BookingStatus.RAC);
        ticket.setRacPosition(q.size() + 1);
        ticket.setWlPosition(0);
        q.add(ticket);
    }

    public synchronized void addToWaitingList(String key, Ticket ticket) {
        Queue<Ticket> q = getWaitingListQueue(key);
        ticket.setStatus(BookingStatus.WAITING_LIST);
        ticket.setWlPosition(q.size() + 1);
        ticket.setRacPosition(0);
        q.add(ticket);
    }

    /**
     * Executes the realistic IR cancellation promotion cascade.
     *
     * @param key queue lookup key
     * @param cancelledTicket the ticket being cancelled
     * @param train the train instance
     * @return summary message detailing any promotions executed
     */
    public synchronized String handleCancellationPromotion(String key, Ticket cancelledTicket, Train train) {
        StringBuilder report = new StringBuilder();
        Queue<Ticket> racQ = getRacQueue(key);
        Queue<Ticket> wlQ = getWaitingListQueue(key);

        BookingStatus originalStatus = cancelledTicket.getStatus();

        if (originalStatus == BookingStatus.CONFIRMED) {
            Seat freedSeat = cancelledTicket.getSeat();

            // 1. Promote head of RAC to Confirmed if RAC queue is not empty
            if (!racQ.isEmpty()) {
                Ticket racHead = racQ.poll();
                racHead.setStatus(BookingStatus.CONFIRMED);
                racHead.setSeat(freedSeat);
                racHead.setRacPosition(0);
                if (freedSeat != null) {
                    freedSeat.setBooked(true);
                    freedSeat.setRacOccupied(false);
                }
                report.append(String.format("RAC passenger '%s' (PNR: %s) PROMOTED to CONFIRMED (Assigned %s). ",
                        racHead.getPassenger().getName(), racHead.getPnr(),
                        freedSeat != null ? freedSeat.getFormattedSeatCode() : "Seat"));

                // Re-index remaining RAC queue
                reindexQueue(racQ, true);

                // 2. Promote head of Waiting List to RAC
                if (!wlQ.isEmpty()) {
                    Ticket wlHead = wlQ.poll();
                    wlHead.setStatus(BookingStatus.RAC);
                    wlHead.setWlPosition(0);
                    wlHead.setRacPosition(racQ.size() + 1);
                    racQ.add(wlHead);

                    report.append(String.format("Waiting List passenger '%s' (PNR: %s) PROMOTED to RAC %d. ",
                            wlHead.getPassenger().getName(), wlHead.getPnr(), wlHead.getRacPosition()));

                    // Re-index remaining Waiting list queue
                    reindexQueue(wlQ, false);
                }
            } else {
                // RAC queue was empty, so the seat simply becomes physically available in the coach
                if (freedSeat != null) {
                    freedSeat.setBooked(false);
                    freedSeat.setRacOccupied(false);
                }
                report.append("Seat released back to general availability inventory. ");
            }

        } else if (originalStatus == BookingStatus.RAC) {
            // Remove cancelled ticket from RAC queue
            racQ.remove(cancelledTicket);
            reindexQueue(racQ, true);

            // Promote head of Waiting List to RAC
            if (!wlQ.isEmpty()) {
                Ticket wlHead = wlQ.poll();
                wlHead.setStatus(BookingStatus.RAC);
                wlHead.setWlPosition(0);
                wlHead.setRacPosition(racQ.size() + 1);
                racQ.add(wlHead);

                report.append(String.format("Waiting List passenger '%s' (PNR: %s) PROMOTED to RAC %d. ",
                        wlHead.getPassenger().getName(), wlHead.getPnr(), wlHead.getRacPosition()));

                reindexQueue(wlQ, false);
            }
        } else if (originalStatus == BookingStatus.WAITING_LIST) {
            // Remove cancelled ticket from Waiting List queue
            wlQ.remove(cancelledTicket);
            reindexQueue(wlQ, false);
            report.append("Passenger removed from Waiting List queue; subsequent positions updated. ");
        }

        return report.toString().trim();
    }

    private synchronized void reindexQueue(Queue<Ticket> queue, boolean isRac) {
        int pos = 1;
        for (Ticket t : queue) {
            if (isRac) {
                t.setRacPosition(pos++);
            } else {
                t.setWlPosition(pos++);
            }
        }
    }

    public synchronized void reset() {
        racQueues.clear();
        waitingListQueues.clear();
    }
}
