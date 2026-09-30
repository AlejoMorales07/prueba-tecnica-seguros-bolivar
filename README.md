# 🛡️ Sistema de Gestión de Pólizas y Riesgos - Seguros Bolívar

Proyecto backend completo desarrollado en **Java 17 / 21** y **Spring Boot 3.x** como solución a la prueba técnica para **Seguros Bolívar**. Aplica de forma estricta Clean Code, Principios SOLID, Clean Layered Architecture y buenas prácticas enterprise.

---

## 📐 Arquitectura y Patrones de Diseño

La aplicación sigue una **Clean Layered Architecture** (Arquitectura Limpia en Capas) para asegurar el desacoplamiento, mantenibilidad y alta testeabilidad:

- **`config/`**: Seguridad mediante filtro Servlet `SecurityFilter` para exigir el header `api-key: 123456`, beans de configuración e inicializador de datos semilla.
- **`controller/`**: REST Controllers delgados. Responsables únicamente del mapeo HTTP, recepción de parámetros y delegación a la capa de servicios.
- **`dto/`**: Objetos de transferencia de datos inmutables y validados mediante Bean Validation (`@NotNull`, `@NotBlank`, `@DecimalMin`). Evita exposición de entidades JPA y bucles infinitos en serialización JSON.
- **`entity/`**: Entidades del modelo de dominio JPA (`Poliza`, `Riesgo`) y Enums (`TipoPoliza`, `EstadoPoliza`, `EstadoRiesgo`). Métodos de dominio para encapsular la lógica de cálculo monetario.
- **`exception/`**: Manejo centralizado de excepciones mediante `@RestControllerAdvice` (`GlobalExceptionHandler`). Retorna respuestas formateadas en JSON con código de estado HTTP adecuado (`400 Bad Request`, `401 Unauthorized`, `404 Not Found`, `500 Internal Server Error`).
- **`mapper/`**: Componentes dedicados (`PolizaMapper`, `RiesgoMapper`) para desacoplar la conversión entre DTOs y Entidades.
- **`repository/`**: Interfaces `JpaRepository` con soporte para consultas derivadas y filtrado dinámico.
- **`service/` & `service/impl/`**: Lógica de negocio aislada en interfaces cohesivas e implementaciones concretas con inyección de dependencias estricta por constructor (`@RequiredArgsConstructor` de Lombok).
- **`client/` & `client/impl/`**: Adaptador cliente HTTP (`CoreIntegrationClientImpl`) desacoplado mediante interfaz para notificar notificaciones al MOCK CORE (`/core-mock/evento`).

---

## 💡 Principios SOLID Aplicados

1. **S - Single Responsibility Principle:**
   - Controladores: solo contratos REST.
   - Servicios: solo reglas de negocio.
   - Mapeadores: solo conversión de datos.
   - Exception Handler: solo captura y formateo de errores HTTP.
2. **O - Open/Closed Principle & D - Dependency Inversion Principle:**
   - Servicios y clientes desacoplados mediante interfaces (`PolizaService`, `RiesgoService`, `CoreIntegrationClient`).
   - Inyección por constructor con componentes inmutables (`final`).
3. **L - Liskov Substitution & I - Interface Segregation:**
   - Interfaces cohesivas sin métodos innecesarios.
4. **Clean Code & Manejo de Valores Monetarios:**
   - Uso estricto de `BigDecimal` con escala a 2 decimales y `RoundingMode.HALF_UP` para evitar problemas de precisión en punto flotante.

---

## 🔒 Seguridad (Header Obligatorio)

Se implementó el servlet filter `SecurityFilter` para interceptar las peticiones a `/polizas/**` y `/riesgos/**`.
- **Header requerido:** `api-key: 123456`
- **Error si falla:** `HTTP 401 Unauthorized`
- **Rutas libres:** Consola H2 (`/h2-console/**`) y el endpoint del mock core (`/core-mock/**`).

---

## 🚀 Guía de Inicio Rápido

### Prerrequisitos
- JDK 17 o superior.
- Maven 3.8+ (opcional si se utiliza la versión instalada en el sistema).

### Compilación y Ejecución
```bash
# Compilar y ejecutar pruebas unitarias/integración
mvn clean test

# Iniciar la aplicación
mvn spring-boot:run
```
La aplicación iniciará en el puerto **8080**: `http://localhost:8080`.

### Consola H2 Database
- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:polizasdb`
- **Usuario:** `sa`
- **Contraseña:** *(vacía)*

---

## 📋 Datos Semilla Precargados

Al iniciar la aplicación, `DataInitializer` precarga automáticamente:
1. **Póliza ID 1 (INDIVIDUAL):** Canon: \$1,500,000.00 | Vigencia: 12 meses | Prima Total: \$18,000,000.00 | 1 Riesgo (ID 1).
2. **Póliza ID 2 (COLECTIVA):** Canon: \$5,000,000.00 | Vigencia: 12 meses | Prima Total: \$60,000,000.00 | 2 Riesgos (ID 2 y ID 3).

---

## 🧪 Colección de cURLs para Pruebas

### 1. Probar Filtro de Seguridad (Sin Header -> HTTP 401)
```bash
curl -X GET "http://localhost:8080/polizas"
```

### 2. Listar Pólizas (Con Header Obligatorio)
```bash
# Obtener todas las pólizas
curl -X GET "http://localhost:8080/polizas" \
  -H "api-key: 123456"

# Filtrar dinámicamente por tipo y estado
curl -X GET "http://localhost:8080/polizas?tipo=INDIVIDUAL&estado=ACTIVA" \
  -H "api-key: 123456"
```

### 3. Consultar Riesgos de una Póliza
```bash
curl -X GET "http://localhost:8080/polizas/1/riesgos" \
  -H "api-key: 123456"
```

### 4. Renovar Póliza (Incremento por IPC)
```bash
# Renovar Póliza ID 1 especificando IPC del 10% (0.10)
curl -X POST "http://localhost:8080/polizas/1/renovar?ipc=0.10" \
  -H "api-key: 123456"
```

### 5. Agregar Riesgo a Póliza Colectiva
```bash
curl -X POST "http://localhost:8080/polizas/2/riesgos" \
  -H "api-key: 123456" \
  -H "Content-Type: application/json" \
  -d '{
    "descripcionInmueble": "Depósito 305, Zona Franca Fontibón",
    "canonRiesgo": 1200000.00
  }'
```

### 6. Probar Regla de Negocio: Agregar 2º Riesgo a Póliza Individual (Debe retornar 400 Bad Request)
```bash
curl -X POST "http://localhost:8080/polizas/1/riesgos" \
  -H "api-key: 123456" \
  -H "Content-Type: application/json" \
  -d '{
    "descripcionInmueble": "Parqueadero 12, Edificio Empresarial",
    "canonRiesgo": 300000.00
  }'
```

### 7. Cancelar Riesgo Específico
```bash
curl -X POST "http://localhost:8080/riesgos/1/cancelar" \
  -H "api-key: 123456"
```

### 8. Cancelar Póliza (Cancela Póliza y sus Riesgos en Cascada)
```bash
curl -X POST "http://localhost:8080/polizas/1/cancelar" \
  -H "api-key: 123456"
```

### 9. Probar Regla de Negocio: Renovar Póliza Cancelada (Debe retornar 400 Bad Request)
```bash
curl -X POST "http://localhost:8080/polizas/1/renovar?ipc=0.05" \
  -H "api-key: 123456"
```

### 10. Invocación Directa al Endpoint Mock Core
```bash
curl -X POST "http://localhost:8080/core-mock/evento" \
  -H "Content-Type: application/json" \
  -d '{
    "evento": "ACTUALIZACION",
    "polizaId": 555
  }'
```
