# tiny-ledger

A tiny in-memory ledger API: record deposits and withdrawals, view the
current balance, view transaction history. Java 21 · Spring Boot · no
database, no UI, by design (see assumptions).

## Assumptions

- **Single account.** The assignment describes one ledger with no account
  concept in its API, so there is exactly one implicit account. Adding multiple
  accounts would be the first extension (`/accounts/{id}/...`).
- **Currency: implicit EUR.** Amounts are integer **cents** (`amountCents`),
  never floating point, cents are the smallest unit, so every amount is an
  exact integer. Having considered the name of the assignment, sub-cent
  precision would overcomplicate a tiny ledger.
- **Amounts must be strictly positive.** The transaction type carries the
  direction; a zero or negative amount is rejected.
- **No overdraft.** A withdrawal exceeding the current balance is rejected.
- **The append-only transaction history is the source of truth.** The balance
  is derived from it, never stored separately. A further extension would be
  to cache the balance if needed.
- **IDs and timestamps are server-generated** (UTC). Clients state intent,
  the ledger records the facts.
- **In-memory only.** Data lives for the lifetime of the process, per the
  assignment's own suggestion.

## How to run

Requires Java 21 or newer. The Maven wrapper handles Maven itself.

    ./mvnw spring-boot:run

The API starts on http://localhost:8080. Run the tests with `./mvnw test`.

## API

### Record a transaction

    curl -i localhost:8080/transactions -H "Content-Type: application/json" \
      -d '{"type":"DEPOSIT","amountCents":500}'

Returns 201 Created with the recorded fact:

    {"id":1,"type":"DEPOSIT","amountCents":500,"timestamp":"2026-08-19T09:12:31.286490Z"}

A zero or negative amount, or a missing type, returns 400 with a message.
A withdrawal above the current balance returns 409:

    {"message":"Transaction amount surpasses available balance."}

### View balance

    curl -i localhost:8080/balance

    {"balanceCents":500}

### View transaction history

    curl -i localhost:8080/transactions

Returns the full append-only history, oldest first.

## What was cut and why

- Authentication and monitoring: excluded by the assignment itself.
- Persistence: in memory by design.
  The service is the seam: swapping the list for a repository would leave
  the API untouched.
- Concurrency beyond coarse locking: every operation is synchronized, so
  each check and append is atomic.
  Real scale would need a per-account locking or a single writer queue.
- Idempotency: retrying a POST records a new transaction.
  Production would accept a client supplied idempotency key.
- Integration tests: the domain rules are unit tested, and every endpoint
  was exercised end to end by hand.
  A MockMvc happy path test would be the next test to write.

## A note on branches

`main` carries the extended version: multiple accounts, atomic transfers, and an
integration test at the HTTP seam. The `interview-extension` branch preserves,
unedited, the extension written live during a timed interview — kept as-is because
code written under a clock is its own kind of record.
