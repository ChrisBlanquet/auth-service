package com.ayuntamiento.auth.security.jwt;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenGenerator {

    @Value("${jwt.rsa.private-key}")
    private String privateKeyBase64;
    
    @Value("${jwt.rsa.public-key}")
    private String publicKeyBase64;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;
    
    private PrivateKey privateKey;
    private PublicKey publicKey;
    
    @PostConstruct
    public void initKeys() throws Exception {
        privateKeyBase64 = privateKeyBase64.replaceAll("\\s+", "");
        publicKeyBase64 = publicKeyBase64.replaceAll("\\s+", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }

    /**
     * GENERAR TOKEN (Login Normal)
     */
    public String generarToken(Authentication authentication, Long usuarioId,List<String> permisos) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);
        
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        return Jwts.builder()
                .setSubject(username)
                .claim("id", usuarioId)
                .claim("rol", role)
                .claim("permisos", permisos)
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * GENERAR TOKEN DESDE EMAIL (Para el Refresh)
     */
    public String generarTokenDesdeEmail(String email, Long usuarioId, String rol,List<String> permisos) {
        Date fechaActual = new Date();
        Date fechaExpiracion = new Date(fechaActual.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .setSubject(email)
                .claim("id", usuarioId)
                .claim("rol", rol)
                .claim("permisos", permisos)
                .setIssuedAt(fechaActual)
                .setExpiration(fechaExpiracion)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * LEER EL TOKEN
     */
    public String obtenerUsernameDelToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
                
        return claims.getSubject();
    }

    /**
     * VALIDAR EL TOKEN
     */
    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    
    /**
     * LEER ROL Y PERMISOS DEL TOKEN
     */
    public Collection<? extends GrantedAuthority> obtenerAutoridadesDelToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        List<GrantedAuthority> autoridades = new ArrayList<>();

        // 1. Extraemos el Rol (ej. "ROLE_SISTEMA")
        Object rolesObj = claims.get("rol");
        if (rolesObj != null) {
            autoridades.add(new SimpleGrantedAuthority(rolesObj.toString()));
        }

        // 2. Extraemos los Permisos (ej. ["VER_INCIDENCIAS", "ASIGNAR_PERSONAL"])
        List<String> permisos = claims.get("permisos", List.class);
        if (permisos != null && !permisos.isEmpty()) {
            for (String permiso : permisos) {
                // Los agregamos a la lista de autoridades
                autoridades.add(new SimpleGrantedAuthority(permiso));
            }
        }

        return autoridades;
    }
}