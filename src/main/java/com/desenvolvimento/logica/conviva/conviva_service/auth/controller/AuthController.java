package com.desenvolvimento.logica.conviva.conviva_service.auth.controller;

import com.desenvolvimento.logica.conviva.conviva_service.auth.dto.LoginRequest;
import com.desenvolvimento.logica.conviva.conviva_service.auth.dto.LoginResponse;
import com.desenvolvimento.logica.conviva.conviva_service.auth.service.AuthService;
import com.desenvolvimento.logica.conviva.conviva_service.common.dto.ApiResponse;
import com.desenvolvimento.logica.conviva.conviva_service.common.dto.UserDataResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("${app.api.base}/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        LoginResponse loginResponse = authService.login(request);

        ResponseCookie accessCookie = ResponseCookie
                .from("access_token", loginResponse.accessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMinutes(5))
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", loginResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("${app.api.base}/auth/refresh")
                .maxAge(Duration.ofMinutes(30))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Login realizado com sucesso."
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        LoginResponse loginResponse = authService.refresh(request);

        ResponseCookie accessCookie = ResponseCookie
                .from("access_token", loginResponse.accessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMinutes(5))
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", loginResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("${app.api.base}/auth/refresh")
                .maxAge(Duration.ofMinutes(30))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Refresh token realizado com sucesso."
        ));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ApiResponse<UserDataResponse> me() {
        return new ApiResponse<>(
                true,
                "Usuário autenticado.",
                authService.me()
        );
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ApiResponse<Void> logout(
            HttpServletRequest request
    ) {
        authService.logout(request);
        return new ApiResponse<>(
                true,
                "Logout realizado com sucesso."
        );
    }
}
