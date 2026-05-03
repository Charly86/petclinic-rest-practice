# Advanced Querying for Owners, Pets, and Visits

This document describes the advanced querying contract for:
- `GET /api/owners`
- `GET /api/pets`
- `GET /api/visits`

## Common Query Contract

- `page`:
  - Zero-based page index.
  - Default: `0`.
- `size`:
  - Page size.
  - Default: `20`.
  - Allowed range: `1..100`.
- `sort`:
  - Format: `field,direction`.
  - Direction: `asc` or `desc`.
  - Default: `id,asc`.
  - Sorting is deterministic with fallback by `id`.

## Pagination Response Headers

Successful list responses include:
- `X-Page-Number`
- `X-Page-Size`
- `X-Total-Elements`
- `X-Total-Pages`

## Resource-Specific Filters

### Owners (`GET /api/owners`)
- `lastName` (prefix)
- `firstName` (prefix)
- `city` (prefix)
- `telephone` (exact)

Allowed sort fields:
- `id`, `lastName`, `firstName`, `city`

### Pets (`GET /api/pets`)
- `name` (contains, case-insensitive)
- `typeId` (exact)
- `ownerId` (exact)
- `birthDateFrom` (inclusive)
- `birthDateTo` (inclusive)

Allowed sort fields:
- `id`, `name`, `birthDate`, `typeId`, `ownerId`

### Visits (`GET /api/visits`)
- `petId` (exact)
- `dateFrom` (inclusive)
- `dateTo` (inclusive)
- `descriptionContains` (contains, case-insensitive)

Allowed sort fields:
- `id`, `date`, `petId`

## Error Behavior

Invalid query parameters return `400 Bad Request` with `ProblemDetail`.

Examples:
- Unsupported sort field: `sort=unknown,asc`
- Unsupported sort direction: `sort=id,descending`
- Invalid range: `birthDateFrom > birthDateTo` or `dateFrom > dateTo`
- Invalid size: `size=0` or `size>100`

## Examples

Owners:

```http
GET /api/owners?lastName=D&page=0&size=10&sort=lastName,desc
```

Pets:

```http
GET /api/pets?ownerId=12&typeId=3&birthDateFrom=2024-01-01&birthDateTo=2024-12-31&sort=name,asc
```

Visits:

```http
GET /api/visits?petId=7&dateFrom=2026-01-01&dateTo=2026-01-31&sort=date,desc
```

## Acceptance Validation Checklist

Functional:
- [ ] Filters can be combined (AND) in each endpoint.
- [ ] `page`, `size`, and `sort` work with defaults and limits.
- [ ] Invalid parameters return `400` with `ProblemDetail`.
- [ ] Same query and same data state return deterministic order.

Non-functional:
- [ ] p95 latency <= 500ms for `size<=50` in 10k dataset benchmark suite.
- [ ] Malicious filter/sort payloads do not return `500`.
- [ ] No stack traces or SQL internals are exposed in API error bodies.
- [ ] Query behavior is consistent across `jpa`, `jdbc`, and `spring-data-jpa` profiles.
