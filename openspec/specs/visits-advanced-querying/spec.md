## ADDED Requirements

### Requirement: Visits list SHALL support validated combinable filters
The system SHALL allow filtering `/api/visits` by `petId`, `dateFrom`, `dateTo`, and `descriptionContains`, combining provided filters with logical AND.

#### Scenario: Filter visits by pet and date range
- **WHEN** a client calls `/api/visits?petId=7&dateFrom=2026-01-01&dateTo=2026-01-31`
- **THEN** the response includes only visits for pet 7 in the inclusive date range

#### Scenario: Filter visits by description content
- **WHEN** a client calls `/api/visits?descriptionContains=checkup`
- **THEN** the response includes only visits whose description matches that filter semantics

### Requirement: Visits list SHALL apply a common pagination contract
The system SHALL support `page`, `size`, and `sort` on `/api/visits` with defaults `page=0`, `size=20`, `sort=id,asc`, and SHALL reject `size>100`.

#### Scenario: Apply default pagination on visits list
- **WHEN** a client calls `/api/visits` without pagination parameters
- **THEN** the first page is returned using default page size and default sort

#### Scenario: Return pagination headers for visits list
- **WHEN** a client calls `/api/visits?page=2&size=5`
- **THEN** the response includes `X-Page-Number`, `X-Page-Size`, `X-Total-Elements`, and `X-Total-Pages`

### Requirement: Visits list SHALL enforce sort whitelist and stable ordering
The system SHALL allow sorting `/api/visits` only by `id`, `date`, `petId`, and SHALL apply `id` as deterministic tie-breaker.

#### Scenario: Sort visits by date descending
- **WHEN** a client calls `/api/visits?sort=date,desc`
- **THEN** visits are sorted by `date` descending with stable order for ties

### Requirement: Visits query SHALL return 400 on invalid query parameters
The system MUST return `400 Bad Request` for invalid query parameters and MUST provide a `ProblemDetail` response.

#### Scenario: Reject invalid visits date range
- **WHEN** a client calls `/api/visits?dateFrom=2026-02-10&dateTo=2026-01-01`
- **THEN** the API responds `400` with a `ProblemDetail` describing the invalid range

#### Scenario: Reject unsupported visits sort field
- **WHEN** a client calls `/api/visits?sort=description,asc`
- **THEN** the API responds `400` with a `ProblemDetail` describing the invalid sort field

### Requirement: Visits advanced querying SHALL meet non-functional acceptance criteria
The system MUST satisfy defined non-functional thresholds for visits advanced queries.

#### Scenario: Meet visits query latency objective
- **WHEN** the visits advanced-query test suite runs with dataset >= 10000 visits and requests using `size<=50`
- **THEN** p95 response time is <= 500 ms

#### Scenario: Resist malicious visits sort input
- **WHEN** a client calls `/api/visits?sort=date desc;select * from users,asc`
- **THEN** the API responds `400` and does not leak SQL/internal implementation details
