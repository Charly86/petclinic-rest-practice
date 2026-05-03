# Suite de Regresión y No Funcional - Consultas Avanzadas

Fecha: 2026-04-10  
Ámbito: Evolutivo `advanced-api-queries-owners-pets-visits`

## 1. Objetivo

Definir la ejecución recomendada de regresión y validaciones no funcionales para proteger:
- contrato funcional del evolutivo,
- comportamiento histórico del API,
- rendimiento y robustez.

## 2. Suites de Regresión

### 2.1 Smoke Suite (15-30 min)

Objetivo: validar rápidamente que el evolutivo está operativo tras despliegue.

Cobertura mínima:
- `GET /api/owners` con defaults.
- `GET /api/pets` con un filtro válido.
- `GET /api/visits` con un filtro válido.
- Un caso `400` por query inválida.
- Verificación de cabeceras de paginación.

Criterio de corte:
- Si falla cualquier prueba P0, se bloquea promoción de build.

### 2.2 Targeted Regression (45-90 min)

Objetivo: validar cambios del evolutivo de forma completa.

Cobertura:
- Todos los casos de filtros válidos por recurso.
- Combinación AND de filtros.
- Sort whitelist + orden estable.
- Rango fechas válido/inválido.
- Límites de `page/size`.
- Errores `400` con `ProblemDetail`.

### 2.3 Full Regression (2-4 h)

Objetivo: validar que el evolutivo no rompe funcionalidad existente del módulo REST.

Cobertura:
- Suite targeted completa.
- Escenarios históricos relacionados con owners/pets/visits sin query avanzada.
- Validación cross-profile (`jpa`, `jdbc`, `spring-data-jpa`).

## 3. Plan No Funcional

## 3.1 Rendimiento (AC-NF-P95)

Criterio:
- p95 <= 500 ms para peticiones con `size<=50` y dataset >= 10.000 registros por recurso.

Ejecución:
- Test automatizado: `AdvancedQueryPerformanceTests`.
- Repetir ejecución al menos 3 veces y registrar p50/p95/p99.

Resultado esperado:
- 3/3 ejecuciones por debajo del umbral p95.

## 3.2 Robustez y Seguridad de Entrada (AC-NF-ROB)

Criterio:
- Inputs malformados o maliciosos no generan `500`.
- No se exponen stack traces ni detalles internos (SQL, clases internas).

Ejecución:
- Test automatizado: `QueryRequestParserTests`.
- Casos manuales maliciosos en `sort` y filtros texto (owners/pets/visits).

Resultado esperado:
- Respuestas controladas (`400` o respuesta de negocio válida), sin fuga técnica.

## 3.3 Paridad de Comportamiento Multi-Perfil (AC-NF-PAR)

Criterio:
- Misma semántica funcional en perfiles `jpa`, `jdbc`, `spring-data-jpa`.

Ejecución:
- `ClinicServiceJpaTests`
- `ClinicServiceJdbcTests`
- `ClinicServiceSpringDataJpaTests`

Resultado esperado:
- Sin diferencias funcionales entre perfiles en filtros, orden y paginación.

## 4. Orden de Ejecución Recomendado

1. Smoke suite.
2. Targeted regression.
3. Validación no funcional (robustez + performance).
4. Full regression (previa a release).

## 5. Comandos Automatizados

```bash
mvn "-Denforcer.skip=true" -Dtest=OwnerRestControllerTests,PetRestControllerTests,VisitRestControllerTests test
mvn "-Denforcer.skip=true" -Dtest=QueryRequestParserTests,AdvancedQueryPerformanceTests test
mvn "-Denforcer.skip=true" -Dtest=ClinicServiceJpaTests,ClinicServiceJdbcTests,ClinicServiceSpringDataJpaTests test
```

## 6. Plantilla de Reporte de Ejecución

```markdown
# Reporte QA - Consultas Avanzadas

Fecha:
Build:
Entorno:
Responsable:

## Resumen
- Total casos ejecutados:
- Pass:
- Fail:
- Blocked:
- Pass rate:

## Resultado por criterio de aceptación
- AC-OWN-FLT: PASS/FAIL
- AC-OWN-PAG: PASS/FAIL
- AC-OWN-SORT: PASS/FAIL
- AC-OWN-400: PASS/FAIL
- AC-PET-FLT: PASS/FAIL
- AC-PET-PAG: PASS/FAIL
- AC-PET-SORT: PASS/FAIL
- AC-PET-400: PASS/FAIL
- AC-VIS-FLT: PASS/FAIL
- AC-VIS-PAG: PASS/FAIL
- AC-VIS-SORT: PASS/FAIL
- AC-VIS-400: PASS/FAIL
- AC-NF-P95: PASS/FAIL
- AC-NF-ROB: PASS/FAIL
- AC-NF-PAR: PASS/FAIL

## Defectos
- BUG-ID / severidad / estado / criterio impactado

## Riesgos abiertos
- Riesgo / impacto / plan de mitigación

## Recomendación de salida
- GO / NO-GO
- Justificación
```
