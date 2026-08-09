package com.desenvolvimento.logica.conviva.conviva_service.auth.repository;

import com.desenvolvimento.logica.conviva.conviva_service.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
}
