package com.railsync.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a railway station along a train route.
 */
public class Station implements Serializable, Comparable<Station> {
    private static final long serialVersionUID = 1L;

    private final String code;          // e.g. "NDLS", "BCT"
    private final String name;          // e.g. "New Delhi", "Mumbai Central"
    private final String city;
    private final String state;
    private final String zone;          // e.g. "NR", "WR", "ER"
    private final double distanceMarkerKm;

    public Station(String code, String name, String city, String state, String zone, double distanceMarkerKm) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Station code cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.city = city != null ? city.trim() : "";
        this.state = state != null ? state.trim() : "";
        this.zone = zone != null ? zone.trim().toUpperCase() : "IR";
        this.distanceMarkerKm = Math.max(0, distanceMarkerKm);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZone() {
        return zone;
    }

    public double getDistanceMarkerKm() {
        return distanceMarkerKm;
    }

    @Override
    public int compareTo(Station other) {
        if (other == null) return 1;
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return code.equalsIgnoreCase(station.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code.toUpperCase());
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
