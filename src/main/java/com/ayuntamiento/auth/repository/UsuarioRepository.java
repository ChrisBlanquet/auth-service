package com.ayuntamiento.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ayuntamiento.auth.entity.Rol;
import com.ayuntamiento.auth.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	
	Optional<Usuario> findByEmail(String email);
	
	boolean existsByIdAndRol(Long id, Rol rol);
	
	List<Usuario> findByActivoTrue();
	
	List<Usuario> findByActivoFalse();
	
	List<Usuario> findByEmailLike(String email);
	
}
