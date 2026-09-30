package com.segurosbolivar.polizas.service.impl;

import com.segurosbolivar.polizas.client.CoreIntegrationClient;
import com.segurosbolivar.polizas.dto.PolizaRequestDTO;
import com.segurosbolivar.polizas.dto.PolizaResponseDTO;
import com.segurosbolivar.polizas.dto.RiesgoRequestDTO;
import com.segurosbolivar.polizas.dto.RiesgoResponseDTO;
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
import com.segurosbolivar.polizas.service.PolizaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolizaServiceImpl implements PolizaService {

    private final PolizaRepository polizaRepository;
    private final PolizaMapper polizaMapper;
    private final RiesgoMapper riesgoMapper;
    private final CoreIntegrationClient coreIntegrationClient;

    @Override
    @Transactional
    public PolizaResponseDTO crearPoliza(PolizaRequestDTO dto) {
        log.info("Creando póliza de tipo: {}", dto.getTipo());

        if (dto.getTipo() == TipoPoliza.INDIVIDUAL && dto.getRiesgos() != null && dto.getRiesgos().size() > 1) {
            throw new BusinessRuleException("Una póliza individual solo puede tener 1 riesgo");
        }

        Poliza poliza = polizaMapper.toEntity(dto);
        Poliza guardada = polizaRepository.save(poliza);
        return polizaMapper.toDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolizaResponseDTO> listarPolizas(TipoPoliza tipo, EstadoPoliza estado) {
        log.info("Listando pólizas con filtro tipo: {}, estado: {}", tipo, estado);
        List<Poliza> polizas;

        if (tipo != null && estado != null) {
            polizas = polizaRepository.findByTipoAndEstado(tipo, estado);
        } else if (tipo != null) {
            polizas = polizaRepository.findByTipo(tipo);
        } else if (estado != null) {
            polizas = polizaRepository.findByEstado(estado);
        } else {
            polizas = polizaRepository.findAll();
        }

        return polizas.stream().map(polizaMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PolizaResponseDTO obtenerPorId(Long id) {
        log.info("Buscando póliza con ID: {}", id);
        Poliza poliza = obtenerPolizaEntity(id);
        return polizaMapper.toDTO(poliza);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiesgoResponseDTO> listarRiesgosPorPoliza(Long polizaId) {
        log.info("Obteniendo riesgos para la póliza ID: {}", polizaId);
        Poliza poliza = obtenerPolizaEntity(polizaId);
        return poliza.getRiesgos().stream().map(riesgoMapper::toDTO).toList();
    }

    @Override
    @Transactional
    public PolizaResponseDTO renovarPoliza(Long id, BigDecimal ipc) {
        log.info("Renovando póliza ID: {} con IPC: {}", id, ipc);
        Poliza poliza = obtenerPolizaEntity(id);

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new BusinessRuleException("No se puede renovar una póliza CANCELADA");
        }

        BigDecimal valorIpc = (ipc != null) ? ipc : new BigDecimal("0.05");
        BigDecimal factorMultiplicador = BigDecimal.ONE.add(valorIpc);

        BigDecimal nuevoCanon = poliza.getCanonMensual()
                .multiply(factorMultiplicador)
                .setScale(2, RoundingMode.HALF_UP);

        poliza.setCanonMensual(nuevoCanon);
        poliza.recalcularPrimaTotal();
        poliza.setEstado(EstadoPoliza.RENOVADA);

        Poliza renovada = polizaRepository.save(poliza);

        coreIntegrationClient.notificarEvento("ACTUALIZACION", id);

        return polizaMapper.toDTO(renovada);
    }

    @Override
    @Transactional
    public PolizaResponseDTO cancelarPoliza(Long id) {
        log.info("Cancelando póliza ID: {}", id);
        Poliza poliza = obtenerPolizaEntity(id);

        poliza.setEstado(EstadoPoliza.CANCELADA);

        if (poliza.getRiesgos() != null) {
            poliza.getRiesgos().forEach(riesgo -> riesgo.setEstado(EstadoRiesgo.CANCELADO));
        }

        Poliza cancelada = polizaRepository.save(poliza);

        coreIntegrationClient.notificarEvento("ACTUALIZACION", id);

        return polizaMapper.toDTO(cancelada);
    }

    @Override
    @Transactional
    public RiesgoResponseDTO agregarRiesgo(Long polizaId, RiesgoRequestDTO riesgoDto) {
        log.info("Agregando riesgo a póliza ID: {}", polizaId);
        Poliza poliza = obtenerPolizaEntity(polizaId);

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new BusinessRuleException("No se pueden agregar riesgos a una póliza CANCELADA");
        }

        if (poliza.getTipo() == TipoPoliza.INDIVIDUAL && !poliza.getRiesgos().isEmpty()) {
            throw new BusinessRuleException("Una póliza individual solo puede tener 1 riesgo");
        }

        Riesgo riesgo = riesgoMapper.toEntity(riesgoDto);
        poliza.agregarRiesgo(riesgo);

        Poliza actualizada = polizaRepository.save(poliza);

        Riesgo riesgoGuardado = actualizada.getRiesgos().get(actualizada.getRiesgos().size() - 1);

        coreIntegrationClient.notificarEvento("ACTUALIZACION", polizaId);

        return riesgoMapper.toDTO(riesgoGuardado);
    }

    private Poliza obtenerPolizaEntity(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Póliza no encontrada con el ID: " + id));
    }
}
