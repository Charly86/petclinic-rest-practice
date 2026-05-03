## Why

Los listados actuales de `owners`, `pets` y `visits` no permiten combinar filtros dinámicos con paginación y ordenación consistente, lo que obliga a sobrecargar clientes con lógica adicional y consultas menos eficientes. Esta mejora habilita búsquedas más precisas y escalables para casos de backoffice y explotación de datos.

## What Changes

- Añadir soporte de filtros en endpoints de consulta para `owners`, `pets` y `visits` mediante parámetros de query con combinaciones AND.
- Incorporar paginación estándar en los tres recursos con contrato común:
  - `page`: índice base 0 (default `0`).
  - `size`: tamaño de página (default `20`, máximo `100`).
  - `sort`: formato `campo,direccion` con dirección `asc|desc` (default `id,asc`).
- Definir whitelist explícita de campos de ordenación por recurso para impedir campos arbitrarios.
- Definir reglas de validación para filtros, rangos de fecha y ordenación; parámetros inválidos devolverán `400` con `ProblemDetail`.
- Mantener compatibilidad hacia atrás cuando no se envíen filtros/paginación/ordenación (misma forma de payload y semántica de negocio actual).

## Capabilities

### New Capabilities
- `owners-advanced-querying`: consultas de propietarios con filtros por campos funcionales, paginación y ordenación controlada.
- `pets-advanced-querying`: consultas de mascotas con filtros por atributos de mascota/relación, paginación y ordenación controlada.
- `visits-advanced-querying`: consultas de visitas con filtros por atributos clínicos/fecha, paginación y ordenación controlada.

### Modified Capabilities
- Ninguna.

## Impact

- Endpoints REST de listado para `owners`, `pets` y `visits`.
- Capa de servicio/repositorio para composición de criterios, `Pageable` y `Sort`.
- Contratos de validación de parámetros y estructura de errores HTTP `400`.
- Pruebas de API (filtros, límites de página, ordenaciones válidas/invalidas, combinaciones de criterios).
- Documentación API para nuevos parámetros de query.
- Criterios de aceptación medibles para validación funcional y no funcional en CI.

### Acceptance Criteria (resumen)
- Funcional:
  - Se pueden combinar filtros válidos por recurso y solo se devuelven registros que cumplen todos los criterios.
  - `page`, `size` y `sort` funcionan de forma homogénea en los 3 endpoints.
  - Peticiones con parámetros inválidos devuelven `400` con error explicativo.
  - Sin parámetros nuevos, la API conserva el comportamiento esperado por clientes actuales.
- No funcional:
  - Ordenación estable y determinista entre llamadas idénticas sin cambios de datos.
  - p95 de latencia <= 500 ms para consultas paginadas (`size<=50`) en dataset de prueba >= 10k registros por recurso.
  - Entradas maliciosas en filtros/ordenación no provocan `500` ni exponen detalles internos.
