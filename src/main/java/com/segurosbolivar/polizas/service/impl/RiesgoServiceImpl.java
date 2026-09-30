package com.segurosbolivar.polizas.service.impl;

import com.segurosbolivar.polizas.client.CoreIntegrationClient;
import com.segurosbolivar.polizas.dto.RiesgoResponseDTO;
import com.segurosbolivar.polizas.entity.EstadoRiesgo;
import com.segurosbolivar.polizas.entity.Riesgo;
import com.segurosbolivar.polizas.exception.ResourceNotFoundException;
import com.segurosbolivar.polizas.mapper.RiesgoMapper;
import com.segurosbolivar.polizas.repository.RiesgoRepository;
import com.segurosbolivar.polizas.service.RiesgoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiesgoServiceImpl implements RiesgoService {

    private final RiesgoRepository riesgoRepository;
    private final RiesgoMapper riesgoMapper;
    private final CoreIntegrationClient coreIntegrationClient;

    @Override
    @Transactional
    public RiesgoResponseDTO cancelarRiesgo(Long riesgoId) {
        log.info("Cancelando riesgo con ID: {}", riesgoId);
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new ResourceNotFoundException("Riesgo no encontrado con el ID: " + riesgoId));

        riesgo.setEstado(EstadoRiesgo.CANCELADO);
        Riesgo cancelado = riesgoRepository.save(riesgo);

        if (riesgo.getPoliza() != null) {
            coreIntegrationClient.notificarEvento("ACTUALIZACION", riesgo.getPoliza().getId());
        }

        return riesgoMapper.toDTO(cancelado);
    }
}
