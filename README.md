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

## Post-interview changes

The transfer feature was written live during the Teya coding interview, under a constraint
of exactly two accounts. It is kept as it was written.

Everything done afterwards had a single purpose: **to make the project compile, run, and
pass its existing tests.** Nothing was redesigned, refactored or tidied.

- **`TransactionController`** — the two GET handlers were left mid-refactor when time ran
  out. They still called `history()` and `balanceCents()` with no arguments after
  `LedgerService` had gained an account id parameter, so `src/main` did not compile. The
  call sites were completed and the two mappings given an `/{id}` segment.
- **`LedgerServiceTest`** — every call was adapted to the new id-bearing signatures by
  passing an account id through. Mechanical only: no assertion was changed, so the tests
  still assert exactly the behaviour they asserted before.

**No tests were added.** The suite is the interview-era suite adapted to compile — nine
tests, all green. It exercises `LedgerService` on account 1 only; the transfer path and
the HTTP layer are not covered.

### Known problems, deliberately left unfixed

Fixing these would mean rewriting work the interviewers watched being written, so they
stay as they are. They are listed here rather than quietly corrected:

- **`transfer` ignores its request.** `origin`, `destination` and `amountCents` are unused
  and the amounts are hardcoded; the two legs do not match each other; and each leg is
  appended to its history a second time on top of the append its helper already does. A
  transfer therefore moves the wrong amounts and records duplicate entries.
- **The id on the two GET endpoints is not bound as a path variable.** It carries no
  `@PathVariable`, so Spring resolves it from the query string instead: `/balance/1`
  fails, `/balance/1?id=1` succeeds.
- **An unknown account id returns 500.** `getTransactions` throws `IllegalStateException`,
  which has no handler in `ApiExceptionHandler`, so it escapes as an unmapped server error
  rather than a 400.
- **Account ids are narrowed to `int`** before the switch that selects a history list, so a
  `long` id whose low 32 bits are 1 or 2 selects an account instead of being rejected.

The `## API` section above documents the original single-account endpoints and has not
been updated to match the two-account signatures.
