# Advanced Querying Validation Report

Change: `advanced-api-queries-owners-pets-visits`

## Functional Validation

- Controller-level contract tests:
  - `OwnerRestControllerTests`
  - `PetRestControllerTests`
  - `VisitRestControllerTests`
- Service-level cross-profile tests:
  - `ClinicServiceJpaTests`
  - `ClinicServiceJdbcTests`
  - `ClinicServiceSpringDataJpaTests`

Coverage focus:
- Valid filters by endpoint.
- Combined filters (AND semantics).
- Pagination defaults and limits.
- Sorting behavior and deterministic fallback by `id`.
- `400` handling for invalid query parameters.

## Non-Functional Validation

- Performance suite:
  - `AdvancedQueryPerformanceTests`
  - Objective: p95 <= 500ms for `size<=50` with synthetic dataset `>=10k`.
- Robustness/security suite:
  - `QueryRequestParserTests`
  - Objective: reject malformed/malicious sort and invalid ranges with controlled `400` behavior.

## Suggested Execution Commands

```bash
mvn "-Denforcer.skip=true" -Dtest=OwnerRestControllerTests,PetRestControllerTests,VisitRestControllerTests test
mvn "-Denforcer.skip=true" -Dtest=ClinicServiceJpaTests,ClinicServiceJdbcTests,ClinicServiceSpringDataJpaTests test
mvn "-Denforcer.skip=true" -Dtest=QueryRequestParserTests,AdvancedQueryPerformanceTests test
```

## Execution Evidence

Executed on: `2026-04-10`

- `mvn -q "-Denforcer.skip=true" -Dtest=OwnerRestControllerTests,PetRestControllerTests,VisitRestControllerTests,QueryRequestParserTests,AdvancedQueryPerformanceTests,ClinicServiceJpaTests,ClinicServiceJdbcTests,ClinicServiceSpringDataJpaTests test`
  - Result: `PASS`
  - Notes:
    - Controller suites passed with pagination headers and `400` validation checks.
    - Cross-profile clinic service suites (`jpa`, `jdbc`, `spring-data-jpa`) passed.
    - Parser robustness and performance suites passed.

## Acceptance Summary Checklist

- [x] Functional acceptance criteria defined and mapped to automated tests.
- [x] Non-functional acceptance criteria defined and mapped to automated tests.
- [x] Performance objective expressed as measurable p95 threshold.
- [x] Robustness objective for malicious query input covered by tests.
- [x] Cross-profile parity validation path documented.
