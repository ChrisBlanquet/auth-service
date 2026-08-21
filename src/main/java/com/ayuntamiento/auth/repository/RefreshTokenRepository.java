package com.ayuntamiento.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.ayuntamiento.auth.entity.RefreshToken;



public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	
	Optional<RefreshToken> findByToken(String token);
	
	@Transactional
    @Modifying
	void deleteByUsuarioId(Long usuarioId);

}
