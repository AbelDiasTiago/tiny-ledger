package com.abeltiago.tinyledger.service;

import com.abeltiago.tinyledger.errors.InsufficientFundsException;
import com.abeltiago.tinyledger.errors.InvalidAmountException;
import com.abeltiago.tinyledger.transaction.Transaction;
import com.abeltiago.tinyledger.transaction.TransactionType;
import com.abeltiago.tinyledger.transaction.TransferRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class LedgerService {

    private final List<Transaction> transactionHistoryAccount1 = new ArrayList<>();
    private final List<Transaction> transactionHistoryAccount2 = new ArrayList<>();
    private long counter = 1;


    public synchronized Transaction deposit(long id, long amountCents) {
        checkAmount(amountCents);
        Transaction transaction = new Transaction(counter++, TransactionType.DEPOSIT, amountCents, Instant.now());
        switch ((int) id) {
            case 1 -> transactionHistoryAccount1.add(transaction);
            case 2 -> transactionHistoryAccount2.add(transaction);
            default -> throw new IllegalStateException("Unexpected value: " + (int) id);
        };
        return transaction;
    }

    public synchronized Transaction withdraw(long id, long amountCents) {
        checkAmount(amountCents);
        long balance = balanceCents(id);

        if (balance - amountCents < 0) {
            throw new InsufficientFundsException("Transaction amount surpasses available balance.");
        } else {
            Transaction transaction = new Transaction(counter++, TransactionType.WITHDRAWAL, amountCents, Instant.now());
            switch ((int) id) {
                case 1 -> transactionHistoryAccount1.add(transaction);
                case 2 -> transactionHistoryAccount2.add(transaction);
                default -> throw new IllegalStateException("Unexpected value: " + (int) id);
            };
            return transaction;
        }
    }

    public synchronized long balanceCents(long id) {
        long balanceCents = 0;

        List<Transaction> transactionHistory = getTransactions((int) id);
        for (Transaction transaction : transactionHistory) {

            switch (transaction.type()) {
                case DEPOSIT -> balanceCents += transaction.amountCents();
                case WITHDRAWAL -> balanceCents -= transaction.amountCents();
            }
        }

        return balanceCents;
    }

    public synchronized List<Transaction> history(long id) {
        List<Transaction> transactionHistory = getTransactions((int) id);
        return List.copyOf(transactionHistory);
    }

    private List<Transaction> getTransactions(int id) {
        List<Transaction> transactionHistory = switch (id) {
            case 1 -> transactionHistoryAccount1;
            case 2 -> transactionHistoryAccount2;
            default -> throw new IllegalStateException("Unexpected value: " + id);
        };
        return transactionHistory;
    }

    private static void checkAmount(long amountCents) {
        if (amountCents <= 0) {
            throw new InvalidAmountException("Transaction amount is zero or negative.");
        }
    }

    public synchronized void transfer(TransferRequest request) {
        Transaction withdraw = withdraw(1, 5000);
        transactionHistoryAccount1.add(withdraw);
        Transaction deposit = deposit(2, 500);
        transactionHistoryAccount2.add(deposit);
    }
}
