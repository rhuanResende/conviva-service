package com.desenvolvimento.logica.conviva.conviva_service.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "tb_refresh_token", schema = "auth")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "co_id", columnDefinition = "uuid", nullable = false)
    private UUID id;

    @Column(name = "co_user", columnDefinition = "uuid", nullable = false)
    private UUID user;

    @Column(name = "ds_token_hash", nullable = false, length = 500)
    private String token;

    @Column(name = "dt_expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "st_revoked", nullable = false)
    private boolean revoked;

    @Column(name = "dt_revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    @Column(name = "ds_device", length = 200)
    private String device;

    @Column(name = "ds_ip_address", length = 50)
    private String ipAddress;

    @Column(name = "dt_created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }

}
