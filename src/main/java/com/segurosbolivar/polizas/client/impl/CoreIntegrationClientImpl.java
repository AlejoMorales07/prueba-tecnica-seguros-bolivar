package com.segurosbolivar.polizas.client.impl;

import com.segurosbolivar.polizas.client.CoreIntegrationClient;
import com.segurosbolivar.polizas.dto.CoreEventoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoreIntegrationClientImpl implements CoreIntegrationClient {

    private final RestTemplate restTemplate;

    @Value("${app.core-mock.url:http://localhost:8080/core-mock/evento}")
    private String coreMockUrl;

    @Override
    public void notificarEvento(String evento, Long polizaId) {
        CoreEventoDTO payload = CoreEventoDTO.builder()
                .evento(evento)
                .polizaId(polizaId)
                .build();

        log.info("Invocando cliente de integración CORE -> URL: {}, Evento: {}, PolizaId: {}", coreMockUrl, evento, polizaId);

        try {
            restTemplate.postForEntity(coreMockUrl, payload, String.class);
            log.info("Notificación al CORE enviada exitosamente para la póliza ID: {}", polizaId);
        } catch (Exception ex) {
            log.error("Error al notificar al CORE para la póliza ID: {}. Causa: {}", polizaId, ex.getMessage());
        }
    }
}
