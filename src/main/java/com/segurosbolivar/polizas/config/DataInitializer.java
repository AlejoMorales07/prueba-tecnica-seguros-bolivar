package com.segurosbolivar.polizas.config;

import com.segurosbolivar.polizas.entity.EstadoPoliza;
import com.segurosbolivar.polizas.entity.EstadoRiesgo;
import com.segurosbolivar.polizas.entity.Poliza;
import com.segurosbolivar.polizas.entity.Riesgo;
import com.segurosbolivar.polizas.entity.TipoPoliza;
import com.segurosbolivar.polizas.repository.PolizaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PolizaRepository polizaRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Cargando datos semilla iniciales en H2...");

        // 1. Póliza Individual con 1 Riesgo
        Poliza polizaIndividual = Poliza.builder()
                .tipo(TipoPoliza.INDIVIDUAL)
                .estado(EstadoPoliza.ACTIVA)
                .canonMensual(new BigDecimal("1500000.00"))
                .mesesVigencia(12)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusYears(1))
                .build();

        polizaIndividual.recalcularPrimaTotal();

        Riesgo riesgoIndividual = Riesgo.builder()
                .descripcionInmueble("Apartamento 502, Calle 100 # 15-20, Bogotá")
                .canonRiesgo(new BigDecimal("1500000.00"))
                .estado(EstadoRiesgo.ACTIVO)
                .build();

        polizaIndividual.agregarRiesgo(riesgoIndividual);
        Poliza guardadaIndividual = polizaRepository.save(polizaIndividual);
        log.info("Póliza Individual creada con ID: {} y {} riesgo(s)", guardadaIndividual.getId(), guardadaIndividual.getRiesgos().size());

        // 2. Póliza Colectiva con 2 Riesgos
        Poliza polizaColectiva = Poliza.builder()
                .tipo(TipoPoliza.COLECTIVA)
                .estado(EstadoPoliza.ACTIVA)
                .canonMensual(new BigDecimal("5000000.00"))
                .mesesVigencia(12)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusYears(1))
                .build();

        polizaColectiva.recalcularPrimaTotal();

        Riesgo riesgoColectivo1 = Riesgo.builder()
                .descripcionInmueble("Local comercial 101, Centro Comercial Santa Fe")
                .canonRiesgo(new BigDecimal("2500000.00"))
                .estado(EstadoRiesgo.ACTIVO)
                .build();

        Riesgo riesgoColectivo2 = Riesgo.builder()
                .descripcionInmueble("Oficina 804, Edificio Empresarial 93")
                .canonRiesgo(new BigDecimal("2500000.00"))
                .estado(EstadoRiesgo.ACTIVO)
                .build();

        polizaColectiva.agregarRiesgo(riesgoColectivo1);
        polizaColectiva.agregarRiesgo(riesgoColectivo2);
        Poliza guardadaColectiva = polizaRepository.save(polizaColectiva);
        log.info("Póliza Colectiva creada con ID: {} y {} riesgo(s)", guardadaColectiva.getId(), guardadaColectiva.getRiesgos().size());

        log.info("Precarga de datos semilla finalizada con éxito.");
    }
}
