package com.desenvolvimento.logica.conviva.conviva_service.user.entity;

import com.desenvolvimento.logica.conviva.conviva_service.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tb_user", schema = "users")
public class User extends BaseEntity {

    @Column(name = "ds_name", nullable = false, length = 200)
    private String name;

    @Column(name = "nr_document", nullable = false, length = 11)
    private String document;

    @Column(name = "ds_email", nullable = false, length = 200)
    private String email;

    @Column(name = "ds_phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "ds_password", nullable = false)
    private String password;

    @Column(name = "st_first_access", nullable = false)
    private Boolean firstAccess;

    @Column(name = "st_force_password_change", nullable = false)
    private Boolean forcePasswordChange;

    @Column(name = "nr_failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts;

    @Column(name = "dt_locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "dt_last_lock_at")
    private LocalDateTime lastLockedAt;

    @Column(name = "dt_password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "dt_password_expires_at")
    private LocalDateTime passwordExpiresAt;

}
