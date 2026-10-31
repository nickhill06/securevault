package com.securevault.securevault.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAccountView {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String accountNumber;
    private BigDecimal balance;
}