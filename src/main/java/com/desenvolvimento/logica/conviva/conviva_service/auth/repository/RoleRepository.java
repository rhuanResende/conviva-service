package com.desenvolvimento.logica.conviva.conviva_service.auth.repository;

import com.desenvolvimento.logica.conviva.conviva_service.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findRoleByName(String name);
    Role findRoleById(UUID id);
}
