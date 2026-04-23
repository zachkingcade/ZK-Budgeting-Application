# Reporting service API reference

**Purpose**: Explain how to talk to `reporting-service` over HTTP and what conventions it follows.
<br>
Last updated: 2026-04-22

## Canonical sources

This page explains the API at a human level. For cross-service envelopes and conventions, refer to:

- Service guide: [`../guide-reporting-service.md`](../guide-reporting-service.md)
- Microservice conventions: [`../standards/microservice-springboot-consistency.md`](../standards/microservice-springboot-consistency.md)

## Base URL

- `http://localhost:8083`

## Envelope conventions

Most JSON responses use an `ApiResponse<T>` envelope:

```json
{
  "statusMessage": "Human readable status",
  "metaData": {
    "requestDate": "YYYY-MM-DD",
    "requestTime": "HH:mm:ss.SSS",
    "executionTimeMs": 12,
    "dataResponseCount": 1
  },
  "data": {}
}
```

Errors return `ApiErrorResponse`:

```json
{
  "errorCode": "BAD_REQUEST",
  "message": "Human readable message"
}
```

## Endpoints

All routes are under the `/reports` prefix.

### Reports

| Method | Path | Summary | Request DTO | Response DTO | Status |
|---|---|---|---|---|---|
| GET | `/reports/catalog` | Get report catalog | — | [`CatalogReportDto[]`](../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/web/dto/catalog/CatalogReportDto.java) | 200 |
| POST | `/reports/requests` | Queue a report job | [`ReportRequestDto`](../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/web/dto/ReportRequestDto.java) | [`ReportJobMetadataDto`](../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/web/dto/ReportJobMetadataDto.java) | 202 |
| GET | `/reports` | List report jobs for user | — | [`ReportJobMetadataDto[]`](../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/web/dto/ReportJobMetadataDto.java) | 200 |
| GET | `/reports/{id}` | Get report job metadata | — | [`ReportJobMetadataDto`](../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/web/dto/ReportJobMetadataDto.java) | 200/404 |
| GET | `/reports/{id}/download` | Download completed PDF | — | `application/pdf` bytes | 200/404 |

