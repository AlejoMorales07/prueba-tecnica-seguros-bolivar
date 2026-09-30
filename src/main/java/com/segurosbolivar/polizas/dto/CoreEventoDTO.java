package com.segurosbolivar.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreEventoDTO {

    @NotBlank(message = "El evento no puede estar vacío")
    private String evento;

    @NotNull(message = "El polizaId es obligatorio")
    private Long polizaId;
}
