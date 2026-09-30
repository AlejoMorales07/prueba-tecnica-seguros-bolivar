package com.segurosbolivar.polizas.controller;

import com.segurosbolivar.polizas.dto.RiesgoResponseDTO;
import com.segurosbolivar.polizas.service.RiesgoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/riesgos")
@RequiredArgsConstructor
public class RiesgoController {

    private final RiesgoService riesgoService;

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<RiesgoResponseDTO> cancelarRiesgo(@PathVariable Long id) {
        RiesgoResponseDTO cancelado = riesgoService.cancelarRiesgo(id);
        return ResponseEntity.ok(cancelado);
    }
}
