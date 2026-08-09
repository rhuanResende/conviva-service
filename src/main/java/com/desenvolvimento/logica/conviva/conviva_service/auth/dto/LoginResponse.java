package com.desenvolvimento.logica.conviva.conviva_service.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        boolean firstAccess
) {
}
