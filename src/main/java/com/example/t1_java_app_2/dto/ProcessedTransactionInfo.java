package com.example.t1_java_app_2.dto;

import com.example.t1_java_app_2.dto.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessedTransactionInfo {
    @JsonProperty("transaction_id")
    private long transactionId;

    @JsonProperty("account_id")
    private long accountId;

    @JsonProperty("status")
    private TransactionStatus status;
}
