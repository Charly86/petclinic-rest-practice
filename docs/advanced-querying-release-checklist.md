# Advanced Querying Release Checklist

## Done Criteria

- [ ] OpenAPI updated for `GET /api/owners`, `GET /api/pets`, `GET /api/visits`.
- [ ] Generated API interfaces compile with updated endpoint signatures.
- [ ] Controllers implement filters + pagination + sorting and set pagination headers.
- [ ] Query parameter validation returns `400` with `ProblemDetail`.
- [ ] Service layer implements advanced query operations for owners/pets/visits.
- [ ] Existing CRUD behavior remains compatible when new query params are omitted.
- [ ] Controller tests pass.
- [ ] Cross-profile clinic service tests pass (`jpa`, `jdbc`, `spring-data-jpa`).
- [ ] Parser robustness tests pass (invalid/malicious inputs).
- [ ] Performance suite runs and reports p95 objective.

## /opsx:apply Exit Checklist

- [ ] Mark implementation tasks complete in `openspec/changes/advanced-api-queries-owners-pets-visits/tasks.md`.
- [ ] Run full validation command set.
- [ ] Update `docs/advanced-querying-validation-report.md` with final execution evidence.
- [ ] Prepare change for archive when all tasks are complete.
