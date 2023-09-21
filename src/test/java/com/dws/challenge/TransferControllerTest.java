package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class TransferControllerTest {

    @MockBean
    NotificationService notificationService;
    @Autowired
    private AccountsService accountsService;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
        accountsService.getAccountsRepository().clearAccounts();
    }


    @Test
    void startTransfer() throws Exception {
        accountsService.createAccount(new Account("A1", BigDecimal.valueOf(10)));
        accountsService.createAccount(new Account("A2", BigDecimal.valueOf(10)));
        this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON).content("{\"fromAccount\":\"A1\",\"toAccount\":\"A2\",\"amount\":2}")).andExpect(status().isOk());
    }


    @Test
    void startTransferSameSenderAndReceiver() throws Exception {
        accountsService.createAccount(new Account("A1", BigDecimal.valueOf(10)));
        accountsService.createAccount(new Account("A2", BigDecimal.valueOf(10)));
        this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON).content("{\"fromAccount\":\"A1\",\"toAccount\":\"A1\",\"amount\":2}")).andExpect(status().isBadRequest());
    }


    @Test
    void startTransferNoReceiver() throws Exception {
        accountsService.createAccount(new Account("A1", BigDecimal.valueOf(10)));
        accountsService.createAccount(new Account("A2", BigDecimal.valueOf(10)));
        this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON).content("{\"fromAccount\":\"A1\",\"amount\":2}")).andExpect(status().isBadRequest());
    }

    @Test
    void startTransferNoSender() throws Exception {
        accountsService.createAccount(new Account("A1", BigDecimal.valueOf(10)));
        accountsService.createAccount(new Account("A2", BigDecimal.valueOf(10)));
        this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON).content("{\"toAccount\":\"A2\",\"amount\":2}")).andExpect(status().isBadRequest());
    }


    @Test
    void startTransferNegativeAmount() throws Exception {
        accountsService.createAccount(new Account("A1", BigDecimal.valueOf(10)));
        accountsService.createAccount(new Account("A2", BigDecimal.valueOf(10)));
        this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON).content("{\"fromAccount\":\"A1\",\"toAccount\":\"A2\",\"amount\":-2}")).andExpect(status().isBadRequest());
    }

    @Test
    void startTransferNoAmount() throws Exception {
        accountsService.createAccount(new Account("A1", BigDecimal.valueOf(10)));
        accountsService.createAccount(new Account("A2", BigDecimal.valueOf(10)));
        this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON).content("{\"fromAccount\":\"A1\",\"toAccount\":\"A2\"}")).andExpect(status().isBadRequest());
    }


    @Test
    void startTransferNotEnoughFounds() throws Exception {
        accountsService.createAccount(new Account("A1", BigDecimal.valueOf(10)));
        accountsService.createAccount(new Account("A2", BigDecimal.valueOf(10)));
        this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON).content("{\"fromAccount\":\"A1\",\"toAccount\":\"A2\",\"amount\":25}")).andExpect(status().isBadRequest());
    }
}