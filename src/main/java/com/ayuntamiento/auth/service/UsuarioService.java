package com.ayuntamiento.auth.service;

import java.util.List;

import com.ayuntamiento.auth.dto.AdminPasswordResetDto;
import com.ayuntamiento.auth.dto.JwtAuthResponseDto;
import com.ayuntamiento.auth.dto.LoginDto;
import com.ayuntamiento.auth.dto.RefreshTokenRequestDto;
import com.ayuntamiento.auth.dto.RegistroDto;
import com.ayuntamiento.auth.dto.UsuarioPasswordUpdateDto;
import com.ayuntamiento.auth.dto.UsuarioPerfilDto;
import com.ayuntamiento.auth.dto.UsuarioUpdateDto;
import com.ayuntamiento.auth.dto.UsuarioUpdateRolDto;

public interface UsuarioService {
	
	UsuarioPerfilDto registrar(RegistroDto registroDto);
	
	UsuarioPerfilDto registrarEmpleado(RegistroDto registroDto);
	
	JwtAuthResponseDto login(LoginDto loginDto);
	
	UsuarioPerfilDto consultaUsuario(Long id);
	
	boolean esCiudadano(Long id);
	
	List<UsuarioPerfilDto> consultarTodosUsuarios();
	
	List<UsuarioPerfilDto> consultarTodosEmpleados();
	
	List<UsuarioPerfilDto> consultarTodosCiudadanos();
	
	void eliminarUsuario(Long id);
	
	UsuarioPerfilDto actualizarUsuario(UsuarioUpdateDto actualizarDto,Long id);
	
	UsuarioPerfilDto actualizarUsuarioRol(UsuarioUpdateRolDto nuevorol,Long id);
	
	UsuarioPerfilDto actualizarUsuarioPassword(UsuarioPasswordUpdateDto password,Long id);
	
	UsuarioPerfilDto buscarUsuarioPorCorreo(String correo);
	
	List<UsuarioPerfilDto> buscarcoincidenciaCorreo(String correo);
	
	JwtAuthResponseDto refrescarToken(RefreshTokenRequestDto request);
	
	void logout(RefreshTokenRequestDto request);
	
	void bloquearusuario(Long id);
	
	void activarusuario(Long id);
	
	List<UsuarioPerfilDto> esActivo();
	
	List<UsuarioPerfilDto> esInactivo();
	
	UsuarioPerfilDto resetearPasswordPorAdmin(AdminPasswordResetDto passwordDto, Long id);

}
