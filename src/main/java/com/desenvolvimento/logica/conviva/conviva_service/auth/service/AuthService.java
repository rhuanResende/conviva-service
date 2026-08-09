package com.desenvolvimento.logica.conviva.conviva_service.auth.service;

import com.desenvolvimento.logica.conviva.conviva_security.holder.TenantContextHolder;
import com.desenvolvimento.logica.conviva.conviva_security.model.AuthenticatedUser;
import com.desenvolvimento.logica.conviva.conviva_security.service.JwtService;
import com.desenvolvimento.logica.conviva.conviva_service.auth.dto.LoginRequest;
import com.desenvolvimento.logica.conviva.conviva_service.auth.dto.LoginResponse;
import com.desenvolvimento.logica.conviva.conviva_service.auth.dto.RoleResponse;
import com.desenvolvimento.logica.conviva.conviva_service.auth.entity.RefreshToken;
import com.desenvolvimento.logica.conviva.conviva_service.auth.entity.UserRole;
import com.desenvolvimento.logica.conviva.conviva_service.auth.enums.SecurityEventType;
import com.desenvolvimento.logica.conviva.conviva_service.auth.mapper.RefreshTokenMapper;
import com.desenvolvimento.logica.conviva.conviva_service.auth.repository.RefreshTokenRepository;
import com.desenvolvimento.logica.conviva.conviva_service.auth.repository.UserRoleRepository;
import com.desenvolvimento.logica.conviva.conviva_service.common.dto.UserDataResponse;
import com.desenvolvimento.logica.conviva.conviva_service.common.dto.UserResponse;
import com.desenvolvimento.logica.conviva.conviva_service.common.exception.BusinessException;
import com.desenvolvimento.logica.conviva.conviva_service.common.exception.InvalidTokenException;
import com.desenvolvimento.logica.conviva.conviva_service.common.exception.UnauthorizedException;
import com.desenvolvimento.logica.conviva.conviva_service.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final SecurityEventService securityEventService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenMapper refreshTokenMapper;

    public AuthService(UserRoleRepository userRoleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       UserService userService,
                       SecurityEventService securityEventService,
                       RoleService roleService,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenMapper refreshTokenMapper
    ) {
        this.userRoleRepository = userRoleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService = userService;
        this.securityEventService = securityEventService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenMapper = refreshTokenMapper;
    }

    public LoginResponse login(LoginRequest request) {

        UserResponse user = userService.findUserByDocument(request.document());

        validateUserLocked(user);

        if (!passwordEncoder.matches(request.password(), user.password())) {

            userService.registerFailedLoginAttempt(UUID.fromString(user.id()));

            registerEventSecurity(
                    UUID.fromString(user.id()),
                    SecurityEventType.LOGIN_FAILED,
                    "Falha ao realizar login"
            );

            throw new UnauthorizedException("Usuário ou senha incorretos.");
        }

        if (user.failedLoginAttempts() > 0) {
            userService.resetFailedLoginAttempts(
                    UUID.fromString(user.id())
            );
        }

        String token = jwtService.generateToken(new AuthenticatedUser(
                UUID.fromString(user.id()),
                user.document(),
                getRoleUser(user.id()).name()
        ));

        String refreshTokenValue = jwtService.generateRefreshToken();
        refreshTokenRepository.save(refreshTokenMapper.toEntity(UUID.fromString(user.id()), refreshTokenValue));

        registerEventSecurity(
                UUID.fromString(user.id()),
                SecurityEventType.LOGIN_SUCCESS,
                "Login realizado com sucesso."
        );

        return new LoginResponse(
                token,
                refreshTokenValue,
                jwtService.getExpiration(),
                user.firstAccess()
        );
    }

    public LoginResponse refresh(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new InvalidTokenException("Refresh token não encontrado");
        }

        String refreshTokenCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() ->
                        new InvalidTokenException("Refresh token não encontrado"));

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenCookie)
                .orElseThrow(() -> new InvalidTokenException("Token Inválido"));

        if (refreshToken.isRevoked()) {
            refreshTokenRepository.findAllByUser(refreshToken.getUser())
                    .forEach(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });

            registerEventSecurity(
                    refreshToken.getUser(),
                    SecurityEventType.REFRESH_REUSE_DETECTED,
                    "Tentativa de reutilização de refresh token."
            );

            throw new InvalidTokenException("Sessão inválida. Realize login novamente.");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            registerEventSecurity(
                    refreshToken.getUser(),
                    SecurityEventType.REFRESH_EXPIRED,
                    "Tentativa de utilização de refresh token expirado."
            );

            throw new InvalidTokenException("Token Expirado");
        }

        UserResponse user = userService.findUserByIdResponse(refreshToken.getUser());

        String token = jwtService.generateToken(new AuthenticatedUser(
                UUID.fromString(user.id()),
                user.document(),
                getRoleUser(user.id()).name()
        ));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String refreshTokenValue = jwtService.generateRefreshToken();
        refreshTokenRepository.save(refreshTokenMapper.toEntity(UUID.fromString(user.id()), refreshTokenValue));

        return new LoginResponse(
                token,
                refreshTokenValue,
                jwtService.getExpiration(),
                user.firstAccess()
        );
    }

    public UserDataResponse me() {
        UserResponse user = userService.findUserByDocument(TenantContextHolder.getUserDocument());

        if (user == null) {
            throw new BusinessException("Usuário não encontrado.");
        }

        return new UserDataResponse(
                user.id(),
                user.name().toUpperCase(),
                user.document(),
                user.email(),
                user.phone(),
                getRoleUser(user.id()).description(),
                user.status(),
                user.firstAccess(),
                user.forcePasswordChange()
        );
    }

    public void logout(HttpServletRequest request) {
        String tokenCookie = getRefreshTokenCookie(request);

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(tokenCookie)
                .orElseThrow(() -> new InvalidTokenException("Token Inválido"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        registerEventSecurity(
                refreshToken.getUser(),
                SecurityEventType.LOGOUT,
                "Login realizado com sucesso."
        );
    }

    private void validateUserLocked(UserResponse userResponse) {
        if (userResponse.lockedUntil() != null &&
                userResponse.lockedUntil().isAfter(LocalDateTime.now())) {

            long minutes = Duration.between(
                    LocalDateTime.now(),
                    userResponse.lockedUntil()
            ).toMinutes();

            throw new UnauthorizedException(
                    "Usuário bloqueado. Tente novamente em " + Math.max(minutes, 1) + " minuto(s)"
            );
        }
    }

    private void registerEventSecurity(UUID user, SecurityEventType event, String message) {
        securityEventService.register(
                user,
                event,
                message
        );
    }

    private RoleResponse getRoleUser(String userId) {
        UserRole userRole = userRoleRepository.findUserRoleByUserAndActiveTrue(UUID.fromString(userId));
        return roleService.findRoleById(userRole.getRole());
    }

    private String getRefreshTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new InvalidTokenException("Refresh token não encontrado");
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() ->
                        new InvalidTokenException("Refresh token não encontrado"));
    }
}
