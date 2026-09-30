package com.segurosbolivar.polizas.controller;

import com.segurosbolivar.polizas.dto.CoreEventoDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    @PostMapping("/evento")
    public ResponseEntity<Map<String, String>> recibirEventoCore(@Valid @RequestBody CoreEventoDTO payload) {
        log.info("[MOCK CORE] Evento recibido exitosamente desde la aplicación. Evento: '{}', PolizaId: {}",
                payload.getEvento(), payload.getPolizaId());

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Evento procesado correctamente por el MOCK CORE",
                "evento", payload.getEvento(),
                "polizaId", String.valueOf(payload.getPolizaId())
        ));
    }
}
