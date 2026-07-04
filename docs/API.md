# aerolane API reference

Base URL: `http://localhost:8080/api/v1`
Auth: HTTP Basic on every request. Interactive version at `/swagger-ui.html`.

Status code semantics used across the API:

- `401` — no credentials or wrong credentials
- `403` — authenticated, but your role isn't allowed to do this
- `404` — resource doesn't exist (body explains which)
- `400` — validation failure (`fieldErrors` map) or malformed body/enum value

## Lanes

`GET /lanes` — all lanes ordered by terminal then name.

`GET /lanes/{id}` — one lane.

`PATCH /lanes/{id}` — supervisor only. Body:

```json
{ "status": "OPEN" }
```

`status` is one of `OPEN`, `CLOSED`, `MAINTENANCE`.

## Inspections

`GET /inspections` — newest first. Optional filters: `?result=PASS|FAIL`, `?laneId={id}`.

`GET /inspections/{id}` — one inspection.

`POST /inspections` — officer or supervisor. Body:

```json
{
  "laneId": 1,
  "equipment": "XRAY_SCANNER",
  "result": "FAIL",
  "notes": "Belt misalignment, error code E-114."
}
```

`equipment` is one of `XRAY_SCANNER`, `METAL_DETECTOR`, `BODY_SCANNER`,
`EXPLOSIVE_TRACE_DETECTOR`, `BAGGAGE_CONVEYOR`. `notes` is optional, max 500 chars.
Returns `201` with a `Location` header and the created record. The authenticated
username is recorded as `inspectedBy` — the client can't spoof it.

## Reports

`GET /reports/summary` — auditor or supervisor only.

```json
{
  "totalInspections": 18,
  "passed": 14,
  "failed": 4,
  "failRatePercent": 22.22,
  "failuresByEquipment": { "XRAY_SCANNER": 2, "BAGGAGE_CONVEYOR": 2 }
}
```

## Error shape

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2026-07-04T14:03:22.118",
  "fieldErrors": { "laneId": "laneId is required" }
}
```

`fieldErrors` only appears on validation failures.
