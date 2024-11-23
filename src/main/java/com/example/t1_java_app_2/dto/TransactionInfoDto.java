package com.example.t1_java_app_2.dto;
//clientId, accountId, transactionId, timestamp, transaction.amount, account.balance

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionInfoDto {
    @JsonProperty("client_id")
    private Long clientId;

    @JsonProperty("account_id")
    private Long accountId;
    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("transaction_amount")
    private Double transactionAmount;

    @JsonProperty("account_balance")
    private Double accountBalance;

}
