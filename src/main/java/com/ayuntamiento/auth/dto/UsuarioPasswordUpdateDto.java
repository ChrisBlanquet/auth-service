package com.ayuntamiento.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioPasswordUpdateDto {

    @NotBlank(message = "La contraseña actual no puede estar vacía")
    private String passwordActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    @Pattern(
    	    regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*\\-_]).*$", 
    	    message = "La contraseña debe tener al menos una mayúscula, un número y un carácter especial"
    	)
    private String nuevapassword;
}