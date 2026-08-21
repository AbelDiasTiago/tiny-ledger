package com.abeltiago.tinyledger.errors;

public class AccountNotFoundException extends RuntimeException{
    public AccountNotFoundException(long id, String message) {
        super(message);
    }
}
