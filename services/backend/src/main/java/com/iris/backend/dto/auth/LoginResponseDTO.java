package com.iris.backend.dto.auth;

import lombok.Getter;

@Getter
public class LoginResponseDTO {

    private final String token;

    private LoginResponseDTO(String token) {
        this.token = token;
    }

    public static LoginResponseDTO fromToken(String token) {
        return new LoginResponseDTO(token);
    }
}
