package com.desenvolvimento.logica.conviva.conviva_service.auth.mapper;

import com.desenvolvimento.logica.conviva.conviva_service.auth.entity.RefreshToken;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class RefreshTokenMapper {

    public RefreshToken toEntity(UUID userId, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userId);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        return refreshToken;
    }

}
