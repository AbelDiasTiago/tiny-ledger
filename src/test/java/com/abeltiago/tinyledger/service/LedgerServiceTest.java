package com.abeltiago.tinyledger.service;

import com.abeltiago.tinyledger.errors.InsufficientFundsException;
import com.abeltiago.tinyledger.errors.InvalidAmountException;
import com.abeltiago.tinyledger.transaction.Transaction;
import com.abeltiago.tinyledger.transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LedgerServiceTest {


    LedgerService service = new LedgerService();
    long amount = 500L;
    long zeroAmount = 0L;
    long negativeAmount = -1L;

    @DisplayName("Deposit should increase balance by respective amount")
    @Test
    void testDepositIncreaseAmount() {
        service.deposit(amount);

        assertEquals(amount, service.balanceCents());
    }

    @DisplayName("Balance should not throw exception when at zero")
    @Test
    void testBalanceZero() {
        assertEquals(0, service.balanceCents());    }

    @DisplayName("Withdraw to zero does not throw exception")
    @Test
    void testWithdrawToZero(){
        service.deposit(amount);

        assertDoesNotThrow(()->service.withdraw(amount));
        assertEquals(zeroAmount, service.balanceCents());
    }

    @DisplayName("Overdraft throws InsufficientFundsException")
    @Test
    void testOverdraft(){
        assertThrows(InsufficientFundsException.class, ()-> service.withdraw(amount));
        assertEquals(0, service.history().size());
    }

    @DisplayName("Zero Or Negative Amounts throws InvalidAmountException")
    @Test
    void testZeroOrNegativeAmount(){
        assertThrows(InvalidAmountException.class, ()-> service.deposit(zeroAmount));
        assertThrows(InvalidAmountException.class, ()-> service.deposit(negativeAmount));
        assertThrows(InvalidAmountException.class, ()-> service.withdraw(zeroAmount));
        assertThrows(InvalidAmountException.class, ()-> service.withdraw(negativeAmount));
    }

    @DisplayName("Deposit, Withdraw, Deposit sequence has correct Balance and order")
    @Test
    void testSequenceBalance(){
        List<Transaction> historyOperationOrder = new ArrayList<>();
        historyOperationOrder.add(service.deposit(amount));
        assertEquals(amount, service.balanceCents());
        historyOperationOrder.add(service.withdraw(amount));
        assertEquals(zeroAmount, service.balanceCents());
        historyOperationOrder.add(service.deposit(amount));
        assertEquals(amount, service.balanceCents());
        assertEquals(3, service.history().size());
        List<Transaction> history = service.history();
        assertEquals(historyOperationOrder.get(0).type(), history.get(0).type());
        assertEquals(historyOperationOrder.get(1).type(), history.get(1).type());
        assertEquals(historyOperationOrder.get(2).type(), history.get(2).type());
    }

    @DisplayName("History returns a copy that can't be mutated")
    @Test
    void testHistoryCopy(){
        service.deposit(amount);
        List<Transaction> history = service.history();
        assertThrows(UnsupportedOperationException.class, () -> history.
            add(new Transaction(1, TransactionType.DEPOSIT, amount, Instant.now())));
    }
    @DisplayName("Ids should increment across transactions")
    @Test
    void testCounter(){
        Transaction deposit = service.deposit(amount);
        assertEquals(1, deposit.id());
        Transaction secondDeposit = service.deposit(amount);
        assertEquals(2, secondDeposit.id());
    }
}