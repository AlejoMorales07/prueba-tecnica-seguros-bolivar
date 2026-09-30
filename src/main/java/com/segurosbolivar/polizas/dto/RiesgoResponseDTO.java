package com.segurosbolivar.polizas.dto;

import com.segurosbolivar.polizas.entity.EstadoRiesgo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiesgoResponseDTO {

    private Long id;
    private Long polizaId;
    private String descripcionInmueble;
    private BigDecimal canonRiesgo;
    private EstadoRiesgo estado;
}
