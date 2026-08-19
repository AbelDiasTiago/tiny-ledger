package com.abeltiago.tinyledger.service;

import com.abeltiago.tinyledger.errors.InsufficientFundsException;
import com.abeltiago.tinyledger.errors.InvalidAmountException;
import com.abeltiago.tinyledger.transaction.Transaction;
import com.abeltiago.tinyledger.transaction.TransactionType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class LedgerService {

    private final List<Transaction> transactionHistory = new ArrayList<>();
    private long counter = 1;


    public synchronized Transaction deposit(long amountCents) {
        checkAmount(amountCents);
        Transaction transaction = new Transaction(counter++, TransactionType.DEPOSIT, amountCents, Instant.now());
        transactionHistory.add(transaction);
        return transaction;
    }

    public synchronized Transaction withdraw(long amountCents) {
        checkAmount(amountCents);
        long balance = balanceCents();

        if (balance - amountCents < 0) {
            throw new InsufficientFundsException("Transaction amount surpasses available balance.");
        } else {
            Transaction transaction = new Transaction(counter++, TransactionType.WITHDRAWAL, amountCents, Instant.now());
            transactionHistory.add(transaction);
            return transaction;
        }
    }

    public synchronized long balanceCents() {
        long balanceCents = 0;

        for (Transaction transaction : transactionHistory) {

            switch (transaction.type()) {
                case DEPOSIT -> balanceCents += transaction.amountCents();
                case WITHDRAWAL -> balanceCents -= transaction.amountCents();
            }
        }

        return balanceCents;
    }

    public synchronized List<Transaction> history() {
        return List.copyOf(transactionHistory);
    }

    private static void checkAmount(long amountCents) {
        if (amountCents <= 0) {
            throw new InvalidAmountException("Transaction amount is zero or negative.");
        }
    }
}
