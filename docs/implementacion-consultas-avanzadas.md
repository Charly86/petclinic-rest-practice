# Implementación de Consultas Avanzadas (Owners, Pets, Visits)

## 1. Resumen Ejecutivo

Se ha implementado soporte de **consultas avanzadas** para los listados de:
- `GET /api/owners`
- `GET /api/pets`
- `GET /api/visits`

El alcance incluye:
- Filtros por recurso.
- Paginación uniforme.
- Ordenación con whitelist de campos.
- Validación de parámetros con respuesta `400` en formato `ProblemDetail`.
- Cabeceras de metadatos de paginación en respuestas exitosas.
- Cobertura de pruebas funcionales, no funcionales y de paridad por perfil (`jpa`, `jdbc`, `spring-data-jpa`).

## 2. Documentación Funcional

## 2.1 Endpoints Cubiertos

- `GET /api/owners`
- `GET /api/pets`
- `GET /api/visits`

## 2.2 Contrato Común de Consulta

Parámetros comunes:
- `page`:
  - índice base 0.
  - default: `0`.
- `size`:
  - tamaño de página.
  - default: `20`.
  - rango permitido: `1..100`.
- `sort`:
  - formato: `campo,direccion`.
  - dirección: `asc|desc`.
  - default: `id,asc`.

Cabeceras devueltas en `200`:
- `X-Page-Number`
- `X-Page-Size`
- `X-Total-Elements`
- `X-Total-Pages`

## 2.3 Filtros por Recurso

Owners (`GET /api/owners`):
- `lastName` (prefijo)
- `firstName` (prefijo)
- `city` (prefijo)
- `telephone` (exacto)

Whitelist de ordenación owners:
- `id`, `lastName`, `firstName`, `city`

Pets (`GET /api/pets`):
- `name` (contains case-insensitive)
- `typeId` (exacto)
- `ownerId` (exacto)
- `birthDateFrom` (inclusive)
- `birthDateTo` (inclusive)

Whitelist de ordenación pets:
- `id`, `name`, `birthDate`, `typeId`, `ownerId`

Visits (`GET /api/visits`):
- `petId` (exacto)
- `dateFrom` (inclusive)
- `dateTo` (inclusive)
- `descriptionContains` (contains case-insensitive)

Whitelist de ordenación visits:
- `id`, `date`, `petId`

## 2.4 Reglas de Validación

Se responde `400 Bad Request` cuando:
- `page < 0`
- `size <= 0` o `size > 100`
- `sort` no cumple `campo,direccion`
- campo de ordenación fuera de whitelist
- dirección fuera de `asc|desc`
- rango de fechas inválido (`from > to`)

## 2.5 Compatibilidad

- Se mantiene el body de respuesta como lista de DTOs.
- Se conserva el comportamiento esperado cuando no se envían parámetros nuevos (defaults aplicados).
- Se añade información de paginado vía cabeceras sin romper el payload.

## 3. Documentación Técnica

## 3.1 Diseño General

Se introdujo una capa de consulta común para evitar divergencia entre recursos:
- Parser y validación de parámetros.
- Criterios tipados por recurso.
- Estructura común de paginado/ordenación.
- Resultado paginado con metadatos.

## 3.2 Componentes Nuevos

Paquete: `src/main/java/org/springframework/samples/petclinic/service/query`

Clases principales:
- `OwnerQueryCriteria`
- `PetQueryCriteria`
- `VisitQueryCriteria`
- `QueryPageRequest`
- `SortOption`
- `SortDirection`
- `PagedResult<T>`
- `QueryRequestParser`
- `QueryPagination`
- `QueryValidationException`

## 3.3 Cambios por Capa

OpenAPI:
- Actualización de `src/main/resources/openapi.yml` con nuevos parámetros y headers de paginación para los 3 listados.

Controladores:
- `OwnerRestController`
- `PetRestController`
- `VisitRestController`

Cambios aplicados:
- Nuevas firmas de métodos de listado según OpenAPI generado.
- Conversión de query params a criterios + `QueryPageRequest`.
- Validación de rangos de fecha en pets/visits.
- Seteo de cabeceras de paginación en respuestas `200`.

Servicio:
- `ClinicService` y `ClinicServiceImpl` ampliados con:
  - `findOwners(OwnerQueryCriteria, QueryPageRequest)`
  - `findPets(PetQueryCriteria, QueryPageRequest)`
  - `findVisits(VisitQueryCriteria, QueryPageRequest)`

`ClinicServiceImpl`:
- Filtrado en memoria con predicados por recurso.
- Ordenación con comparadores por whitelist y fallback determinista por `id`.
- Paginación con `QueryPagination`.

Repositorios (paridad por perfil):
- Interfaces `OwnerRepository`, `PetRepository`, `VisitRepository` añaden `findAllForAdvancedQuery()`.
- Implementaciones `jpa`, `jdbc`, `spring-data-jpa` implementan el método (delegando a `findAll()` para semántica homogénea).

Manejo de errores:
- `ExceptionControllerAdvice` extendido para manejar:
  - `QueryValidationException`
  - `MethodArgumentTypeMismatchException`
  - `IllegalArgumentException`
- Todas estas rutas devuelven `400` con `ProblemDetail`.

## 3.4 Estrategia de Seguridad y Robustez

- Rechazo explícito de campos de ordenación no permitidos.
- Rechazo de direcciones de orden inválidas.
- Rechazo de inputs maliciosos en `sort`/rangos sin producir `500`.
- No exposición de detalles internos en respuestas de error de query inválida.

## 4. Validación y Pruebas

## 4.1 Pruebas Funcionales

Controladores REST:
- `OwnerRestControllerTests`
- `PetRestControllerTests`
- `VisitRestControllerTests`

Cobertura validada:
- listados con nuevos parámetros,
- cabeceras de paginación,
- casos `400` por parámetros inválidos,
- regresión de comportamiento existente.

## 4.2 Pruebas de Consistencia por Perfil

Servicio cross-profile:
- `ClinicServiceJpaTests`
- `ClinicServiceJdbcTests`
- `ClinicServiceSpringDataJpaTests`

## 4.3 Pruebas No Funcionales

Parser y robustez:
- `QueryRequestParserTests`

Rendimiento (objetivo p95):
- `AdvancedQueryPerformanceTests`
- Dataset sintético >= 10k por recurso.
- Objetivo: p95 <= 500ms para `size<=50`.

## 4.4 Comando de Validación Ejecutado

```bash
mvn -q "-Denforcer.skip=true" -Dtest=OwnerRestControllerTests,PetRestControllerTests,VisitRestControllerTests,QueryRequestParserTests,AdvancedQueryPerformanceTests,ClinicServiceJpaTests,ClinicServiceJdbcTests,ClinicServiceSpringDataJpaTests test
```

Resultado: **OK**.

## 5. Artefactos y Estado OpenSpec

- Cambio OpenSpec: `advanced-api-queries-owners-pets-visits`
- Tareas: `26/26` completadas.
- Cambio archivado en:
  - `openspec/changes/archive/2026-04-10-advanced-api-queries-owners-pets-visits`
- Delta specs sincronizadas en:
  - `openspec/specs/owners-advanced-querying/spec.md`
  - `openspec/specs/pets-advanced-querying/spec.md`
  - `openspec/specs/visits-advanced-querying/spec.md`

## 6. Documentación Relacionada

- `docs/advanced-querying.md`
- `docs/advanced-querying-validation-report.md`
- `docs/advanced-querying-release-checklist.md`

