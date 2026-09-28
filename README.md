# FraudWatch - Basic Transaction Anomaly Flagging System

Spring Boot / Java 17 / Spring Data JPA / MySQL.
Packages: `controller`, `entity`, `service`, `repository`, `exception`. Frontend: `static/index.html` + `static/style.css`.

## Run
Set your MySQL username and password in `src/main/resources/application.properties`, then:
```
mvn spring-boot:run
```
Open http://localhost:8080/ for the UI.

Two rules are seeded on first start: *High Amount* (amount > 10000) and *Rapid Transfers* (more than 3 from one sender in 60 s).

## API
| Feature | Endpoint |
|---|---|
| Record transaction (rules evaluated) | `POST /api/transactions` -> 201 |
| List / get transactions | `GET /api/transactions[/{id}]` |
| Attempt to complete a transaction | `POST /api/transactions/{id}/complete` |
| Create / update / list rules | `POST /api/rules`, `PUT /api/rules/{id}`, `GET /api/rules` |
| Review queue | `GET /api/flagged?status=PENDING_REVIEW&page=0&size=20` |
| Approve / block | `POST /api/flagged/{id}/review` |
| Dashboard by rule | `GET /api/dashboard/flagged-by-rule` |

## Business rules (service layer)
- All active rules are evaluated before the transaction is saved; every triggered rule is recorded on one `FlaggedTransaction`.
- Flagged transactions are held (`FLAGGED`) and cannot complete until approved.
- `BLOCKED` transactions can never be completed (409) or re-reviewed.
- A flagged item can be reviewed only once (409 on a second attempt).
- Errors return JSON `{status, error, message, details, timestamp}`, never stack traces.
