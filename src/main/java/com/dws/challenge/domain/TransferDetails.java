package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransferDetails {
    @NotNull
    @NotEmpty
    private final String fromAccount;

    @NotNull
    @NotEmpty
    private final String toAccount;

    @NotNull
    @Min(value = 0, message = "Initial balance must be positive.")
    private BigDecimal amount;

    public TransferDetails(String fromAccount,String toAccount) {
        this.fromAccount = fromAccount;
        this.amount = BigDecimal.ZERO;
        this.toAccount = toAccount;
    }

    @JsonCreator
    public TransferDetails(@JsonProperty("fromAccount") String fromAccount,
                           @JsonProperty("toAccount") String toAccount,
                           @JsonProperty("amount") BigDecimal amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }
}
