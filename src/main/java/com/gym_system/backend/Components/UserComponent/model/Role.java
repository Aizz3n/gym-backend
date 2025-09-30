package com.gym_system.backend.Components.UserComponent.model;

import java.util.Locale;

public enum Role {
    USER,
    ADMIN;

    public static Role fromString(String role){
        if(role == null){
            throw new IllegalArgumentException("Role cannot be null");
        }

        try {
            return Role.valueOf(role.trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Role: " + role);
        }
    }

    public boolean hasAdminRole(){
        return this == ADMIN;
    }
}
