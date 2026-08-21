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
    long account = 1L;

    @DisplayName("Deposit should increase balance by respective amount")
    @Test
    void testDepositIncreaseAmount() {
        service.deposit(account, amount);

        assertEquals(amount, service.balanceCents(account));
    }

    @DisplayName("Balance should not throw exception when at zero")
    @Test
    void testBalanceZero() {
        assertEquals(0, service.balanceCents(account));    }

    @DisplayName("Withdraw to zero does not throw exception")
    @Test
    void testWithdrawToZero(){
        service.deposit(account, amount);

        assertDoesNotThrow(()->service.withdraw(account, amount));
        assertEquals(zeroAmount, service.balanceCents(account));
    }

    @DisplayName("Overdraft throws InsufficientFundsException")
    @Test
    void testOverdraft(){
        assertThrows(InsufficientFundsException.class, ()-> service.withdraw(account, amount));
        assertEquals(0, service.history(account).size());
    }

    @DisplayName("Zero Or Negative Amounts throws InvalidAmountException")
    @Test
    void testZeroOrNegativeAmount(){
        assertThrows(InvalidAmountException.class, ()-> service.deposit(account, zeroAmount));
        assertThrows(InvalidAmountException.class, ()-> service.deposit(account, negativeAmount));
        assertThrows(InvalidAmountException.class, ()-> service.withdraw(account, zeroAmount));
        assertThrows(InvalidAmountException.class, ()-> service.withdraw(account, negativeAmount));
    }

    @DisplayName("Deposit, Withdraw, Deposit sequence has correct Balance and order")
    @Test
    void testSequenceBalance(){
        List<Transaction> historyOperationOrder = new ArrayList<>();
        historyOperationOrder.add(service.deposit(account, amount));
        assertEquals(amount, service.balanceCents(account));
        historyOperationOrder.add(service.withdraw(account, amount));
        assertEquals(zeroAmount, service.balanceCents(account));
        historyOperationOrder.add(service.deposit(account, amount));
        assertEquals(amount, service.balanceCents(account));
        assertEquals(3, service.history(account).size());
        List<Transaction> history = service.history(account);
        assertEquals(historyOperationOrder.get(0).type(), history.get(0).type());
        assertEquals(historyOperationOrder.get(1).type(), history.get(1).type());
        assertEquals(historyOperationOrder.get(2).type(), history.get(2).type());
    }

    @DisplayName("History returns a copy that can't be mutated")
    @Test
    void testHistoryCopy(){
        service.deposit(account, amount);
        List<Transaction> history = service.history(account);
        assertThrows(UnsupportedOperationException.class, () -> history.
            add(new Transaction(1, TransactionType.DEPOSIT, amount, Instant.now())));
    }
    @DisplayName("Ids should increment across transactions")
    @Test
    void testCounter(){
        Transaction deposit = service.deposit(account, amount);
        assertEquals(1, deposit.id());
        Transaction secondDeposit = service.deposit(account, amount);
        assertEquals(2, secondDeposit.id());
    }
}