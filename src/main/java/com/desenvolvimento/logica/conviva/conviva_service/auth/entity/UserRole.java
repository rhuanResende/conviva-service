package com.desenvolvimento.logica.conviva.conviva_service.auth.entity;

import com.desenvolvimento.logica.conviva.conviva_service.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tb_user_role", schema = "auth")
public class UserRole extends BaseEntity {

    @Column(name = "co_user", columnDefinition = "uuid", nullable = false)
    private UUID user;

    @Column(name = "co_role", columnDefinition = "uuid", nullable = false)
    private UUID role;

}

