package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.TransferException;
import com.dws.challenge.service.NotificationService;
import com.dws.challenge.service.TransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TransferServiceTest {
    @Autowired
    TransferService transferService;
    @MockBean
    NotificationService notificationService;

    @Test
    public void transfer() {
        Account account1 = new Account("ID1", BigDecimal.ONE);
        Account account2 = new Account("ID2", BigDecimal.ONE);
        transferService.transfer(account1, account2, BigDecimal.ONE);
        assertAll(() -> assertThat(account1.getBalance().compareTo(BigDecimal.ZERO)).isEqualTo(0), () -> assertThat(account2.getBalance().compareTo(BigDecimal.valueOf(2))).isEqualTo(0));
    }


    @Test
    public void transferSameAccount() {
        Account account1 = new Account("ID1", BigDecimal.ONE);
        try {
            transferService.transfer(account1, account1, BigDecimal.ONE);
            fail("Should fail when using same account as sender and receiver");
        } catch (TransferException ex) {
            assertThat(ex.getMessage()).isEqualTo("You are trying to transfer between same account");
        }
    }


    @Test
    public void transferOutOfBalance() {
        Account account1 = new Account("ID1", BigDecimal.ONE);
        Account account2 = new Account("ID2", BigDecimal.ONE);
        try {
            transferService.transfer(account1, account2, BigDecimal.TEN);
            fail("Should fail when try to transfer more than you have");
        } catch (TransferException ex) {
            assertThat(ex.getMessage()).isEqualTo("Not enough funds to transfer");
        }
    }


    @Test
    public void transferConcurrent() throws ExecutionException, InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(10);
        Account a1 = new Account("ID1", BigDecimal.valueOf(10000));
        Account a2 = new Account("ID2", BigDecimal.valueOf(10000));
        Account a3 = new Account("ID3", BigDecimal.valueOf(10000));
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> transferFunction(a1, a2), service);
        CompletableFuture<Void> f2 = CompletableFuture.runAsync(() -> transferFunction(a3, a1), service);
        CompletableFuture<Void> f3 = CompletableFuture.runAsync(() -> transferFunction(a2, a3), service);
        f1.get();
        f2.get();
        f3.get();
        assertAll(() -> assertThat(a1.getBalance()).isEqualTo(BigDecimal.valueOf(10000)), () -> assertThat(a2.getBalance()).isEqualTo(BigDecimal.valueOf(10000)), () -> assertThat(a3.getBalance()).isEqualTo(BigDecimal.valueOf(10000)));
    }

    private void transferFunction(Account a, Account b) {
        for (int i = 1; i < 10000; i++) {
            transferService.transfer(a, b, BigDecimal.ONE);
        }
    }

}