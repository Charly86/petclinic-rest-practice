## ADDED Requirements

### Requirement: Owners list SHALL support validated combinable filters
The system SHALL allow filtering `/api/owners` by `lastName`, `firstName`, `city`, and `telephone`, combining provided filters with logical AND.

#### Scenario: Filter owners by multiple fields
- **WHEN** a client calls `/api/owners?lastName=Sm&city=Barcelona`
- **THEN** the response includes only owners matching both filters

#### Scenario: Filter owners by telephone
- **WHEN** a client calls `/api/owners?telephone=934123456`
- **THEN** the response includes only owners with that telephone value

### Requirement: Owners list SHALL apply a common pagination contract
The system SHALL support `page`, `size`, and `sort` on `/api/owners` with defaults `page=0`, `size=20`, `sort=id,asc`, and SHALL reject `size>100`.

#### Scenario: Apply default pagination on owners list
- **WHEN** a client calls `/api/owners` without pagination parameters
- **THEN** the first page is returned using default page size and default sort

#### Scenario: Return pagination headers for owners list
- **WHEN** a client calls `/api/owners?page=1&size=10`
- **THEN** the response includes `X-Page-Number`, `X-Page-Size`, `X-Total-Elements`, and `X-Total-Pages`

### Requirement: Owners list SHALL enforce sort whitelist and stable ordering
The system SHALL allow sorting `/api/owners` only by `id`, `lastName`, `firstName`, `city`, and SHALL apply `id` as deterministic tie-breaker.

#### Scenario: Sort owners by last name descending
- **WHEN** a client calls `/api/owners?sort=lastName,desc`
- **THEN** owners are sorted by `lastName` descending with stable order for ties

### Requirement: Owners query SHALL return 400 on invalid query parameters
The system MUST return `400 Bad Request` for invalid query parameters and MUST provide a `ProblemDetail` response.

#### Scenario: Reject unsupported owners sort field
- **WHEN** a client calls `/api/owners?sort=unknownField,asc`
- **THEN** the API responds `400` with a `ProblemDetail` describing the invalid sort field

#### Scenario: Reject invalid owners page size
- **WHEN** a client calls `/api/owners?size=500`
- **THEN** the API responds `400` with a `ProblemDetail` describing the size limit

### Requirement: Owners advanced querying SHALL meet non-functional acceptance criteria
The system MUST satisfy defined non-functional thresholds for owners advanced queries.

#### Scenario: Meet owners query latency objective
- **WHEN** the owners advanced-query test suite runs with dataset >= 10000 owners and requests using `size<=50`
- **THEN** p95 response time is <= 500 ms

#### Scenario: Resist malicious owners sort input
- **WHEN** a client calls `/api/owners?sort=lastName;drop table owners,asc`
- **THEN** the API responds `400` and the response contains no stack trace or SQL internals
