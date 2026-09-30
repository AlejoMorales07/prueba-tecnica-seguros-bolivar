package com.segurosbolivar.polizas.service;

import com.segurosbolivar.polizas.client.CoreIntegrationClient;
import com.segurosbolivar.polizas.dto.PolizaRequestDTO;
import com.segurosbolivar.polizas.dto.PolizaResponseDTO;
import com.segurosbolivar.polizas.dto.RiesgoRequestDTO;
import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.EstadoRiesgo;
import com.segurosbolivar.polizas.entity.Poliza;
import com.segurosbolivar.polizas.entity.Riesgo;
import com.segurosbolivar.polizas.entity.TipoPoliza;
import com.segurosbolivar.polizas.exception.BusinessRuleException;
import com.segurosbolivar.polizas.exception.ResourceNotFoundException;
import com.segurosbolivar.polizas.mapper.PolizaMapper;
import com.segurosbolivar.polizas.mapper.RiesgoMapper;
import com.segurosbolivar.polizas.repository.PolizaRepository;
import com.segurosbolivar.polizas.service.impl.PolizaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolizaServiceImplTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private PolizaMapper polizaMapper;

    @Mock
    private RiesgoMapper riesgoMapper;

    @Mock
    private CoreIntegrationClient coreIntegrationClient;

    @InjectMocks
    private PolizaServiceImpl polizaService;

    private Poliza polizaIndividual;
    private Poliza polizaCancelada;

    @BeforeEach
    void setUp() {
        polizaIndividual = Poliza.builder()
                .id(1L)
                .tipo(TipoPoliza.INDIVIDUAL)
                .estado(EstadoPoliza.ACTIVA)
                .canonMensual(new BigDecimal("1000000.00"))
                .mesesVigencia(12)
                .primaTotal(new BigDecimal("12000000.00"))
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusYears(1))
                .riesgos(new ArrayList<>())
                .build();

        polizaIndividual.agregarRiesgo(Riesgo.builder()
                .id(10L)
                .descripcionInmueble("Apto 101")
                .canonRiesgo(new BigDecimal("1000000.00"))
                .estado(EstadoRiesgo.ACTIVO)
                .build());

        polizaCancelada = Poliza.builder()
                .id(2L)
                .tipo(TipoPoliza.INDIVIDUAL)
                .estado(EstadoPoliza.CANCELADA)
                .canonMensual(new BigDecimal("1000000.00"))
                .mesesVigencia(12)
                .primaTotal(new BigDecimal("12000000.00"))
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusYears(1))
                .riesgos(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Debe lanzar BusinessRuleException si se intenta crear póliza INDIVIDUAL con más de 1 riesgo")
    void crearPoliza_individualConMasDeUnRiesgo_lanzaExcepcion() {
        PolizaRequestDTO dto = PolizaRequestDTO.builder()
                .tipo(TipoPoliza.INDIVIDUAL)
                .canonMensual(new BigDecimal("1000000.00"))
                .mesesVigencia(12)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusYears(1))
                .riesgos(List.of(
                        RiesgoRequestDTO.builder().descripcionInmueble("Inmueble 1").canonRiesgo(new BigDecimal("500000.00")).build(),
                        RiesgoRequestDTO.builder().descripcionInmueble("Inmueble 2").canonRiesgo(new BigDecimal("500000.00")).build()
                ))
                .build();

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> polizaService.crearPoliza(dto));
        assertEquals("Una póliza individual solo puede tener 1 riesgo", exception.getMessage());
    }

    @Test
    @DisplayName("Debe renovar una póliza activa calculando IPC correctamente e invocando el Core Mock")
    void renovarPoliza_exitosa() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaIndividual));
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PolizaResponseDTO responseDTOExpected = PolizaResponseDTO.builder()
                .id(1L)
                .estado(EstadoPoliza.RENOVADA)
                .canonMensual(new BigDecimal("1050000.00"))
                .primaTotal(new BigDecimal("12600000.00"))
                .build();

        when(polizaMapper.toDTO(any(Poliza.class))).thenReturn(responseDTOExpected);

        PolizaResponseDTO resultado = polizaService.renovarPoliza(1L, new BigDecimal("0.05"));

        assertNotNull(resultado);
        assertEquals(EstadoPoliza.RENOVADA, resultado.getEstado());
        assertEquals(new BigDecimal("1050000.00"), resultado.getCanonMensual());
        verify(coreIntegrationClient).notificarEvento(eq("ACTUALIZACION"), eq(1L));
    }

    @Test
    @DisplayName("Debe denegar la renovación de una póliza CANCELADA")
    void renovarPoliza_cancelada_lanzaExcepcion() {
        when(polizaRepository.findById(2L)).thenReturn(Optional.of(polizaCancelada));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> polizaService.renovarPoliza(2L, new BigDecimal("0.05")));

        assertEquals("No se puede renovar una póliza CANCELADA", exception.getMessage());
    }

    @Test
    @DisplayName("Debe denegar agregar riesgo a póliza INDIVIDUAL si ya cuenta con 1 riesgo")
    void agregarRiesgo_polizaIndividualConRiesgoExistente_lanzaExcepcion() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaIndividual));

        RiesgoRequestDTO nuevoRiesgo = RiesgoRequestDTO.builder()
                .descripcionInmueble("Apto 202")
                .canonRiesgo(new BigDecimal("500000.00"))
                .build();

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> polizaService.agregarRiesgo(1L, nuevoRiesgo));

        assertEquals("Una póliza individual solo puede tener 1 riesgo", exception.getMessage());
    }

    @Test
    @DisplayName("Debe cancelar póliza y en cascada cambiar estado de sus riesgos a CANCELADO")
    void cancelarPoliza_cancelaRiesgosEnCascada() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaIndividual));
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PolizaResponseDTO responseDTO = PolizaResponseDTO.builder()
                .id(1L)
                .estado(EstadoPoliza.CANCELADA)
                .build();

        when(polizaMapper.toDTO(any(Poliza.class))).thenReturn(responseDTO);

        PolizaResponseDTO resultado = polizaService.cancelarPoliza(1L);

        assertEquals(EstadoPoliza.CANCELADA, resultado.getEstado());
        assertEquals(EstadoRiesgo.CANCELADO, polizaIndividual.getRiesgos().get(0).getEstado());
        verify(coreIntegrationClient).notificarEvento(eq("ACTUALIZACION"), eq(1L));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la póliza no existe")
    void obtenerPorId_noEncontrada_lanzaExcepcion() {
        when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> polizaService.obtenerPorId(99L));
    }
}
