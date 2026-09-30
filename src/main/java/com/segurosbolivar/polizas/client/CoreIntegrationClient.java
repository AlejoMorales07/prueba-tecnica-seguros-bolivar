package com.segurosbolivar.polizas.client;

public interface CoreIntegrationClient {

    void notificarEvento(String evento, Long polizaId);
}
