package com.abeltiago.tinyledger.controller;


import com.abeltiago.tinyledger.errors.InvalidTransactionException;
import com.abeltiago.tinyledger.service.LedgerService;
import com.abeltiago.tinyledger.transaction.BalanceResponse;
import com.abeltiago.tinyledger.transaction.Transaction;
import com.abeltiago.tinyledger.transaction.TransactionRequest;
import com.abeltiago.tinyledger.transaction.TransferRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TransactionController {

    private final LedgerService service;

    public TransactionController(LedgerService service) {
        this.service = service;
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> transaction(@RequestBody TransactionRequest request) {
        if (request.type() == null) {
            throw new InvalidTransactionException("TransactionRequest type must not be null");
        }
        Transaction returnedTransaction = switch (request.type()) {
            case DEPOSIT -> service.deposit(request.id(), request.amountCents());
            case WITHDRAWAL -> service.withdraw(request.id(), request.amountCents());
        };

        return ResponseEntity.status(HttpStatus.CREATED).body(returnedTransaction);

    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<List<Transaction>> history(long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.history(id));
    }

    @GetMapping("/balance/{id}")
    public ResponseEntity<BalanceResponse> balance(long id) {
        return ResponseEntity.status(HttpStatus.OK).body(new BalanceResponse(service.balanceCents(id)));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transaction(@RequestBody TransferRequest request) {

        service.transfer(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

}
