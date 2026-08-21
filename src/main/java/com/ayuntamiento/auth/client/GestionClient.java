package com.ayuntamiento.auth.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(name = "SERVICIO-GESTION-INSTITUCIONAL", path = "/api/gestion/personal")
@Retry(name = "gestionRetry")
public interface GestionClient {

    @GetMapping("/{id}/permisos")
    List<String> obtenerPermisosPorPuesto(@PathVariable("id") Long idPuesto);
}