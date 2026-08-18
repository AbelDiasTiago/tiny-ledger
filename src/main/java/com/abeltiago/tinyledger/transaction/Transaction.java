package com.abeltiago.tinyledger.transaction;

import java.time.Instant;

public record Transaction(long id, TransactionType type, long amountCents, Instant timestamp) {
}
