package com.securevault.securevault.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String status;
    private String senderAccountNumber;
    private BigDecimal senderNewBalance;
    private String receiverAccountNumber;
    private String idempotencyKey;
}