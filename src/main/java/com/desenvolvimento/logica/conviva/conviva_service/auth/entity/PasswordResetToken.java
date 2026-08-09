package com.desenvolvimento.logica.conviva.conviva_service.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tb_password_reset_token", schema = "auth")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "co_id")
    private UUID id;

    @Column(name = "co_user", columnDefinition = "uuid", nullable = false)
    private UUID user;

    @Column(name = "ds_token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "dt_expiration", nullable = false)
    private LocalDateTime expiration;

    @Column(name = "st_used", nullable = false)
    private boolean used;

    @Column(name = "dt_used_at", nullable = false)
    private LocalDateTime usedAt;

    @Column(name = "dt_created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        used = false;
        createdAt = LocalDateTime.now();
    }
}
