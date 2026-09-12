package com.railsync.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an individual passenger booked on a train.
 * Encapsulates personal information, concession eligibility, and berth preference.
 */
public class Passenger implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Concession {
        NONE("General / No Concession", 0.0),
        CHILD("Child (5-11 yrs, 50% discount)", 0.50),
        INFANT("Infant (<5 yrs, Free)", 1.00),
        SENIOR_CITIZEN("Senior Citizen (40% discount)", 0.40),
        STUDENT("Student (25% discount)", 0.25);

        private final String description;
        private final double discountRate;

        Concession(String description, double discountRate) {
            this.description = description;
            this.discountRate = discountRate;
        }

        public String getDescription() {
            return description;
        }

        public double getDiscountRate() {
            return discountRate;
        }
    }

    private final String name;
    private final int age;
    private final String gender;
    private final String phone;
    private final Concession concession;
    private final BerthType berthPreference;

    public Passenger(String name, int age, String gender, String phone, BerthType berthPreference) {
        this(name, age, gender, phone, determineAutoConcession(age, gender), berthPreference);
    }

    public Passenger(String name, int age, String gender, String phone, Concession concession, BerthType berthPreference) {
        if (name == null || name.trim().length() < 2) {
            throw new IllegalArgumentException("Passenger name must have at least 2 characters");
        }
        if (age < 0 || age > 125) {
            throw new IllegalArgumentException("Age must be between 0 and 125");
        }
        this.name = name.trim();
        this.age = age;
        this.gender = (gender != null && !gender.trim().isEmpty()) ? gender.trim() : "Other";
        this.phone = (phone != null) ? phone.trim() : "";
        this.concession = concession != null ? concession : determineAutoConcession(age, this.gender);
        this.berthPreference = berthPreference;
    }

    public static Concession determineAutoConcession(int age, String gender) {
        if (age < 5) {
            return Concession.INFANT;
        } else if (age <= 11) {
            return Concession.CHILD;
        } else if (("Male".equalsIgnoreCase(gender) && age >= 60) ||
                ("Female".equalsIgnoreCase(gender) && age >= 58)) {
            return Concession.SENIOR_CITIZEN;
        }
        return Concession.NONE;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public Concession getConcession() {
        return concession;
    }

    public BerthType getBerthPreference() {
        return berthPreference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return age == passenger.age &&
                Objects.equals(name.toLowerCase(), passenger.name.toLowerCase()) &&
                Objects.equals(phone, passenger.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), age, phone);
    }

    @Override
    public String toString() {
        return name + " (" + age + "/" + gender.charAt(0) + ") - Concession: " + concession;
    }
}
