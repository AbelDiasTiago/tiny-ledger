package com.abeltiago.tinyledger.service;

import com.abeltiago.tinyledger.errors.AccountNotFoundException;
import com.abeltiago.tinyledger.errors.InsufficientFundsException;
import com.abeltiago.tinyledger.errors.InvalidAmountException;
import com.abeltiago.tinyledger.errors.InvalidTransactionException;
import com.abeltiago.tinyledger.transaction.Transaction;
import com.abeltiago.tinyledger.transaction.TransactionType;
import com.abeltiago.tinyledger.transaction.TransferRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LedgerServiceTest {


    LedgerService service = new LedgerService();
    TransferRequest transferRequest = new TransferRequest(1, 2, 500);
    long amount = 500L;
    long minorAmount = 200L;
    long zeroAmount = 0L;
    long negativeAmount = -1L;
    long id = 1L;

    @DisplayName("Deposit should increase balance by respective amount")
    @Test
    void testDepositIncreaseAmount() {
        service.deposit(id, amount);

        assertEquals(amount, service.balanceCents(id));
    }

    @DisplayName("Balance should not throw exception when at zero")
    @Test
    void testBalanceZero() {
        service.deposit(id, amount);
        service.withdraw(id, amount);
        assertEquals(0, service.balanceCents(id));    }

    @DisplayName("Withdraw to zero does not throw exception")
    @Test
    void testWithdrawToZero(){
        service.deposit(id, amount);

        assertDoesNotThrow(()->service.withdraw(id, amount));
        assertEquals(zeroAmount, service.balanceCents(id));
    }

    @DisplayName("Overdraft throws InsufficientFundsException")
    @Test
    void testOverdraft(){
        service.deposit(id, minorAmount);
        assertThrows(InsufficientFundsException.class, ()-> service.withdraw(id, amount));
        assertEquals(1, service.history(id).size());
    }

    @DisplayName("Zero Or Negative Amounts throws InvalidAmountException")
    @Test
    void testZeroOrNegativeAmount(){
        assertThrows(InvalidAmountException.class, ()-> service.deposit(id, zeroAmount));
        assertThrows(InvalidAmountException.class, ()-> service.deposit(id, negativeAmount));
        assertThrows(InvalidAmountException.class, ()-> service.withdraw(id, zeroAmount));
        assertThrows(InvalidAmountException.class, ()-> service.withdraw(id, negativeAmount));
    }

    @DisplayName("Deposit, Withdraw, Deposit sequence has correct Balance and order")
    @Test
    void testSequenceBalance(){
        List<Transaction> historyOperationOrder = new ArrayList<>();
        historyOperationOrder.add(service.deposit(id, amount));
        assertEquals(amount, service.balanceCents(id));
        historyOperationOrder.add(service.withdraw(id, amount));
        assertEquals(zeroAmount, service.balanceCents(id));
        historyOperationOrder.add(service.deposit(id, amount));
        assertEquals(amount, service.balanceCents(id));
        assertEquals(3, service.history(id).size());
        List<Transaction> history = service.history(id);
        assertEquals(historyOperationOrder.get(0).type(), history.get(0).type());
        assertEquals(historyOperationOrder.get(1).type(), history.get(1).type());
        assertEquals(historyOperationOrder.get(2).type(), history.get(2).type());
    }

    @DisplayName("History returns a copy that can't be mutated")
    @Test
    void testHistoryCopy(){
        service.deposit(id, amount);
        List<Transaction> history = service.history(id);
        assertThrows(UnsupportedOperationException.class, () -> history.
            add(new Transaction(1, TransactionType.DEPOSIT, amount, Instant.now())));
    }
    @DisplayName("Ids should increment across transactions")
    @Test
    void testCounter(){
        Transaction deposit = service.deposit(id, amount);
        assertEquals(1, deposit.id());
        Transaction secondDeposit = service.deposit(id, amount);
        assertEquals(2, secondDeposit.id());
    }

    @DisplayName("Withdrwal on a account that does not exist should return AccountNotFoundException")
    @Test
    void withdrawalOnNonExistingAccount(){
        assertThrows(AccountNotFoundException.class, ()-> service.withdraw(999, 500));
    }

    @DisplayName("History on a account that does not exist should return AccountNotFoundException")
    @Test
    void historyOnNonExistingAccount(){
        assertThrows(AccountNotFoundException.class, ()-> service.history(999));
    }

    @DisplayName("Balance on a account that does not exist should return AccountNotFoundException")
    @Test
    void BalanceOnNonExistingAccount(){
        assertThrows(AccountNotFoundException.class, ()-> service.balanceCents(999));
    }

    @DisplayName("A non existing origin account from a transfer should return AccountNotFoundException")
    @Test
    void testIfInvalidOriginThrows(){
        assertThrows(AccountNotFoundException.class, () -> service.transfer(transferRequest));
    }

    @DisplayName("A non existing destination account from a transfer should return create one")
    @Test
    void testIfInvalidDestinationIsCreated() {
        service.deposit(1, amount);
        service.transfer(transferRequest);
        assertEquals(amount, service.balanceCents(2));
    }

    @DisplayName("A origin account with insufficient funds should throw InsufficientFundsException")
    @Test
    void testIfInsufficientFundsExceptionIsThrown() {
        service.deposit(1, minorAmount);
        service.deposit(2, minorAmount);
        assertThrows(InsufficientFundsException.class, ()->service.transfer(transferRequest));
        assertEquals(service.balanceCents(1), service.balanceCents(2));
        assertEquals(1, service.history(1).size());
        assertEquals(1, service.history(2).size());
    }

    @DisplayName("A origin account with sufficient funds creates a new destination account")
    @Test
    void testIfSufficientFundsCreatesNewAccount() {
        service.deposit(1, amount);
        service.deposit(2, minorAmount);
        service.transfer(transferRequest);
        assertEquals(700, service.balanceCents(2));
    }

}