package com.ayuntamiento.auth.mapper;

import org.springframework.stereotype.Component;

import com.ayuntamiento.auth.dto.RegistroDto;
import com.ayuntamiento.auth.dto.UsuarioPerfilDto;
import com.ayuntamiento.auth.entity.Usuario;

@Component
public class UsuarioMapper {
	
	// De DTO de Registro a Entidad y guardar en BD
    public Usuario UsuarioaEntidad(RegistroDto dto) {
        if (dto == null) return null;

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());
        usuario.setTelefono(dto.getTelefono());
        usuario.setRol(dto.getRol());
        
        return usuario;
    }

    //De entidad a DTO
    public UsuarioPerfilDto UsuarioaDto(Usuario entity) {
        if (entity == null) return null;

        UsuarioPerfilDto dto = new UsuarioPerfilDto();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setEmail(entity.getEmail());
        dto.setTelefono(entity.getTelefono());
        dto.setActivo(entity.getActivo());
        dto.setRol(entity.getRol());
        dto.setFechaRegistro(entity.getFechaRegistro());
        
        return dto;
    }

}
