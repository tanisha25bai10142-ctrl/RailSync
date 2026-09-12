package com.railsync.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an authenticated user in the RailSync system (Passenger or Administrator).
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role {
        PASSENGER,
        ADMIN
    }

    private final String userId;
    private final String username;
    private String password;
    private final String fullName;
    private final String email;
    private final String phone;
    private final Role role;

    public User(String userId, String username, String password, String fullName, String email, String phone, Role role) {
        this.userId = Objects.requireNonNull(userId);
        this.username = Objects.requireNonNull(username).toLowerCase().trim();
        this.password = Objects.requireNonNull(password);
        this.fullName = fullName != null ? fullName.trim() : username;
        this.email = email != null ? email.trim() : "";
        this.phone = phone != null ? phone.trim() : "";
        this.role = role != null ? role : Role.PASSENGER;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public boolean verifyPassword(String attempt) {
        return attempt != null && this.password.equals(attempt);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + " - " + username + ")";
    }
}
