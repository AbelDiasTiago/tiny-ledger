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

*Run instructions and API examples will land with the endpoints.*
