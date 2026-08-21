package com.ayuntamiento.auth.security.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class UsuarioPrincipal extends User {

    private final Long id;

    public UsuarioPrincipal(Long id, String username, String password,Boolean activo, Collection<? extends GrantedAuthority> authorities) {
        super(username, password,activo,true,true,true, authorities);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    
}