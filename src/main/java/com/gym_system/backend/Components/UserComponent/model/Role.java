package com.gym_system.backend.Components.UserComponent.model;

import java.util.Locale;

public enum Role {
    USER,
    ADMIN;

    public static Role fromString(String role) {
        if (role == null || role.trim().isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or blank");
        }

        try {
            return Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or unsupported role: " + role);
        }
    }

    public boolean is(Role other) {
        return this == other;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}