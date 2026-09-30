package com.segurosbolivar.polizas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.segurosbolivar.polizas.dto.ErrorResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
public class SecurityFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "api-key";
    private static final String REQUIRED_API_KEY_VALUE = "123456";

    private final ObjectMapper objectMapper;

    public SecurityFilter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Excluir rutas públicas (h2-console, core-mock) o peticiones que no inicien con /polizas o /riesgos
        if (shouldSkipFilter(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKeyHeader = request.getHeader(API_KEY_HEADER);

        if (apiKeyHeader == null || !REQUIRED_API_KEY_VALUE.equals(apiKeyHeader)) {
            log.warn("Petición rechazada por header api-key no válido o ausente en ruta: {}", requestURI);

            ErrorResponseDTO errorDTO = ErrorResponseDTO.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                    .message("Acceso denegado: El header 'api-key' es obligatorio y debe contener un valor válido (ej: 123456)")
                    .path(requestURI)
                    .build();

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(errorDTO));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipFilter(String requestURI) {
        return requestURI.startsWith("/h2-console")
                || requestURI.startsWith("/core-mock")
                || (!requestURI.startsWith("/polizas") && !requestURI.startsWith("/riesgos"));
    }
}
