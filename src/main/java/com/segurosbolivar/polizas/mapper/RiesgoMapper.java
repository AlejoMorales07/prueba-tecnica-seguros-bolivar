package com.segurosbolivar.polizas.mapper;

import com.segurosbolivar.polizas.dto.RiesgoRequestDTO;
import com.segurosbolivar.polizas.dto.RiesgoResponseDTO;
import com.segurosbolivar.polizas.entity.EstadoRiesgo;
import com.segurosbolivar.polizas.entity.Riesgo;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class RiesgoMapper {

    public Riesgo toEntity(RiesgoRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return Riesgo.builder()
                .descripcionInmueble(dto.getDescripcionInmueble())
                .canonRiesgo(dto.getCanonRiesgo() != null ? dto.getCanonRiesgo().setScale(2, RoundingMode.HALF_UP) : null)
                .estado(EstadoRiesgo.ACTIVO)
                .build();
    }

    public RiesgoResponseDTO toDTO(Riesgo entity) {
        if (entity == null) {
            return null;
        }
        return RiesgoResponseDTO.builder()
                .id(entity.getId())
                .polizaId(entity.getPoliza() != null ? entity.getPoliza().getId() : null)
                .descripcionInmueble(entity.getDescripcionInmueble())
                .canonRiesgo(entity.getCanonRiesgo())
                .estado(entity.getEstado())
                .build();
    }
}
