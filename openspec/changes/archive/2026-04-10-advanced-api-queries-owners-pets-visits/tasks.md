## 1. Contrato API y reglas de aceptación

- [x] 1.1 Actualizar OpenAPI de `owners`, `pets` y `visits` con filtros, `page`, `size`, `sort`, defaults (`0`, `20`, `id,asc`) y límite `size<=100`.
- [x] 1.2 Documentar whitelist de ordenación por recurso y ejemplos válidos/invalidos.
- [x] 1.3 Definir y publicar criterios de aceptación funcionales y no funcionales como checklist de validación de release.

## 2. Implementación de criterios y validación

- [x] 2.1 Crear objetos de criterios (`OwnerQueryCriteria`, `PetQueryCriteria`, `VisitQueryCriteria`) y objeto común de paginación/ordenación.
- [x] 2.2 Implementar parser/normalizador de query params con defaults homogéneos en los tres endpoints.
- [x] 2.3 Implementar validaciones de query params (rangos de fechas, `size<=100`, `page>=0`, `sort` permitido, dirección `asc|desc`).
- [x] 2.4 Ajustar manejo de excepciones para devolver `400` con `ProblemDetail` sin filtrar detalles internos.

## 3. Implementación en servicio y repositorios por perfil

- [x] 3.1 Extender capa de servicio con operaciones de consulta avanzada para `owners`, `pets`, `visits`.
- [x] 3.2 Implementar filtros + paginación + ordenación en repositorios `jpa`.
- [x] 3.3 Implementar comportamiento equivalente en repositorios `jdbc`.
- [x] 3.4 Implementar comportamiento equivalente en repositorios `springdatajpa`.
- [x] 3.5 Garantizar orden estable con fallback por `id` en empates.

## 4. Integración REST y compatibilidad

- [x] 4.1 Adaptar `listOwners`, `listPets` y `listVisits` para usar criterios avanzados.
- [x] 4.2 Añadir cabeceras `X-Page-Number`, `X-Page-Size`, `X-Total-Elements`, `X-Total-Pages` en respuestas con resultados.
- [x] 4.3 Verificar compatibilidad hacia atrás para llamadas sin parámetros nuevos.

## 5. Validación funcional (criterios de aceptación)

- [x] 5.1 Crear pruebas de integración por endpoint para filtros individuales y combinados (AND).
- [x] 5.2 Crear pruebas de paginación (`page`, `size`, límites, defaults) y cabeceras de paginación.
- [x] 5.3 Crear pruebas de ordenación (asc/desc, whitelist, estabilidad con empate por `id`).
- [x] 5.4 Crear pruebas de errores `400` para parámetros inválidos y formato de `ProblemDetail`.

## 6. Validación no funcional (criterios de aceptación)

- [x] 6.1 Añadir suite de rendimiento automatizable que mida p95 por endpoint y valide objetivo `<=500 ms` en dataset >= 10k.
- [x] 6.2 Añadir pruebas de robustez ante entradas maliciosas en filtros y `sort` (sin `500`, sin fuga de detalles internos).
- [x] 6.3 Añadir pruebas de paridad de resultados entre perfiles `jpa`, `jdbc`, `springdatajpa` usando el mismo set de casos.
- [x] 6.4 Añadir reporte de validación final con evidencia funcional y no funcional para aceptación del cambio.

## 7. Documentación y cierre

- [x] 7.1 Actualizar documentación de API con ejemplos de consultas avanzadas y cabeceras esperadas.
- [x] 7.2 Documentar límites operativos (`size` máximo, campos ordenables, filtros permitidos por recurso).
- [x] 7.3 Registrar checklist de salida para `/opsx:apply` y criterios de done de implementación.

