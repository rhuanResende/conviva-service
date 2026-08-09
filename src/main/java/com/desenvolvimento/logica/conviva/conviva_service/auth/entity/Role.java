package com.desenvolvimento.logica.conviva.conviva_service.auth.entity;

import com.desenvolvimento.logica.conviva.conviva_service.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "tb_role", schema = "auth")
public class Role extends BaseEntity {

    @Column(name = "ds_name", nullable = false, length = 200)
    private String name;

    @Column(name = "ds_description", length = 255)
    private String description;

}
