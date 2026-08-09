package com.desenvolvimento.logica.conviva.conviva_service.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "CPF é obrigatório.")
        String document,

        @NotBlank(message = "Senha é obrigatória.")
        String password
) {
}
