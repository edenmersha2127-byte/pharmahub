package com.medicinelocator.auth.application.command;

import com.medicinelocator.auth.domain.enums.Role;

import java.util.UUID;

public class ChangePasswordCommand {

    private final UUID userId;
    private final Role role;
    private final String currentPassword;
    private final String newPassword;

    public ChangePasswordCommand(UUID userId, Role role, String currentPassword, String newPassword) {
        this.userId = userId;
        this.role = role;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public UUID getUserId() { return userId; }
    public Role getRole() { return role; }
    public String getCurrentPassword() { return currentPassword; }
    public String getNewPassword() { return newPassword; }
}
