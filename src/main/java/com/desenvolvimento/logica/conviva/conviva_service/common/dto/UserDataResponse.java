package com.desenvolvimento.logica.conviva.conviva_service.common.dto;

public record UserDataResponse(
        String id,
        String name,
        String document,
        String email,
        String phone,
        String profile,
        String status,
        Boolean firstAccess,
        Boolean forcePasswordChange
) {
}
