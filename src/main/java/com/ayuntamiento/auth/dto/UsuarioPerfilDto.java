package com.ayuntamiento.auth.dto;

import java.time.LocalDateTime;

import com.ayuntamiento.auth.entity.Rol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioPerfilDto {
	private Long id;
	private String nombre;
    private String email;
    private String telefono;
    private Rol rol;
    private boolean activo;
    private LocalDateTime fechaRegistro;
}
