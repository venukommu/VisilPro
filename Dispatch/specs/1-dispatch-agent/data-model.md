# Data Model: Dispatch Agent

Entity: HealthPayload
- status: string (e.g., "UP" | "DOWN") — required
- reason: string — optional, short message explaining degraded state
- metadata: object — optional free-form key/value pairs (e.g., version, uptime)

Validation rules
- `status` MUST be present and one of a small set of values (UP, DOWN, STARTING, DEGRADED)
- `reason` if present SHOULD be short (under 240 characters)

State transitions (for monitoring)
- STARTING -> UP
- UP -> DEGRADED/STARTING/ DOWN
- DEGRADED -> UP or DOWN

Notes
- No persistent storage is required for this payload; responses are composed at runtime.
