package com.segurosbolivar.polizas.dto;

import com.segurosbolivar.polizas.entity.TipoPoliza;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class PolizaRequestDTO {

    @NotNull(message = "El tipo de póliza es obligatorio")
    private TipoPoliza tipo;

    @NotNull(message = "El canon mensual es obligatorio")
    @DecimalMin(value = "0.01", message = "El canon mensual debe ser mayor a 0")
    private BigDecimal canonMensual;

    @NotNull(message = "Los meses de vigencia son obligatorios")
    @Min(value = 1, message = "Los meses de vigencia deben ser al menos 1")
    private Integer mesesVigencia;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @Valid
    private List<RiesgoRequestDTO> riesgos;
}
