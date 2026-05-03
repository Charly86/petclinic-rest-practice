# Casos de Prueba Manuales - Consultas Avanzadas

Fecha: 2026-04-10  
Ámbito: `GET /api/owners`, `GET /api/pets`, `GET /api/visits`

## 1. Convenciones

Prioridad:
- P0: bloqueante para salida.
- P1: alta prioridad.
- P2: media prioridad.

Resultado esperado común:
- `200` para consultas válidas.
- `400` con `ProblemDetail` para consultas inválidas.
- Presencia de cabeceras: `X-Page-Number`, `X-Page-Size`, `X-Total-Elements`, `X-Total-Pages` en respuestas `200`.

## 2. Casos Comunes (contrato transversal)

| ID | Prioridad | Objetivo | Request | Resultado esperado |
|---|---|---|---|---|
| TC-COM-001 | P0 | Validar defaults sin query params | `/api/owners` | `200`, página 0, tamaño 20, `sort=id,asc`, cabeceras presentes |
| TC-COM-002 | P0 | Rechazar `page` negativo | `/api/owners?page=-1` | `400` con `ProblemDetail` |
| TC-COM-003 | P0 | Rechazar `size=0` | `/api/pets?size=0` | `400` con `ProblemDetail` |
| TC-COM-004 | P0 | Rechazar `size>100` | `/api/visits?size=101` | `400` con `ProblemDetail` |
| TC-COM-005 | P1 | Rechazar sort sin formato `campo,direccion` | `/api/pets?sort=name` | `400` con detalle de formato inválido |
| TC-COM-006 | P1 | Rechazar dirección de sort inválida | `/api/visits?sort=date,descending` | `400` con detalle de dirección inválida |

## 3. Owners

| ID | Prioridad | Objetivo | Request | Resultado esperado |
|---|---|---|---|---|
| TC-OWN-001 | P0 | Filtro por `lastName` prefijo | `/api/owners?lastName=Sm` | Solo owners cuyo apellido empieza por `Sm` |
| TC-OWN-002 | P0 | Filtro por `firstName` prefijo | `/api/owners?firstName=Jo` | Solo owners cuyo nombre empieza por `Jo` |
| TC-OWN-003 | P0 | Filtro por `city` prefijo | `/api/owners?city=Bar` | Solo owners con ciudad prefijo `Bar` |
| TC-OWN-004 | P0 | Filtro por `telephone` exacto | `/api/owners?telephone=934123456` | Solo owners con ese teléfono exacto |
| TC-OWN-005 | P0 | Filtros combinados (AND) | `/api/owners?lastName=Sm&city=Barcelona` | Solo owners que cumplan ambas condiciones |
| TC-OWN-006 | P1 | Ordenación por `lastName,desc` | `/api/owners?sort=lastName,desc` | Orden descendente, estable para empates |
| TC-OWN-007 | P1 | Paginación explícita | `/api/owners?page=1&size=10` | `200`, 10 elementos máximo, cabeceras coherentes |
| TC-OWN-008 | P0 | Sort field no permitido | `/api/owners?sort=unknownField,asc` | `400` con `ProblemDetail` |
| TC-OWN-009 | P1 | Input malicioso en sort | `/api/owners?sort=lastName;drop table owners,asc` | `400`, sin stacktrace ni detalle SQL |

## 4. Pets

| ID | Prioridad | Objetivo | Request | Resultado esperado |
|---|---|---|---|---|
| TC-PET-001 | P0 | Filtro por `name` contains (case-insensitive) | `/api/pets?name=fl` | Solo pets cuyo nombre contiene `fl` |
| TC-PET-002 | P0 | Filtro por `typeId` | `/api/pets?typeId=3` | Solo pets de tipo 3 |
| TC-PET-003 | P0 | Filtro por `ownerId` | `/api/pets?ownerId=12` | Solo pets del owner 12 |
| TC-PET-004 | P0 | Filtro por rango de fechas inclusivo | `/api/pets?birthDateFrom=2024-01-01&birthDateTo=2024-12-31` | Solo pets en rango inclusive |
| TC-PET-005 | P0 | Filtros combinados (owner + type) | `/api/pets?ownerId=12&typeId=3` | Solo pets que cumplen ambas condiciones |
| TC-PET-006 | P1 | Ordenación por `name,asc` | `/api/pets?sort=name,asc` | Orden ascendente por nombre, estable |
| TC-PET-007 | P1 | Paginación con tamaño 25 | `/api/pets?page=0&size=25` | `200`, máximo 25, cabeceras coherentes |
| TC-PET-008 | P0 | Rango de fechas inválido | `/api/pets?birthDateFrom=2024-12-31&birthDateTo=2024-01-01` | `400` con `ProblemDetail` |
| TC-PET-009 | P0 | Sort field no permitido | `/api/pets?sort=color,asc` | `400` con `ProblemDetail` |
| TC-PET-010 | P1 | Input malicioso en filtro texto | `/api/pets?name=' or '1'='1` | No `500`; respuesta controlada (`200` o `400`) |

## 5. Visits

| ID | Prioridad | Objetivo | Request | Resultado esperado |
|---|---|---|---|---|
| TC-VIS-001 | P0 | Filtro por `petId` | `/api/visits?petId=7` | Solo visits de `petId=7` |
| TC-VIS-002 | P0 | Filtro por rango fecha inclusivo | `/api/visits?dateFrom=2026-01-01&dateTo=2026-01-31` | Solo visits en rango inclusive |
| TC-VIS-003 | P0 | Filtro por descripción contains | `/api/visits?descriptionContains=checkup` | Solo visits cuyo texto contiene `checkup` |
| TC-VIS-004 | P0 | Filtros combinados (pet + rango) | `/api/visits?petId=7&dateFrom=2026-01-01&dateTo=2026-01-31` | Solo visits que cumplen todo (AND) |
| TC-VIS-005 | P1 | Ordenación por `date,desc` | `/api/visits?sort=date,desc` | Orden descendente por fecha, estable |
| TC-VIS-006 | P1 | Paginación con página alta | `/api/visits?page=2&size=5` | `200`, cabeceras coherentes con totales |
| TC-VIS-007 | P0 | Rango inválido | `/api/visits?dateFrom=2026-02-10&dateTo=2026-01-01` | `400` con `ProblemDetail` |
| TC-VIS-008 | P0 | Sort field no permitido | `/api/visits?sort=description,asc` | `400` con `ProblemDetail` |
| TC-VIS-009 | P1 | Input malicioso en sort | `/api/visits?sort=date desc;select * from users,asc` | `400`, sin fuga técnica |

## 6. Cierre de Ejecución Manual

Criterios de aceptación manual:
- Todos los casos P0 en PASS.
- Al menos 95% de casos P1 en PASS.
- 0 bugs abiertos de severidad alta/crítica para este alcance.

Evidencia mínima por caso:
- URL invocada completa.
- Código HTTP.
- Fragmento de body o cabeceras relevantes.
- Resultado PASS/FAIL.
- ID de bug enlazado en caso de FAIL.
