package com.railsync.model;

import java.io.Serializable;

/**
 * Enumerates types of berths and seats available on Indian Railway coaches.
 */
public enum BerthType implements Serializable {
    LOWER("Lower Berth (LB)"),
    MIDDLE("Middle Berth (MB)"),
    UPPER("Upper Berth (UB)"),
    SIDE_LOWER("Side Lower (SLB)"),
    SIDE_UPPER("Side Upper (SUB)"),
    WINDOW("Window Seat (WS)"),
    AISLE("Aisle Seat (AS)");

    private final String label;

    BerthType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
