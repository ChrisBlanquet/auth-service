package com.ayuntamiento.auth.security.service;

import com.ayuntamiento.auth.entity.Usuario;
import com.ayuntamiento.auth.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    // Inyección de dependencias por constructor 
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email"));

        // Traducimos Rol a de Spring Security ("ROLE_ADMIN")
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());

        //Retornamos el usuario oficial de Spring Security
        return new UsuarioPrincipal(
        		usuario.getId(),
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.getActivo(),
                Collections.singleton(authority)          
        );
    }
}