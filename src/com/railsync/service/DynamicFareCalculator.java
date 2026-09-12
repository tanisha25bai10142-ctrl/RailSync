package com.railsync.service;

import com.railsync.model.Passenger;
import com.railsync.model.SeatClass;
import com.railsync.model.Train;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Robust implementation of FareCalculator modeling real-world Indian Railways fare rules.
 * Encapsulates distance-based slab tariffs, class multipliers, superfast surcharges,
 * age concessions (Child, Senior Citizen, Student), and time-tiered cancellation refunds.
 */
public class DynamicFareCalculator implements FareCalculator {

    private static final double BASE_RATE_PER_KM = 0.55;

    @Override
    public double calculateFare(Train train, SeatClass seatClass, Passenger passenger) {
        if (train == null || seatClass == null || passenger == null) {
            throw new IllegalArgumentException("Train, SeatClass, and Passenger must not be null");
        }

        // 1. Distance & Train Rate
        double distance = train.getDistanceKm();
        double ratePerKm = Math.max(BASE_RATE_PER_KM, train.getBaseFareRatePerKm());
        double baseDistanceFare = distance * ratePerKm;

        // 2. Apply Seat Class Multiplier
        double classBaseFare = baseDistanceFare * seatClass.getFareMultiplier();

        // 3. Train Surcharges (e.g. Rajdhani catering, Superfast surcharge)
        double trainSurcharge = train.getSurcharge();

        // 4. Fixed Reservation Fee
        double reservationFee = seatClass.getReservationFee();

        // Subtotal before concession
        double grossFare = classBaseFare + trainSurcharge + reservationFee;

        // 5. Apply Passenger Concession
        Passenger.Concession concession = passenger.getConcession();
        double discount = 0.0;
        if (concession != null && concession.getDiscountRate() > 0.0) {
            // Discounts apply to the base class fare
            discount = classBaseFare * concession.getDiscountRate();
        }

        double netFare = grossFare - discount;
        // Superfast/luxury min fare floor
        double minimumFare = 50.0;
        if (concession == Passenger.Concession.INFANT) {
            return 0.0; // Infants travel free
        }
        netFare = Math.max(minimumFare, netFare);

        // Round to nearest 5 Rupees (Indian Railway convention)
        return Math.round(netFare / 5.0) * 5.0;
    }

    @Override
    public double[] calculateRefund(double originalFare, LocalDateTime journeyDepartureTime,
                                     LocalDateTime cancellationTime, boolean isRacOrWl) {
        if (originalFare <= 0) {
            return new double[]{0.0, 0.0};
        }

        // If ticket was RAC or Waiting List, IR charges a flat clerkage fee
        if (isRacOrWl) {
            double clerkageFee = 60.0;
            double refund = Math.max(0.0, originalFare - clerkageFee);
            return new double[]{refund, Math.min(originalFare, clerkageFee)};
        }

        Duration duration = Duration.between(cancellationTime, journeyDepartureTime);
        long hoursLeft = duration.toHours();

        double cancellationFee;
        if (hoursLeft > 48) {
            // Flat nominal fee (>48 hrs before departure)
            cancellationFee = Math.min(originalFare * 0.10, 180.0);
            if (cancellationFee < 60.0) cancellationFee = 60.0;
        } else if (hoursLeft >= 12) {
            // 25% cancellation charge (12 to 48 hrs)
            cancellationFee = originalFare * 0.25;
        } else if (hoursLeft >= 4) {
            // 50% cancellation charge (4 to 12 hrs)
            cancellationFee = originalFare * 0.50;
        } else {
            // Non-refundable (<4 hrs / chart prepared)
            cancellationFee = originalFare;
        }

        double netRefund = Math.max(0.0, originalFare - cancellationFee);
        return new double[]{netRefund, cancellationFee};
    }
}
