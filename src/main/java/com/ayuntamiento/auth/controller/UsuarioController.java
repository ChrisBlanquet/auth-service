package com.ayuntamiento.auth.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ayuntamiento.auth.client.GestionClient;
import com.ayuntamiento.auth.dto.AdminPasswordResetDto;
import com.ayuntamiento.auth.dto.JwtAuthResponseDto;
import com.ayuntamiento.auth.dto.LoginDto;
import com.ayuntamiento.auth.dto.RefreshTokenRequestDto;
import com.ayuntamiento.auth.dto.RegistroDto;
import com.ayuntamiento.auth.dto.UsuarioPasswordUpdateDto;
import com.ayuntamiento.auth.dto.UsuarioPerfilDto;
import com.ayuntamiento.auth.dto.UsuarioUpdateDto;
import com.ayuntamiento.auth.dto.UsuarioUpdateRolDto;
import com.ayuntamiento.auth.service.UsuarioService;
import com.ayuntamiento.auth.util.CookieUtil;

import jakarta.validation.Valid;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class UsuarioController {
	
	private final UsuarioService usuarioService;
	private final CookieUtil cookieUtil;
	@Autowired
    private GestionClient gestionClient;
	
	@PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDto> login(@RequestBody LoginDto loginDto) {
        JwtAuthResponseDto respuesta = usuarioService.login(loginDto);

        // Usamos el Utility
        ResponseCookie cookie = cookieUtil.crearCookieRefresh(respuesta.getRefreshToken());
        respuesta.setRefreshToken(null); 
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(respuesta);
    }
	

	@PostMapping("/registrar")
	public ResponseEntity<UsuarioPerfilDto> registrarCiudadano(@RequestBody RegistroDto registroDto){
		
		UsuarioPerfilDto usuarioGuardado = usuarioService.registrar(registroDto);
		
		return new ResponseEntity<>(usuarioGuardado, HttpStatus.CREATED);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/registrar/empleado")
	public ResponseEntity<UsuarioPerfilDto> registrarEmpleado(@RequestBody RegistroDto registroDto){
		
		UsuarioPerfilDto usuarioGuardado = usuarioService.registrarEmpleado(registroDto);
		
		return new ResponseEntity<>(usuarioGuardado, HttpStatus.CREATED);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','SISTEMA') or #usuarioid == principal.id")
	@GetMapping("/usuarios/{id}")
    public ResponseEntity<?> obtenUsuario(@PathVariable("id") Long usuarioid) {
        UsuarioPerfilDto usuarioDto = usuarioService.consultaUsuario(usuarioid);
        return ResponseEntity.ok(usuarioDto);
    }
	
	@PreAuthorize("hasAnyRole('ADMIN','SISTEMA')")
	@GetMapping("/usuarios/{id}/ciudadanos")
    public ResponseEntity<?> obtenUsuarioCiudadano(@PathVariable("id") Long usuarioid) {
        return ResponseEntity.ok(usuarioService.esCiudadano(usuarioid));
    }
	
	@PreAuthorize("hasAnyRole('ADMIN','SISTEMA')")
	@GetMapping("/usuarios/ciudadanos")
    public ResponseEntity<List<UsuarioPerfilDto>> obtenTodosCiudadanos() {
		List<UsuarioPerfilDto> usuarios=usuarioService.consultarTodosCiudadanos();
        return ResponseEntity.ok(usuarios);
    }
	
	@PreAuthorize("hasAnyRole('ADMIN','SISTEMA')")
	@GetMapping("/usuarios/empleados")
    public ResponseEntity<List<UsuarioPerfilDto>> obtenTodosEmpleados() {
		List<UsuarioPerfilDto> usuarios=usuarioService.consultarTodosEmpleados();
        return ResponseEntity.ok(usuarios);
    }
	
	@PreAuthorize("hasAnyRole('ADMIN','SISTEMA')")
	@GetMapping("/usuarios")
	public ResponseEntity<List<UsuarioPerfilDto>> obtentodosUsuarios(){
		List<UsuarioPerfilDto> usuariosDto= usuarioService.consultarTodosUsuarios();
		return ResponseEntity.ok(usuariosDto); 
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','SISTEMA')")
	@GetMapping("/usuarios/activos")
	public ResponseEntity<List<UsuarioPerfilDto>> obtentodosUsuariosActivos(){
		List<UsuarioPerfilDto> usuariosDto= usuarioService.esActivo();
		return ResponseEntity.ok(usuariosDto); 
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','SISTEMA')")
	@GetMapping("/usuarios/inactivos")
	public ResponseEntity<List<UsuarioPerfilDto>> obtentodosUsuariosInactivos(){
		List<UsuarioPerfilDto> usuariosDto= usuarioService.esInactivo();
		return ResponseEntity.ok(usuariosDto); 
	}
	
	@PreAuthorize("hasRole('ADMIN') or #correo == authentication.name")
	@GetMapping("/usuarios/buscar")
    public ResponseEntity<UsuarioPerfilDto> buscarPorEmail(@RequestParam("email") String correo) {
        UsuarioPerfilDto usuarioDto = usuarioService.buscarUsuarioPorCorreo(correo);
        return ResponseEntity.ok(usuarioDto); 
    }
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/usuarios/buscar/correo")
    public ResponseEntity<List<UsuarioPerfilDto>> buscarPorEmailCoincidencia(@RequestParam("email") String correo) {
        List<UsuarioPerfilDto> usuarioDto = usuarioService.buscarcoincidenciaCorreo(correo);
        return ResponseEntity.ok(usuarioDto); 
    }
	
	@PreAuthorize("hasRole('ADMIN') or #usuarioid == authentication.principal.id")
	@PutMapping("/actualizar/{id}")
	public ResponseEntity<UsuarioPerfilDto> actualizarUsuario(@PathVariable("id") Long usuarioid, @RequestBody UsuarioUpdateDto updateUsuario){
		
		UsuarioPerfilDto usuarioActualizado= usuarioService.actualizarUsuario(updateUsuario, usuarioid);
		return ResponseEntity.ok(usuarioActualizado);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/actualizar/{id}/rol")
	public ResponseEntity<UsuarioPerfilDto> actualizarUsuarioRol(@PathVariable("id") Long usuarioid, @RequestBody UsuarioUpdateRolDto updateRol){	
		UsuarioPerfilDto usuarioActualizado= usuarioService.actualizarUsuarioRol(updateRol, usuarioid);
		return ResponseEntity.ok(usuarioActualizado); 
	}

	@PreAuthorize("hasRole('ADMIN') or #usuarioid == authentication.principal.id")
	@PatchMapping("/actualizar/{id}/password")
	public ResponseEntity<UsuarioPerfilDto> actualizarUsuarioPassword(@PathVariable("id") Long usuarioid,@Valid @RequestBody UsuarioPasswordUpdateDto password){	
		UsuarioPerfilDto usuarioActualizado= usuarioService.actualizarUsuarioPassword(password, usuarioid);
		return ResponseEntity.ok(usuarioActualizado);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/admin/usuarios/{id}/reset-password")
	public ResponseEntity<UsuarioPerfilDto> resetearPasswordAdmin(
	        @PathVariable("id") Long usuarioid, 
	        @Valid @RequestBody AdminPasswordResetDto passwordDto) {	
	    
	    UsuarioPerfilDto usuarioActualizado = usuarioService.resetearPasswordPorAdmin(passwordDto, usuarioid);
	    return ResponseEntity.ok(usuarioActualizado);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> borrarUsuario(@PathVariable("id")Long usuarioid){
		usuarioService.eliminarUsuario(usuarioid);
		return ResponseEntity.ok("Usuario eliminado correctamente");
	}
	
	@PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponseDto> refrescarToken(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie) {
        
        if (refreshTokenCookie == null || refreshTokenCookie.isEmpty()) {
            throw new RuntimeException("Refresh Token no encontrado en la cookie de seguridad.");
        }

        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken(refreshTokenCookie);
        JwtAuthResponseDto respuesta = usuarioService.refrescarToken(request);
        ResponseCookie nuevaCookie = cookieUtil.crearCookieRefresh(respuesta.getRefreshToken());
        respuesta.setRefreshToken(null);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, nuevaCookie.toString())
                .body(respuesta);
    }
	
	@PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie) {
        
        if (refreshTokenCookie != null && !refreshTokenCookie.isEmpty()) {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto();
            request.setRefreshToken(refreshTokenCookie);
            usuarioService.logout(request);
        }
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Sesión cerrada exitosamente en el servidor.");
        ResponseCookie cookieMuerte = cookieUtil.limpiarCookieRefresh();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieMuerte.toString())
                .body(respuesta);
    }
	
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("bloqueo/{id}")
    public ResponseEntity<Map<String, String>> bloquearusuario(@PathVariable("id") Long usuarioid) {
        usuarioService.bloquearusuario(usuarioid);
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Usuario bloqueado correctamente");
        return ResponseEntity.ok(respuesta);
    }
	
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("reactivar/{id}")
    public ResponseEntity<Map<String, String>> reactivarusuario(@PathVariable("id") Long usuarioid) {
        usuarioService.activarusuario(usuarioid);
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Usuario reactivado correctamente");
        return ResponseEntity.ok(respuesta);
    }
	
	
	@GetMapping("/puestos/{id}/permisos")
    public ResponseEntity<List<String>> probarConexionGestion(@PathVariable("id") Long id) {
        
        System.out.println("Llamando al micro de Gestión para el puesto: " + id);
        List<String> permisos = gestionClient.obtenerPermisosPorPuesto(id);
        
        return ResponseEntity.ok(permisos);
    }
	
	
	
}