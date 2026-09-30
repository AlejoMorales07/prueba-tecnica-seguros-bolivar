package com.segurosbolivar.polizas.dto;

import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.TipoPoliza;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolizaResponseDTO {

    private Long id;
    private TipoPoliza tipo;
    private EstadoPoliza estado;
    private BigDecimal canonMensual;
    private Integer mesesVigencia;
    private BigDecimal primaTotal;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<RiesgoResponseDTO> riesgos;
}
