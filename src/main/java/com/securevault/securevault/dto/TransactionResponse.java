package com.securevault.securevault.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private String status;
    private LocalDateTime timestamp;
}