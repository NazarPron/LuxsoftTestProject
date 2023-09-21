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
        int countOfTrans = 10000;
        ExecutorService service = Executors.newFixedThreadPool(3);
        Account a1 = new Account("ID1", BigDecimal.valueOf(countOfTrans));
        Account a2 = new Account("ID2", BigDecimal.valueOf(countOfTrans));
        Account a3 = new Account("ID3", BigDecimal.valueOf(countOfTrans));
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> transferFunction(a1, a2,countOfTrans), service);
        CompletableFuture<Void> f2 = CompletableFuture.runAsync(() -> transferFunction(a3, a1,countOfTrans), service);
        CompletableFuture<Void> f3 = CompletableFuture.runAsync(() -> transferFunction(a2, a3,countOfTrans), service);
        f1.get();
        f2.get();
        f3.get();
        assertAll(() -> assertThat(a1.getBalance()).isEqualTo(BigDecimal.valueOf(countOfTrans)), () -> assertThat(a2.getBalance()).isEqualTo(BigDecimal.valueOf(countOfTrans)), () -> assertThat(a3.getBalance()).isEqualTo(BigDecimal.valueOf(countOfTrans)));
    }

    private void transferFunction(Account a, Account b,int countOfTrans) {
        for (int i = 1; i < countOfTrans; i++) {
            transferService.transfer(a, b, BigDecimal.ONE);
        }
    }

}