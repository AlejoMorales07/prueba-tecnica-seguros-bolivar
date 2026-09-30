package com.segurosbolivar.polizas.service;

import com.segurosbolivar.polizas.dto.PolizaRequestDTO;
import com.segurosbolivar.polizas.dto.PolizaResponseDTO;
import com.segurosbolivar.polizas.dto.RiesgoRequestDTO;
import com.segurosbolivar.polizas.dto.RiesgoResponseDTO;
import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.TipoPoliza;

import java.math.BigDecimal;
import java.util.List;

public interface PolizaService {

    PolizaResponseDTO crearPoliza(PolizaRequestDTO dto);

    List<PolizaResponseDTO> listarPolizas(TipoPoliza tipo, EstadoPoliza estado);

    PolizaResponseDTO obtenerPorId(Long id);

    List<RiesgoResponseDTO> listarRiesgosPorPoliza(Long polizaId);

    PolizaResponseDTO renovarPoliza(Long id, BigDecimal ipc);

    PolizaResponseDTO cancelarPoliza(Long id);

    RiesgoResponseDTO agregarRiesgo(Long polizaId, RiesgoRequestDTO riesgoDto);
}
