package com.ayuntamiento.auth.security.jwt;

import com.ayuntamiento.auth.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenGenerator jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    // Inyectamos la clases 
    public AuthJwtAuthenticationFilter(JwtTokenGenerator jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String token = obtenerTokenDeLaSolicitud(request);

        // Validamos el token
        if (StringUtils.hasText(token) && jwtTokenProvider.validarToken(token)) {
            
            // Sacamos el email del token
            String username = jwtTokenProvider.obtenerUsernameDelToken(token);

            // Cargamos el usuario desde la base de datos
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            //Creamos el objeto de autenticación para Spring Security
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //guardamos el usuario
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    // Obtener tooken
    private String obtenerTokenDeLaSolicitud(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // El estándar es que el token empiece con la palabra "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}