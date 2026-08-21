package com.ayuntamiento.auth.service;

import java.util.Optional;

import com.ayuntamiento.auth.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken crearRefreshToken(Long usuarioId);
    
    Optional<RefreshToken> buscarPorToken(String token);
    
    RefreshToken verificarExpiracion(RefreshToken token);
    
    void borrarPorUsuarioId(Long usuarioId);
    
}