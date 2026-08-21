package com.ayuntamiento.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private static final String COOKIE_NAME = "refresh_token";
    
    @Value("${app.security.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.security.cookie.same-site}")
    private String cookieSameSite;

    //Crea la cookie para Login y Refresh
    public ResponseCookie crearCookieRefresh(String token) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)     // 👈 Lee del properties
                .path("/api/auth")
                //.maxAge(7 * 24 * 60 * 60)
                .sameSite(cookieSameSite) // 👈 Lee del properties
                .build();
    }

    // Crea una cookie "vacía" para matar la sesión
    public ResponseCookie limpiarCookieRefresh() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}