package com.railsync.util;

import com.railsync.exception.InvalidPassengerException;
import com.railsync.exception.InvalidStationException;
import com.railsync.model.Station;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Helper methods for validating passenger details, phone numbers, and station inputs.
 */
public final class ValidationUtils {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s.\\-]{2,50}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern PNR_PATTERN = Pattern.compile("^RS[A-Z0-9]{6,8}$");

    private ValidationUtils() {
        // Prevent instantiation of utility class
    }

    public static void validatePassenger(String name, int age, String phone) throws InvalidPassengerException {
        if (name == null || !NAME_PATTERN.matcher(name.trim()).matches()) {
            throw new InvalidPassengerException("Invalid passenger name: '" + name + "'. Name must contain 2-50 letters and spaces.");
        }
        if (age < 0 || age > 125) {
            throw new InvalidPassengerException("Invalid age: " + age + ". Age must be between 0 and 125.");
        }
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new InvalidPassengerException("Invalid mobile number: '" + phone + "'. Must be a 10-digit Indian phone number starting with 6-9.");
        }
    }

    public static void validateStations(Station source, Station destination) throws InvalidStationException {
        if (source == null || destination == null) {
            throw new InvalidStationException("Source and destination stations cannot be null.");
        }
        if (source.getCode().equalsIgnoreCase(destination.getCode())) {
            throw new InvalidStationException("Source and Destination stations cannot be identical (" + source.getCode() + ").");
        }
    }

    public static void validateJourneyDate(LocalDate date) throws IllegalArgumentException {
        if (date == null) {
            throw new IllegalArgumentException("Journey date cannot be null.");
        }
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new IllegalArgumentException("Journey date cannot be in the past: " + date);
        }
        if (date.isAfter(today.plusDays(120))) {
            throw new IllegalArgumentException("Advance Reservation Period is maximum 120 days from today.");
        }
    }

    public static boolean isValidPnr(String pnr) {
        return pnr != null && PNR_PATTERN.matcher(pnr.trim().toUpperCase()).matches();
    }
}
