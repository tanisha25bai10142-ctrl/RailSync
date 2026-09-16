package com.railsync.service;

import com.railsync.model.Passenger;
import com.railsync.model.SeatClass;
import com.railsync.model.Train;

import java.time.LocalDateTime;

/**
 * Interface defining fare calculation and cancellation refund calculations.
 */
public interface FareCalculator {

    /**
     * Calculates the individual passenger fare considering distance,
     * train category surcharge, class multiplier, and age/concession discounts.
     */
    double calculateFare(Train train, SeatClass seatClass, Passenger passenger);

    /**
     * Calculates the refund amount upon cancellation based on the hours left before departure.
     *
     * @param originalFare the fare initially paid
     * @param journeyDepartureTime the scheduled journey departure timestamp
     * @param cancellationTime the current cancellation timestamp
     * @param isRacOrWl whether the ticket was RAC or Waiting List
     * @return array of [netRefundAmount, cancellationFee]
     */
    double[] calculateRefund(double originalFare, LocalDateTime journeyDepartureTime,
                             LocalDateTime cancellationTime, boolean isRacOrWl);
}
