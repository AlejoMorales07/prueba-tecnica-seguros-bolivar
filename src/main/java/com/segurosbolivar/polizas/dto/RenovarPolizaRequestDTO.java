package com.segurosbolivar.polizas.dto;

import jakarta.validation.constraints.DecimalMin;
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
public class RenovarPolizaRequestDTO {

    @Builder.Default
    @DecimalMin(value = "0.00", message = "El IPC debe ser mayor o igual a 0")
    private BigDecimal ipc = new BigDecimal("0.05");
}
