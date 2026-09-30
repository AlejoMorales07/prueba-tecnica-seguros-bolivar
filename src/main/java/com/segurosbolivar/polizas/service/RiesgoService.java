package com.segurosbolivar.polizas.service;

import com.segurosbolivar.polizas.dto.RiesgoResponseDTO;

public interface RiesgoService {

    RiesgoResponseDTO cancelarRiesgo(Long riesgoId);
}
