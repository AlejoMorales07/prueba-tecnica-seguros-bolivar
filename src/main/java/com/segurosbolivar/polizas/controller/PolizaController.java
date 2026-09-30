package com.segurosbolivar.polizas.controller;

import com.segurosbolivar.polizas.dto.PolizaRequestDTO;
import com.segurosbolivar.polizas.dto.PolizaResponseDTO;
import com.segurosbolivar.polizas.dto.RenovarPolizaRequestDTO;
import com.segurosbolivar.polizas.dto.RiesgoRequestDTO;
import com.segurosbolivar.polizas.dto.RiesgoResponseDTO;
import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.TipoPoliza;
import com.segurosbolivar.polizas.service.PolizaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/polizas")
@RequiredArgsConstructor
public class PolizaController {

    private final PolizaService polizaService;

    @PostMapping
    public ResponseEntity<PolizaResponseDTO> crearPoliza(@Valid @RequestBody PolizaRequestDTO dto) {
        PolizaResponseDTO creada = polizaService.crearPoliza(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping
    public ResponseEntity<List<PolizaResponseDTO>> listarPolizas(
            @RequestParam(required = false) TipoPoliza tipo,
            @RequestParam(required = false) EstadoPoliza estado) {
        List<PolizaResponseDTO> polizas = polizaService.listarPolizas(tipo, estado);
        return ResponseEntity.ok(polizas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolizaResponseDTO> obtenerPorId(@PathVariable Long id) {
        PolizaResponseDTO poliza = polizaService.obtenerPorId(id);
        return ResponseEntity.ok(poliza);
    }

    @GetMapping("/{id}/riesgos")
    public ResponseEntity<List<RiesgoResponseDTO>> listarRiesgosPorPoliza(@PathVariable Long id) {
        List<RiesgoResponseDTO> riesgos = polizaService.listarRiesgosPorPoliza(id);
        return ResponseEntity.ok(riesgos);
    }

    @PostMapping("/{id}/renovar")
    public ResponseEntity<PolizaResponseDTO> renovarPoliza(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal ipc,
            @RequestBody(required = false) RenovarPolizaRequestDTO requestBody) {

        BigDecimal ipcFinal = ipc;
        if (ipcFinal == null && requestBody != null && requestBody.getIpc() != null) {
            ipcFinal = requestBody.getIpc();
        }
        if (ipcFinal == null) {
            ipcFinal = new BigDecimal("0.05");
        }

        PolizaResponseDTO renovada = polizaService.renovarPoliza(id, ipcFinal);
        return ResponseEntity.ok(renovada);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PolizaResponseDTO> cancelarPoliza(@PathVariable Long id) {
        PolizaResponseDTO cancelada = polizaService.cancelarPoliza(id);
        return ResponseEntity.ok(cancelada);
    }

    @PostMapping("/{id}/riesgos")
    public ResponseEntity<RiesgoResponseDTO> agregarRiesgo(
            @PathVariable Long id,
            @Valid @RequestBody RiesgoRequestDTO riesgoDto) {
        RiesgoResponseDTO riesgoCreado = polizaService.agregarRiesgo(id, riesgoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(riesgoCreado);
    }
}
