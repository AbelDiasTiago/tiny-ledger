package com.abeltiago.tinyledger.transaction;

public record TransferRequest(long origin, long destination, long amountCents) {
}
