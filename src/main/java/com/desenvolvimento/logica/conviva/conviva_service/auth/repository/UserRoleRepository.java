package com.desenvolvimento.logica.conviva.conviva_service.auth.repository;


import com.desenvolvimento.logica.conviva.conviva_service.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    UserRole findUserRoleByUserAndActiveTrue(UUID userId);
}
