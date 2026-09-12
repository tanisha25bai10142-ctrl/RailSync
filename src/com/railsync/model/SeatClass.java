package com.railsync.model;

import java.io.Serializable;

/**
 * Enumerates train travel classes with distinct fare multipliers,
 * seat configurations, and codes.
 * Demonstrates enum with fields, constructors, and methods in Core Java.
 */
public enum SeatClass implements Serializable {
    FIRST_AC("1A", "AC First Class (1A)", 3.50, 60.0),
    SECOND_AC("2A", "AC 2 Tier (2A)", 2.40, 50.0),
    THIRD_AC("3A", "AC 3 Tier (3A)", 1.65, 40.0),
    CHAIR_CAR("CC", "AC Chair Car (CC)", 1.30, 40.0),
    SLEEPER("SL", "Sleeper Class (SL)", 1.00, 20.0);

    private final String code;
    private final String displayName;
    private final double fareMultiplier;
    private final double reservationFee;

    SeatClass(String code, String displayName, double fareMultiplier, double reservationFee) {
        this.code = code;
        this.displayName = displayName;
        this.fareMultiplier = fareMultiplier;
        this.reservationFee = reservationFee;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getFareMultiplier() {
        return fareMultiplier;
    }

    public double getReservationFee() {
        return reservationFee;
    }

    public static SeatClass fromCode(String code) {
        if (code == null) return SLEEPER;
        for (SeatClass sc : values()) {
            if (sc.code.equalsIgnoreCase(code.trim()) || sc.name().equalsIgnoreCase(code.trim())) {
                return sc;
            }
        }
        return SLEEPER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
