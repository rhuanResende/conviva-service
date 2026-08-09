package com.desenvolvimento.logica.conviva.conviva_service.user.repository;

import com.desenvolvimento.logica.conviva.conviva_service.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findUserByDocumentAndActiveTrue(String document);
    User findUserByIdAndActiveTrue(UUID id);
}
