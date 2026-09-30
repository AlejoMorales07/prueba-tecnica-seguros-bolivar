package com.segurosbolivar.polizas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RiesgoRequestDTO {

    @NotBlank(message = "La descripción del inmueble no puede estar vacía")
    private String descripcionInmueble;

    @NotNull(message = "El canon del riesgo es obligatorio")
    @DecimalMin(value = "0.01", message = "El canon del riesgo debe ser mayor a 0")
    private BigDecimal canonRiesgo;
}
