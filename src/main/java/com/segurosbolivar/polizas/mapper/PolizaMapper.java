package com.segurosbolivar.polizas.mapper;

import com.segurosbolivar.polizas.dto.PolizaRequestDTO;
import com.segurosbolivar.polizas.dto.PolizaResponseDTO;
import com.segurosbolivar.polizas.dto.RiesgoResponseDTO;
import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.Poliza;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PolizaMapper {

    private final RiesgoMapper riesgoMapper;

    public Poliza toEntity(PolizaRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Poliza poliza = Poliza.builder()
                .tipo(dto.getTipo())
                .estado(EstadoPoliza.ACTIVA)
                .canonMensual(dto.getCanonMensual() != null ? dto.getCanonMensual().setScale(2, RoundingMode.HALF_UP) : null)
                .mesesVigencia(dto.getMesesVigencia())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .riesgos(new ArrayList<>())
                .build();

        poliza.recalcularPrimaTotal();

        if (dto.getRiesgos() != null) {
            dto.getRiesgos().forEach(rDto -> {
                var riesgo = riesgoMapper.toEntity(rDto);
                poliza.agregarRiesgo(riesgo);
            });
        }

        return poliza;
    }

    public PolizaResponseDTO toDTO(Poliza entity) {
        if (entity == null) {
            return null;
        }

        List<RiesgoResponseDTO> riesgosDTO = entity.getRiesgos() != null
                ? entity.getRiesgos().stream().map(riesgoMapper::toDTO).toList()
                : List.of();

        return PolizaResponseDTO.builder()
                .id(entity.getId())
                .tipo(entity.getTipo())
                .estado(entity.getEstado())
                .canonMensual(entity.getCanonMensual())
                .mesesVigencia(entity.getMesesVigencia())
                .primaTotal(entity.getPrimaTotal())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .riesgos(riesgosDTO)
                .build();
    }
}
