## Context

La API REST actual de Petclinic expone listados de `owners`, `pets` y `visits` con comportamiento básico:
- `owners` permite filtrar solo por `lastName`.
- `pets` y `visits` devuelven listados completos sin filtros ni paginación.
- Las respuestas de listado son arrays planos (`List<...Dto>`), sin metadatos de página.

Además, el proyecto mantiene varias implementaciones de repositorio (`jpa`, `jdbc`, `springdatajpa`), por lo que la solución debe preservar consistencia funcional entre perfiles.

## Goals / Non-Goals

**Goals:**
- Definir un contrato uniforme de filtros, paginación y ordenación para `owners`, `pets` y `visits`.
- Mantener compatibilidad hacia atrás para clientes existentes.
- Validar parámetros de consulta y devolver `400` con mensajes claros ante valores/campos inválidos.
- Permitir extensión incremental de filtros sin rediseñar endpoints.
- Hacer verificables los criterios de aceptación funcionales y no funcionales.

**Non-Goals:**
- Cambiar endpoints de detalle/alta/edición/borrado.
- Introducir búsqueda de texto libre avanzada o full-text search.
- Cambiar el modelo de dominio principal (Owner/Pet/Visit) más allá de lo necesario para consultas.
- Resolver optimizaciones de caching distribuido en esta iteración.

## Decisions

1. **Contrato de query params homogéneo por recurso**
- Parámetros comunes: `page`, `size`, `sort`.
- Defaults globales: `page=0`, `size=20`, `sort=id,asc`.
- Límite de seguridad: `size` máximo `100`.
- Cada endpoint define filtros propios y whitelist cerrada de campos de ordenación.
- Alternativa descartada: `filter` JSON único (peor legibilidad y validación más compleja).

2. **Compatibilidad de payload y semántica de listados**
- El body de respuesta se mantiene como lista de DTOs para no romper clientes.
- Se añaden cabeceras de paginación en respuestas con resultados: `X-Page-Number`, `X-Page-Size`, `X-Total-Elements`, `X-Total-Pages`.
- Se mantiene la semántica actual de listados vacíos para no introducir ruptura funcional inesperada.

3. **Capa de criterios tipados y validación temprana**
- Se introducen criterios por recurso (`OwnerQueryCriteria`, `PetQueryCriteria`, `VisitQueryCriteria`) y estructura común para paginación/ordenación.
- La validación se ejecuta antes de acceder a repositorio: formato de `sort`, límites de `size`, rangos de fechas, valores negativos.
- Alternativa descartada: validación parcial por repositorio (riesgo de comportamiento inconsistente entre perfiles).

4. **Paridad entre repositorios por perfil**
- Se implementa el mismo contrato en `jpa`, `jdbc` y `springdatajpa`.
- Se exige ordenación estable con fallback por `id` para empates.
- Se validará paridad con tests de contrato compartidos.

5. **Error handling y hardening**
- Parámetros inválidos devolverán `400 Bad Request` con `ProblemDetail`.
- Campos de ordenación fuera de whitelist y entradas maliciosas no deben llegar como SQL dinámico sin control.
- Alternativa descartada: ignorar parámetros inválidos silenciosamente (comportamiento no determinista).

## Risks / Trade-offs

- **[Riesgo] Divergencia entre implementaciones de repositorio (`jpa`/`jdbc`/`springdatajpa`)** -> **Mitigación:** tests de contrato por endpoint ejecutados en cada perfil.
- **[Riesgo] Degradación de rendimiento en filtros combinados** -> **Mitigación:** `size` máximo, filtros indexables y validación de p95 en entorno de pruebas.
- **[Riesgo] Ambigüedad de rangos de fecha** -> **Mitigación:** rango inclusivo y validación obligatoria `from <= to`.
- **[Trade-off] Metadatos en cabeceras en lugar de body** -> **Mitigación:** documentación explícita y ejemplos de consumo.

## Acceptance Criteria

### Funcionales
- Cada endpoint soporta sus filtros definidos y combinación AND.
- Cada endpoint soporta `page`, `size`, `sort` con defaults y límites comunes.
- Ordenación solo por campos permitidos; dirección válida: `asc|desc`.
- Parámetros inválidos generan `400` con mensaje accionable.
- Misma petición y mismo estado de datos devuelven orden estable.
- Sin parámetros nuevos, se mantiene comportamiento esperado por clientes actuales.

### No funcionales
- **Rendimiento:** p95 <= 500 ms para consultas con `size<=50` sobre dataset >= 10k registros por recurso.
- **Fiabilidad:** tasa de error 5xx = 0% en suite de pruebas de consultas avanzadas.
- **Seguridad:** entradas maliciosas en filtros/sort devuelven `400`, sin stack traces ni detalles internos en respuesta.
- **Consistencia:** paridad de resultados entre perfiles `jpa`, `jdbc` y `springdatajpa` para los mismos casos de prueba.

## Migration Plan

1. Actualizar contrato OpenAPI de listados (`owners`, `pets`, `visits`) con parámetros, defaults, límites y cabeceras.
2. Regenerar interfaces/DTOs API y ajustar controladores.
3. Implementar criterios, validaciones y consultas avanzadas en servicio/repositorios por perfil.
4. Añadir manejo de errores de query params en advice/controladores.
5. Ejecutar validación funcional y no funcional basada en criterios de aceptación.
6. Despliegue sin migración de datos; rollback mediante reversión del cambio de código.

## Open Questions

- ¿Qué dataset sintético estándar se utilizará en CI para validar el criterio p95?
- ¿Se publicará una guía de migración para consumidores que actualmente dependen de respuestas sin cabeceras de paginación?
