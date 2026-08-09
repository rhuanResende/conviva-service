package com.desenvolvimento.logica.conviva.conviva_service.common.dto;

import java.time.LocalDateTime;

public record UserResponse(
        String id,
        String name,
        String document,
        String email,
        String phone,
        String password,
        Boolean firstAccess,
        Boolean forcePasswordChange,
        Integer failedLoginAttempts,
        LocalDateTime lockedUntil,
        LocalDateTime lastLockAt,
        LocalDateTime passwordChangedAt,
        LocalDateTime passwordExpiresAt,
        String status,
        String role
) {
}
