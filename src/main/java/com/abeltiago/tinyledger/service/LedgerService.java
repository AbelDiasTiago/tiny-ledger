package com.abeltiago.tinyledger.service;

import com.abeltiago.tinyledger.errors.AccountNotFoundException;
import com.abeltiago.tinyledger.errors.InsufficientFundsException;
import com.abeltiago.tinyledger.errors.InvalidAmountException;
import com.abeltiago.tinyledger.transaction.Transaction;
import com.abeltiago.tinyledger.transaction.TransactionType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LedgerService {

    private final ConcurrentMap<Long,List<Transaction>> transactionHistory = new ConcurrentHashMap<>();
    private long counter = 1;


    public synchronized Transaction deposit(long id, long amountCents) {
        checkAmount(amountCents);
        List<Transaction> transactionList = transactionHistory.computeIfAbsent(id, k -> new ArrayList<>());
        Transaction transaction = new Transaction(counter++, TransactionType.DEPOSIT, amountCents, Instant.now());
        transactionList.add(transaction);
        return transaction;
    }

    public synchronized Transaction withdraw(long id, long amountCents) {
        checkAmount(amountCents);
        List<Transaction> ledger = getLedgerOrThrow(id);

        long balance = balanceCents(id);

        if (balance - amountCents < 0) {
            throw new InsufficientFundsException("Transaction amount surpasses available balance.");
        } else {
            Transaction transaction = new Transaction(counter++, TransactionType.WITHDRAWAL, amountCents, Instant.now());
            ledger.add(transaction);
            return transaction;
        }
    }

    public synchronized long balanceCents(long id) {
        long balanceCents = 0;
        List<Transaction> ledger = getLedgerOrThrow(id);

        for (Transaction transaction : ledger) {

            switch (transaction.type()) {
                case DEPOSIT -> balanceCents += transaction.amountCents();
                case WITHDRAWAL -> balanceCents -= transaction.amountCents();
            }
        }

        return balanceCents;
    }

    public synchronized List<Transaction> history(long id) {
        return List.copyOf(getLedgerOrThrow(id));
    }

    private static void checkAmount(long amountCents) {
        if (amountCents <= 0) {
            throw new InvalidAmountException("Transaction amount is zero or negative.");
        }
    }

    private List<Transaction> getLedgerOrThrow(long id) {
        List<Transaction> transactionList = transactionHistory.get(id);
        if(transactionList == null){
            throw new AccountNotFoundException(id, "Account with id " + id + " does not exist");
        }
        return transactionList;
    }
}
