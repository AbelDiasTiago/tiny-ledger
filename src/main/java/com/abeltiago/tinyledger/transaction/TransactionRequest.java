package com.abeltiago.tinyledger.transaction;

public record TransactionRequest(TransactionType type, long amountCents) {
}
