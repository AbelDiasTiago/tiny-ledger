package com.abeltiago.tinyledger.errors;

public class InvalidAmountException  extends RuntimeException{
    public InvalidAmountException(String message) {
        super(message);
    }
}
