package com.segurosbolivar.polizas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.segurosbolivar.polizas.dto.PolizaRequestDTO;
import com.segurosbolivar.polizas.entity.TipoPoliza;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PolizaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Debe retornar 401 Unauthorized si no se envía el header api-key")
    void listarPolizas_sinApiKey_retorna401() throws Exception {
        mockMvc.perform(get("/polizas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Debe listar pólizas precargadas cuando se envía api-key correcta")
    void listarPolizas_conApiKeyValida_retorna200() throws Exception {
        mockMvc.perform(get("/polizas")
                        .header("api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Debe renovar póliza precargada ID 1 y retornar 200 OK")
    void renovarPoliza_exito() throws Exception {
        mockMvc.perform(post("/polizas/1/renovar")
                        .header("api-key", "123456")
                        .param("ipc", "0.10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RENOVADA"))
                .andExpect(jsonPath("$.canonMensual").value(1650000.00));
    }
}
