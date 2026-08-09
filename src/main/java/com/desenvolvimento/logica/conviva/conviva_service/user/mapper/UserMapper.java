package com.desenvolvimento.logica.conviva.conviva_service.user.mapper;

import com.desenvolvimento.logica.conviva.conviva_service.common.dto.UserResponse;
import com.desenvolvimento.logica.conviva.conviva_service.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toDTO(User user, String role) {
        return new UserResponse(
                user.getId().toString(),
                user.getName(),
                user.getDocument(),
                user.getEmail(),
                user.getPhone(),
                user.getPassword(),
                user.getFirstAccess(),
                user.getForcePasswordChange(),
                user.getFailedLoginAttempts(),
                user.getLockedUntil(),
                user.getLastLockedAt(),
                user.getPasswordChangedAt(),
                user.getPasswordExpiresAt(),
                user.getActive() ? "ATIVO" : "INATIVO",
                role.equals("ADMIN") ?
                        "ADMINISTRADOR" :
                        role.equals("MANAGER") ?
                                "GERENTE" :
                                role.equals("USER") ?
                                        "USUÁRIO" :
                                        role
        );
    }
}
