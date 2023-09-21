package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferDetails;
import com.dws.challenge.exception.TransferException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransferService {
    private final AccountsService accountsService;
    private final NotificationService notificationService;

    @Autowired
    public TransferService(AccountsService accountsService, NotificationService notificationService) {
        this.accountsService = accountsService;
        this.notificationService = notificationService;
    }

    public void transfer(TransferDetails transferDetails) {
        transfer(accountsService.getAccount(transferDetails.getFromAccount()), accountsService.getAccount(transferDetails.getToAccount()), transferDetails.getAmount());
    }

    public void transfer(Account from, Account to, BigDecimal amount) throws TransferException {
        if (!from.getAccountId().equals(to.getAccountId())) {
            Object[] locks = getLocks(from, to);
            synchronized (locks[0]) {
                synchronized (locks[1]) {
                    if (from.getBalance().compareTo(amount) >= 0) {
                        from.setBalance(from.getBalance().subtract(amount));
                        to.setBalance(to.getBalance().add(amount));
                        notificationService.notifyAboutTransfer(from, "You transferred " + amount.toPlainString() + " to account " + to.getAccountId());
                        notificationService.notifyAboutTransfer(to, "You received transfer " + amount.toPlainString() + " from " + from.getAccountId());
                    } else throw new TransferException("Not enough funds to transfer");
                }
            }
        } else throw new TransferException("You are trying to transfer between same account");
    }

    private static Object[] getLocks(Account account1, Account account2) {
        return account1.getCreationTimeNano() > account2.getCreationTimeNano() ? new Object[]{account1, account2} : new Object[]{account2, account1};
    }
}
