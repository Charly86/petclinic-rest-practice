# Plan Maestro de Pruebas - Evolutivo de Consultas Avanzadas

Fecha: 2026-04-10  
Versión del plan: 1.0  
Cambio OpenSpec: `advanced-api-queries-owners-pets-visits`

## 1. Objetivo

Definir la estrategia de validación funcional y no funcional para el evolutivo de consultas avanzadas en:
- `GET /api/owners`
- `GET /api/pets`
- `GET /api/visits`

El plan garantiza trazabilidad con los criterios de aceptación aprobados y cobertura de regresión sobre comportamiento existente.

## 2. Alcance

En alcance:
- Filtros por dominio en owners, pets y visits.
- Paginación común (`page`, `size`) con defaults y límites.
- Ordenación (`sort=campo,direccion`) con whitelist por endpoint.
- Validaciones de query params y respuesta `400` con `ProblemDetail`.
- Cabeceras de paginación en respuestas `200`.
- Paridad de comportamiento en perfiles `jpa`, `jdbc`, `spring-data-jpa`.
- Criterios no funcionales de latencia y robustez.

Fuera de alcance:
- Cambios en payload funcional de entidades fuera del contrato actual.
- Optimización de base de datos más allá de la validación de SLO definida.
- Nuevos endpoints distintos de `/owners`, `/pets`, `/visits`.

## 3. Matriz de Criterios de Aceptación

| ID | Criterio | Tipo | Verificación |
|---|---|---|---|
| AC-OWN-FLT | Owners filtra por `lastName`, `firstName`, `city`, `telephone` combinables (AND) | Funcional | Casos manuales + `OwnerRestControllerTests` |
| AC-OWN-PAG | Owners aplica defaults y límites de paginación | Funcional | Casos manuales + `OwnerRestControllerTests` |
| AC-OWN-SORT | Owners ordena por whitelist con orden estable | Funcional | Casos manuales + tests controlador/servicio |
| AC-OWN-400 | Owners devuelve `400` en query inválida | Funcional/Robustez | Casos negativos + `QueryRequestParserTests` |
| AC-PET-FLT | Pets filtra por `name`, `typeId`, `ownerId`, rango de fechas | Funcional | Casos manuales + `PetRestControllerTests` |
| AC-PET-PAG | Pets aplica defaults y límites de paginación | Funcional | Casos manuales + `PetRestControllerTests` |
| AC-PET-SORT | Pets ordena por whitelist con orden estable | Funcional | Casos manuales + tests controlador/servicio |
| AC-PET-400 | Pets devuelve `400` en query inválida | Funcional/Robustez | Casos negativos + `QueryRequestParserTests` |
| AC-VIS-FLT | Visits filtra por `petId`, rango de fechas, `descriptionContains` | Funcional | Casos manuales + `VisitRestControllerTests` |
| AC-VIS-PAG | Visits aplica defaults y límites de paginación | Funcional | Casos manuales + `VisitRestControllerTests` |
| AC-VIS-SORT | Visits ordena por whitelist con orden estable | Funcional | Casos manuales + tests controlador/servicio |
| AC-VIS-400 | Visits devuelve `400` en query inválida | Funcional/Robustez | Casos negativos + `QueryRequestParserTests` |
| AC-NF-P95 | p95 <= 500 ms para `size<=50` y dataset >= 10k | No funcional | `AdvancedQueryPerformanceTests` |
| AC-NF-ROB | Inputs maliciosos/malformados no generan `500` ni fuga técnica | No funcional/Seguridad | `QueryRequestParserTests` + casos manuales |
| AC-NF-PAR | Paridad funcional entre `jpa`,`jdbc`,`spring-data-jpa` | No funcional/Confiabilidad | `ClinicServiceJpa/Jdbc/SpringDataJpaTests` |

## 4. Estrategia de Pruebas

Niveles de validación:
- API funcional (happy path y combinaciones de filtros).
- API negativa (parámetros inválidos, formato inválido, rangos inválidos).
- Contrato de cabeceras de paginación.
- Regresión del comportamiento previo (sin parámetros nuevos).
- Paridad multi-perfil de persistencia.
- No funcional: rendimiento p95, robustez ante entrada maliciosa.

Técnicas aplicadas:
- Equivalence partitioning para `page`, `size`, `sort`.
- Boundary value para `size` y rangos de fechas.
- Pairwise básico para combinación de filtros por recurso.
- Pruebas de resiliencia sobre entradas malformadas.

## 5. Entorno y Datos

Entornos:
- Local CI-like con Maven y perfiles de repositorio disponibles.
- Entorno de integración (si aplica) para smoke final de API.

Datos mínimos recomendados:
- Owners: >= 50 registros variados en nombres, ciudad y teléfono.
- Pets: >= 100 registros con variedad de `typeId`, `ownerId` y fechas.
- Visits: >= 200 registros con descripciones diversas y fechas.

Datos para no funcional:
- Dataset sintético >= 10.000 por recurso para medir p95.

## 6. Criterios de Entrada y Salida

Criterios de entrada:
- OpenAPI actualizado y desplegado.
- Build en verde.
- Datos de prueba cargados.
- Ambientes de ejecución accesibles.

Criterios de salida funcional:
- 100% de casos P0 ejecutados.
- 100% de criterios AC funcionales en estado PASS.
- 0 defectos abiertos de severidad crítica/alta para este alcance.

Criterios de salida no funcional:
- AC-NF-P95 en PASS (p95 <= 500 ms).
- AC-NF-ROB en PASS (sin `500`, sin stack traces ni detalles internos).
- AC-NF-PAR en PASS (sin divergencias entre perfiles).

## 7. Riesgos y Mitigaciones

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Datos de prueba poco representativos | Falsos positivos/negativos | Preparar dataset con alta variabilidad y bordes |
| Diferencias entre perfiles de persistencia | Inconsistencias en producción | Ejecutar siempre suites cross-profile |
| Degradación de latencia con filtros combinados | Incumplimiento SLO | Monitorear p95 y ajustar estrategia de fetch/filtrado |
| Mensajes de error inconsistentes | Baja trazabilidad QA/soporte | Normalizar validación por `ProblemDetail` |

## 8. Entregables de QA

- Plan maestro de pruebas (este documento).
- Catálogo de casos manuales funcionales/negativos.
- Suite de regresión y no funcional con orden de ejecución.
- Informe de ejecución por ciclo (pass/fail, bugs, riesgos).

## 9. Comandos de Referencia

```bash
mvn "-Denforcer.skip=true" -Dtest=OwnerRestControllerTests,PetRestControllerTests,VisitRestControllerTests test
mvn "-Denforcer.skip=true" -Dtest=ClinicServiceJpaTests,ClinicServiceJdbcTests,ClinicServiceSpringDataJpaTests test
mvn "-Denforcer.skip=true" -Dtest=QueryRequestParserTests,AdvancedQueryPerformanceTests test
```
