package com.desenvolvimento.logica.conviva.conviva_service.auth.entity;

import com.desenvolvimento.logica.conviva.conviva_service.auth.enums.SecurityEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tb_security_event", schema = "auth")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "co_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "co_user", nullable = false)
    private UUID user;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_event", nullable = false)
    private SecurityEventType event;

    @Column(name = "ds_message", length = 255)
    private String message;

    @Column(name = "dt_created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
