package com.ayuntamiento.auth.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.ayuntamiento.auth.entity.RefreshToken;
import com.ayuntamiento.auth.entity.Rol;
import com.ayuntamiento.auth.entity.Usuario;
import com.ayuntamiento.auth.exception.BadRequestException;
import com.ayuntamiento.auth.exception.ResourceAlreadyExistsException;
import com.ayuntamiento.auth.exception.ResourceNotFoundException;
import com.ayuntamiento.auth.mapper.UsuarioMapper;
import com.ayuntamiento.auth.repository.UsuarioRepository;
import com.ayuntamiento.auth.security.jwt.JwtTokenGenerator;
import com.ayuntamiento.auth.service.RefreshTokenService;
import com.ayuntamiento.auth.service.UsuarioService;

import feign.FeignException;

@Service
public class UsuarioServiceImpl implements UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private UsuarioMapper usuarioMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenGenerator jwtTokenProvider;

	@Autowired
	private RefreshTokenService refreshTokenService;
	
	@Autowired
    private GestionClient gestionClient;

	@Override
	public UsuarioPerfilDto registrar(RegistroDto registroDto) {
		Optional<Usuario> existente = usuarioRepository.findByEmail(registroDto.getEmail());

		if (existente.isPresent()) {
			throw new ResourceAlreadyExistsException(
					"El email " + registroDto.getEmail() + " ya está registrado en el sistema.");
		}

		// De DTO a Entidad mediante mapper
		Usuario usuario = usuarioMapper.UsuarioaEntidad(registroDto);
		String passwordEncriptada = passwordEncoder.encode(registroDto.getPassword());
		usuario.setPassword(passwordEncriptada);
		usuario.setRol(Rol.CIUDADANO);
		usuario.setActivo(true);
		Usuario guardado = usuarioRepository.save(usuario);
		return usuarioMapper.UsuarioaDto(guardado);
	}

	@Override
	public UsuarioPerfilDto registrarEmpleado(RegistroDto registroDto) {
		Optional<Usuario> existente = usuarioRepository.findByEmail(registroDto.getEmail());

		if (existente.isPresent()) {
			throw new ResourceAlreadyExistsException(
					"El email " + registroDto.getEmail() + " ya está registrado en el sistema.");
		}

		// De DTO a Entidad mediante mapper
		Usuario usuario = usuarioMapper.UsuarioaEntidad(registroDto);
		String passwordEncriptada = passwordEncoder.encode(registroDto.getPassword());
		usuario.setPassword(passwordEncriptada);
		usuario.setActivo(true);
		Usuario guardado = usuarioRepository.save(usuario);
		return usuarioMapper.UsuarioaDto(guardado);
	}

	@Override
	public JwtAuthResponseDto login(LoginDto loginDto) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		Usuario usuario = usuarioRepository.findByEmail(loginDto.getEmail())
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		
		refreshTokenService.borrarPorUsuarioId(usuario.getId());
		Long idPuesto = usuario.getId();
		List<String> permisosDelUsuario; 
		if (usuario.getRol() == Rol.SISTEMA) {
		    permisosDelUsuario = List.of("PERMISOS_SISTEMA"); 
		} else {
			try {
			    permisosDelUsuario = gestionClient.obtenerPermisosPorPuesto(usuario.getId());
			} catch (FeignException.NotFound e) { // Atrapar específicamente el 404
			    System.out.println("El usuario es nuevo o no tiene permisos asignados. Dando acceso básico.");
			    permisosDelUsuario = List.of("PERMISO_CIUDADADANO");
			} catch (Exception e) {
			    permisosDelUsuario = List.of("PERMISO_CIUDADADANO");
			}
		}
        

		String accessToken = jwtTokenProvider.generarToken(authentication, usuario.getId(),permisosDelUsuario);

		RefreshToken refreshToken = refreshTokenService.crearRefreshToken(usuario.getId());

		return new JwtAuthResponseDto(accessToken, refreshToken.getToken());
	}

	@Override
	public UsuarioPerfilDto consultaUsuario(Long id) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

		if (usuario.getRol() == Rol.SISTEMA) {
			throw new ResourceNotFoundException("Usuario no encontrado");
		}

		return usuarioMapper.UsuarioaDto(usuario);
	}

	@Override
	public List<UsuarioPerfilDto> consultarTodosUsuarios() {
		List<Usuario> usuarios = usuarioRepository.findAll();
		return usuarios.stream().filter(usuario -> usuario.getRol() != Rol.SISTEMA).map(usuarioMapper::UsuarioaDto)
				.toList();
	}

	@Override
	public void eliminarUsuario(Long id) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
		usuarioRepository.delete(usuario);
	}

	@Override
	public UsuarioPerfilDto actualizarUsuario(UsuarioUpdateDto registroDto, Long id) {
		Usuario usuarioExistente = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
		usuarioExistente.setNombre(registroDto.getNombre());
		usuarioExistente.setEmail(registroDto.getEmail());
		usuarioExistente.setTelefono(registroDto.getTelefono());
		Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);

		return usuarioMapper.UsuarioaDto(usuarioActualizado);
	}

	@Override
	@Transactional
	public UsuarioPerfilDto actualizarUsuarioRol(UsuarioUpdateRolDto rolDto, Long id) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

		usuario.setRol(rolDto.getRol());
		Usuario actualizado = usuarioRepository.save(usuario);

		refreshTokenService.borrarPorUsuarioId(id);
		return usuarioMapper.UsuarioaDto(actualizado);
	}

	@Override
	public UsuarioPerfilDto actualizarUsuarioPassword(UsuarioPasswordUpdateDto passwordDto, Long id) {

		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

		if (!passwordEncoder.matches(passwordDto.getPasswordActual(), usuario.getPassword())) {
			throw new BadRequestException("La contraseña actual es incorrecta");
		}

		if (passwordEncoder.matches(passwordDto.getNuevapassword(), usuario.getPassword())) {
			throw new BadRequestException("La nueva contraseña no puede ser igual a la anterior");
		}

		String passwordEncriptada = passwordEncoder.encode(passwordDto.getNuevapassword());
		usuario.setPassword(passwordEncriptada);

		Usuario actualizado = usuarioRepository.save(usuario);
		return usuarioMapper.UsuarioaDto(actualizado);
	}

	@Override
	public UsuarioPerfilDto buscarUsuarioPorCorreo(String correo) {
		Usuario usuario = usuarioRepository.findByEmail(correo)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún usuario con el correo "));
		return usuarioMapper.UsuarioaDto(usuario);
	}

	@Override
	public JwtAuthResponseDto refrescarToken(RefreshTokenRequestDto request) {
		
		System.out.println(request.getRefreshToken());
		
		RefreshToken refreshToken = refreshTokenService.buscarPorToken(request.getRefreshToken())
				.orElseThrow(() -> new ResourceNotFoundException("Refresh Token no encontrado en la base de datos"));

		refreshTokenService.verificarExpiracion(refreshToken);

		Usuario usuario = refreshToken.getUsuario();

		if (!usuario.getActivo()) {
			throw new BadRequestException("Su cuenta ha sido bloqueada. Sesión terminada.");
		}
		
		
		Long idPuesto = usuario.getId();
		List<String> permisosDelUsuario; 

		if (usuario.getRol() == Rol.SISTEMA) {
		    permisosDelUsuario = List.of("PERMISOS_SISTEMA"); 
		} else {
			try {
			    permisosDelUsuario = gestionClient.obtenerPermisosPorPuesto(usuario.getId());
			} catch (FeignException.NotFound e) { // Atrapar específicamente el 404
			    System.out.println("El usuario es nuevo o no tiene permisos asignados. Dando acceso básico.");
			    permisosDelUsuario = List.of("PERMISO_CIUDADADANO");
			} catch (Exception e) {
			    permisosDelUsuario = List.of("PERMISO_CIUDADADANO");
			}
		}
        
		
		
		String rolConPrefijo = "ROLE_" + usuario.getRol().name();


		String nuevoAccessToken = jwtTokenProvider.generarTokenDesdeEmail(usuario.getEmail(), usuario.getId(),
				rolConPrefijo,permisosDelUsuario);

		return new JwtAuthResponseDto(nuevoAccessToken, refreshToken.getToken());
	}

	@Override
	public void logout(RefreshTokenRequestDto request) {
		Optional<RefreshToken> refreshTokenOpt = refreshTokenService.buscarPorToken(request.getRefreshToken());
		if (refreshTokenOpt.isPresent()) {
			Long usuarioId = refreshTokenOpt.get().getUsuario().getId();
			refreshTokenService.borrarPorUsuarioId(usuarioId);
		}
	}

	@Override
	public void bloquearusuario(Long id) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontró ningun id asociado "));
		usuario.setActivo(false);
		usuarioRepository.save(usuario);
		refreshTokenService.borrarPorUsuarioId(id);
	}

	@Override
	public void activarusuario(Long id) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontró ningun id asociado "));
		usuario.setActivo(true);
		usuarioRepository.save(usuario);
	}

	@Override
	public boolean esCiudadano(Long id) {
	    return usuarioRepository.existsByIdAndRol(id, Rol.CIUDADANO);
	}

	@Override
	public List<UsuarioPerfilDto> consultarTodosEmpleados() {
	    List<Usuario> usuarios = usuarioRepository.findAll();
	    return usuarios.stream()
	            .filter(u -> u.getRol() == Rol.EMPLEADO)
	            .map(usuarioMapper::UsuarioaDto)
	            .toList();
	}

	@Override
	public List<UsuarioPerfilDto> consultarTodosCiudadanos() {
	    List<Usuario> usuarios = usuarioRepository.findAll();
	    return usuarios.stream()
	            .filter(u -> u.getRol() == Rol.CIUDADANO)
	            .map(usuarioMapper::UsuarioaDto)
	            .toList();
	}

	@Override
	public List<UsuarioPerfilDto> esActivo() {
	    return usuarioRepository.findByActivoTrue().stream()
	            .filter(u -> u.getRol() != Rol.SISTEMA)
	            .map(usuarioMapper::UsuarioaDto)
	            .toList();
	}

	@Override
	public List<UsuarioPerfilDto> esInactivo() {
	    return usuarioRepository.findByActivoFalse().stream()
	            .filter(u -> u.getRol() != Rol.SISTEMA)
	            .map(usuarioMapper::UsuarioaDto)
	            .toList();
	}

	@Override
	public List<UsuarioPerfilDto> buscarcoincidenciaCorreo(String correo) {
	    String patron = "%" + correo + "%";
	    return usuarioRepository.findByEmailLike(patron).stream()
	            .filter(u -> u.getRol() != Rol.SISTEMA)
	            .map(usuarioMapper::UsuarioaDto)
	            .toList();
	}

	@Override
	@Transactional
	public UsuarioPerfilDto resetearPasswordPorAdmin(AdminPasswordResetDto passwordDto, Long id) {
	    Usuario usuario = usuarioRepository.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

	    if (passwordEncoder.matches(passwordDto.getNuevaPassword(), usuario.getPassword())) {
	        throw new BadRequestException("La nueva contraseña no puede ser igual a la anterior");
	    }

	    String passwordEncriptada = passwordEncoder.encode(passwordDto.getNuevaPassword());
	    usuario.setPassword(passwordEncriptada);

	    Usuario actualizado = usuarioRepository.save(usuario);
	    return usuarioMapper.UsuarioaDto(actualizado);
	}
}