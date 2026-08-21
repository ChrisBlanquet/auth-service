package com.ayuntamiento.auth.service.impl;

import com.ayuntamiento.auth.entity.RefreshToken;
import com.ayuntamiento.auth.entity.Usuario;
import com.ayuntamiento.auth.exception.ResourceNotFoundException; 
import com.ayuntamiento.auth.exception.BadRequestException;
import com.ayuntamiento.auth.repository.RefreshTokenRepository;
import com.ayuntamiento.auth.repository.UsuarioRepository;
import com.ayuntamiento.auth.service.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UsuarioRepository usuarioRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public RefreshToken crearRefreshToken(Long usuarioId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + usuarioId));
        
        refreshTokenRepository.deleteByUsuarioId(usuarioId);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setFechaExpiracion(LocalDateTime.now().plusDays(7));

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> buscarPorToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken verificarExpiracion(RefreshToken token) {
        if (token.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new BadRequestException("El Refresh Token ha expirado. Por favor, inicie sesión nuevamente.");
        }
        return token;
    }

    @Override
    @Transactional
    public void borrarPorUsuarioId(Long usuarioId) {
        refreshTokenRepository.deleteByUsuarioId(usuarioId);
    }
}