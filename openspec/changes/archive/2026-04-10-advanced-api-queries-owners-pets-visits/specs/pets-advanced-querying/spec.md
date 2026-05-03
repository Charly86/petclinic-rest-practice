## ADDED Requirements

### Requirement: Pets list SHALL support validated combinable domain filters
The system SHALL allow filtering `/api/pets` by `name`, `typeId`, `ownerId`, `birthDateFrom`, and `birthDateTo`, combining provided filters with logical AND.

#### Scenario: Filter pets by owner and type
- **WHEN** a client calls `/api/pets?ownerId=12&typeId=3`
- **THEN** the response includes only pets matching both criteria

#### Scenario: Filter pets by birth date range
- **WHEN** a client calls `/api/pets?birthDateFrom=2024-01-01&birthDateTo=2024-12-31`
- **THEN** the response includes only pets whose birth date is within the inclusive range

### Requirement: Pets list SHALL apply a common pagination contract
The system SHALL support `page`, `size`, and `sort` on `/api/pets` with defaults `page=0`, `size=20`, `sort=id,asc`, and SHALL reject `size>100`.

#### Scenario: Apply default pagination on pets list
- **WHEN** a client calls `/api/pets` without pagination parameters
- **THEN** the first page is returned using default page size and default sort

#### Scenario: Return pagination headers for pets list
- **WHEN** a client calls `/api/pets?page=0&size=25`
- **THEN** the response includes `X-Page-Number`, `X-Page-Size`, `X-Total-Elements`, and `X-Total-Pages`

### Requirement: Pets list SHALL enforce sort whitelist and stable ordering
The system SHALL allow sorting `/api/pets` only by `id`, `name`, `birthDate`, `typeId`, `ownerId`, and SHALL apply `id` as deterministic tie-breaker.

#### Scenario: Sort pets by name ascending
- **WHEN** a client calls `/api/pets?sort=name,asc`
- **THEN** pets are sorted by `name` ascending with stable order for ties

### Requirement: Pets query SHALL return 400 on invalid query parameters
The system MUST return `400 Bad Request` for invalid query parameters and MUST provide a `ProblemDetail` response.

#### Scenario: Reject invalid birth date range
- **WHEN** a client calls `/api/pets?birthDateFrom=2024-12-31&birthDateTo=2024-01-01`
- **THEN** the API responds `400` with a `ProblemDetail` describing the invalid range

#### Scenario: Reject unsupported pets sort field
- **WHEN** a client calls `/api/pets?sort=color,asc`
- **THEN** the API responds `400` with a `ProblemDetail` describing the invalid sort field

### Requirement: Pets advanced querying SHALL meet non-functional acceptance criteria
The system MUST satisfy defined non-functional thresholds for pets advanced queries.

#### Scenario: Meet pets query latency objective
- **WHEN** the pets advanced-query test suite runs with dataset >= 10000 pets and requests using `size<=50`
- **THEN** p95 response time is <= 500 ms

#### Scenario: Resist malicious pets filter input
- **WHEN** a client calls `/api/pets?name=' or '1'='1`
- **THEN** the API responds with a valid business response or `400`, and never with `500`
