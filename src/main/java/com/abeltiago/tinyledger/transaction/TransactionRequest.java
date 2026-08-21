package com.abeltiago.tinyledger.transaction;

public record TransactionRequest(long id, TransactionType type, long amountCents) {
}
