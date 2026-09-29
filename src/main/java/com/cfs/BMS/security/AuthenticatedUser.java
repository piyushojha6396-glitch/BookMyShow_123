package com.cfs.BMS.security;

import com.cfs.BMS.enums.Role;

/** Principal placed in the SecurityContext for every authenticated request. */
public record AuthenticatedUser(Long id, String email, Role role) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
