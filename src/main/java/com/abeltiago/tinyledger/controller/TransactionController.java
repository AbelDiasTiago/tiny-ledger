package com.abeltiago.tinyledger.controller;


import com.abeltiago.tinyledger.errors.InvalidTransactionException;
import com.abeltiago.tinyledger.service.LedgerService;
import com.abeltiago.tinyledger.transaction.BalanceResponse;
import com.abeltiago.tinyledger.transaction.Transaction;
import com.abeltiago.tinyledger.transaction.TransactionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TransactionController {

    private final LedgerService service;

    public TransactionController(LedgerService service) {
        this.service = service;
    }

    @PostMapping("/accounts/{id}/transactions")
    public ResponseEntity<Transaction> transaction(@PathVariable long id, @RequestBody TransactionRequest request) {
        if (request.type() == null) {
            throw new InvalidTransactionException("TransactionRequest type must not be null");
        }
        Transaction returnedTransaction = switch (request.type()) {
            case DEPOSIT -> service.deposit(id, request.amountCents());
            case WITHDRAWAL -> service.withdraw(id, request.amountCents());
        };

        return ResponseEntity.status(HttpStatus.CREATED).body(returnedTransaction);

    }

    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<List<Transaction>> history(@PathVariable long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.history(id));
    }

    @GetMapping("/accounts/{id}/balance")
    public ResponseEntity<BalanceResponse> balance(@PathVariable long id) {
        return ResponseEntity.status(HttpStatus.OK).body(new BalanceResponse(service.balanceCents(id)));
    }

}
