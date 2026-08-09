package com.desenvolvimento.logica.conviva.conviva_service.user.service;

import com.desenvolvimento.logica.conviva.conviva_service.auth.enums.SecurityEventType;
import com.desenvolvimento.logica.conviva.conviva_service.auth.service.SecurityEventService;
import com.desenvolvimento.logica.conviva.conviva_service.auth.service.UserRoleService;
import com.desenvolvimento.logica.conviva.conviva_service.common.dto.UserResponse;
import com.desenvolvimento.logica.conviva.conviva_service.common.exception.UnauthorizedException;
import com.desenvolvimento.logica.conviva.conviva_service.user.entity.User;
import com.desenvolvimento.logica.conviva.conviva_service.user.mapper.UserMapper;
import com.desenvolvimento.logica.conviva.conviva_service.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final SecurityEventService securityEventService;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       UserRoleService userRoleService,
                       SecurityEventService securityEventService,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userRoleService = userRoleService;
        this.securityEventService = securityEventService;
        this.userMapper = userMapper;
    }

    public UserResponse findUserByDocument(final String document) {

        User user = userRepository.findUserByDocumentAndActiveTrue(document);

        if (user == null) {
            throw new UnauthorizedException(
                    "Usuário não encontrado."
            );
        }

        return userMapper.toDTO(user, userRoleService.findRoleByUser(user.getId()));
    }

    @Transactional
    public void registerFailedLoginAttempt(UUID userId) {

        User user = getUserByIdAndActiveTrue(userId);

        int attempts =
                Optional.ofNullable(user.getFailedLoginAttempts())
                        .orElse(0);

        attempts++;

        user.setFailedLoginAttempts(attempts);

        if (attempts >= 5) {

            if (user.getLastLockedAt() != null &&
                    user.getLastLockedAt().isAfter(LocalDateTime.now().minusHours(24))) {
                user.setForcePasswordChange(true);

                securityEventService.register(
                        userId,
                        SecurityEventType.FORCE_PASSWORD_CHANGE,
                        "Troca de senha obrigatória");
            }

            user.setLastLockedAt(LocalDateTime.now());

            user.setLockedUntil(
                    LocalDateTime.now().plusMinutes(15)
            );

            securityEventService.register(userId, SecurityEventType.ACCOUNT_LOCKED, "Conta bloqueada temporariamente.");
        }

        userRepository.save(user);
    }

    @Transactional
    public void resetFailedLoginAttempts(UUID userId) {

        User user = getUserByIdAndActiveTrue(userId);

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        userRepository.save(user);
    }

    public UserResponse findUserByIdResponse(final UUID id) {
        User user = getUserByIdAndActiveTrue(id);
        return userMapper.toDTO(user, userRoleService.findRoleByUser(user.getId()));
    }

    private User getUserByIdAndActiveTrue(UUID id){
        User user = userRepository.findUserByIdAndActiveTrue(id);

        if (user == null) {
            throw new UnauthorizedException("Usuário não encontrado.");
        }

        return user;
    }
}
