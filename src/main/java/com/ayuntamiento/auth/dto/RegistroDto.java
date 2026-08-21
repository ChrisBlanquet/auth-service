package com.ayuntamiento.auth.dto;

import com.ayuntamiento.auth.entity.Rol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistroDto {
	private String nombre;
    private String email;
    private String password;
    private String telefono;
    private Rol rol;
}
