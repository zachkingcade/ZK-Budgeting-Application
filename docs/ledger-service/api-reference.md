# Ledger service API reference

**Purpose**: Explain how to talk to `ledger-service` over HTTP and what conventions it follows.
<br>
Last updated: 2026-04-22

## Canonical sources

This page explains the API at a human level. For exact routes and DTO shapes, refer to:

- Service guide: [`../guide-ledger-service.md`](../guide-ledger-service.md)
- Service standards and envelopes: [`../standards/microservice-springboot-consistency.md`](../standards/microservice-springboot-consistency.md)

## Base characteristics

- **Style**: JSON over HTTP (Spring MVC controllers).
- **URI pattern**: resource-based with explicit operation segments (`/all`, `/byid/{id}`, `/add`, `/update`).
- **DTO style**: Java `record` request/response types under `adapter/in/web/dto`.

## Envelope conventions

Success responses follow a consistent envelope:

```json
{
  "statusMessage": "Returned [10] Accounts",
  "metaData": {
    "requestDate": "2026-03-31",
    "requestTime": "12:34:56.789",
    "executionTimeMs": 12,
    "dataResponseCount": 10
  },
  "data": {}
}
```

Errors use a consistent shape (see [error-handling](./error-handling.md)).

## Endpoints

The primary resource groups are:

### Accounts

| Method | Path | Summary | Request DTO | Response DTO | Status |
|---|---|---|---|---|---|
| GET | `/accounts/all` | List all accounts | — | [`GetAllAccountsResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/account/GetAllAccountsResponse.java) | 200 |
| POST | `/accounts/all/filtered` | List accounts (sorted/filtered) | [`GetAllAccountsRequest`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/account/GetAllAccountsRequest.java) | [`GetAllAccountsResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/account/GetAllAccountsResponse.java) | 200 |
| GET | `/accounts/byid/{id}` | Get account by id | — | [`GetAccountByIdResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/account/GetAccountByIdResponse.java) | 200 |
| POST | `/accounts/add` | Create account | [`CreateAccountRequest`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/account/CreateAccountRequest.java) | [`CreateAccountResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/account/CreateAccountResponse.java) | 201 |
| POST | `/accounts/update` | Update account | [`UpdateAccountRequest`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/account/UpdateAccountRequest.java) | [`UpdateAccountResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/account/UpdateAccountResponse.java) | 200 |

### Account types

| Method | Path | Summary | Request DTO | Response DTO | Status |
|---|---|---|---|---|---|
| GET | `/accounttypes/all` | List all account types | — | [`GetAllAccountTypesResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/accounttype/GetAllAccountTypesResponse.java) | 200 |
| POST | `/accounttypes/all/filtered` | List account types (sorted/filtered) | [`GetAllAccountTypesRequest`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/accounttype/GetAllAccountTypesRequest.java) | [`GetAllAccountTypesResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/accounttype/GetAllAccountTypesResponse.java) | 200 |
| GET | `/accounttypes/byid/{id}` | Get account type by id | — | [`GetAccountTypeByIdResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/accounttype/GetAccountTypeByIdResponse.java) | 200 |
| POST | `/accounttypes/add` | Create account type | [`CreateAccountTypeRequest`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/accounttype/CreateAccountTypeRequest.java) | [`CreateAccountTypeResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/accounttype/CreateAccountTypeResponse.java) | 201 |
| POST | `/accounttypes/update` | Update account type | [`UpdateAccountTypeRequest`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/accounttype/UpdateAccountTypeRequest.java) | [`UpdateAccountTypeResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/accounttype/UpdateAccountTypeResponse.java) | 200 |

### Account classifications

| Method | Path | Summary | Request DTO | Response DTO | Status |
|---|---|---|---|---|---|
| GET | `/accountclassifications/all` | List all classifications | — | [`GetAllAccountClassificationResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/accountclassification/GetAllAccountClassificationResponse.java) | 200 |
| GET | `/accountclassifications/byid/{id}` | Get classification by id | — | [`GetByIdAccountClassificationResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/accountclassification/GetByIdAccountClassificationResponse.java) | 200 |

### Journal entries

| Method | Path | Summary | Request DTO | Response DTO | Status |
|---|---|---|---|---|---|
| GET | `/journalentry/all` | List all journal entries | — | [`GetAllJournalEntryResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/journal/GetAllJournalEntryResponse.java) | 200 |
| POST | `/journalentry/all/filtered` | List journal entries (sorted/filtered) | [`GetAllJournalEntryRequest`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/journal/GetAllJournalEntryRequest.java) | [`GetAllJournalEntryResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/journal/GetAllJournalEntryResponse.java) | 200 |
| GET | `/journalentry/byid/{id}` | Get journal entry by id | — | [`GetByIdJournalEntryResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/journal/GetByIdJournalEntryResponse.java) | 200 |
| POST | `/journalentry/add` | Create journal entry | [`CreateJournalEntryRequest`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/journal/CreateJournalEntryRequest.java) | [`CreateJournalEntryResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/journal/CreateJournalEntryResponse.java) | 201 |
| POST | `/journalentry/update` | Update journal entry | [`UpdateJournalEntryRequest`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/journal/UpdateJournalEntryRequest.java) | [`UpdateJournalEntryResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/journal/UpdateJournalEntryResponse.java) | 200 |
| DELETE | `/journalentry/remove/{id}` | Remove journal entry by id | — | [`RemoveJournalEntryDTOResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/journal/RemoveJournalEntryDTOResponse.java) | 200 |

### Pending transactions

| Method | Path | Summary | Request DTO | Response DTO | Status |
|---|---|---|---|---|---|
| GET | `/pendingtransactions/all` | List pending transactions | — | [`GetAllPendingTransactionsResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/pendingtransaction/GetAllPendingTransactionsResponse.java) | 200 |
| DELETE | `/pendingtransactions/remove/{transactionNumber}` | Remove pending transaction | — | [`RemovePendingTransactionResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/pendingtransaction/RemovePendingTransactionResponse.java) | 200 |
| POST | `/pendingtransactions/import` | Import pending transactions (file upload) | multipart form (`formatId`, `file`) | [`ImportPendingTransactionsResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/pendingtransaction/ImportPendingTransactionsResponse.java) | 201 |
| POST | `/pendingtransactions/apply` | Apply pending transactions to journal entries | [`ApplyPendingTransactionsRequest`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/pendingtransaction/apply/ApplyPendingTransactionsRequest.java) | [`ApplyPendingTransactionsResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/pendingtransaction/apply/ApplyPendingTransactionsResponse.java) | 200 |

### Import formats

| Method | Path | Summary | Request DTO | Response DTO | Status |
|---|---|---|---|---|---|
| GET | `/importformats/all` | List active import formats | — | [`GetAllImportFormatsResponse`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/importformat/GetAllImportFormatsResponse.java) | 200 |

## Status codes

- Reads and updates typically return `200 OK`.
- Creates typically return `201 CREATED`.

