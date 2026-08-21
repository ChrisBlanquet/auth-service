package com.ayuntamiento.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JwtAuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    
    public JwtAuthResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}