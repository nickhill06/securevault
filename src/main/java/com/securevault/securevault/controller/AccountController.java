package com.securevault.securevault.controller;


import com.securevault.securevault.dto.TransactionResponse;
import java.util.List;
import com.securevault.securevault.dto.TransferRequest;
import com.securevault.securevault.dto.TransferResponse;
import com.securevault.securevault.service.TransferService;
import com.securevault.securevault.dto.AccountResponse;
import com.securevault.securevault.dto.AmountRequest;
import com.securevault.securevault.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Audited;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {


    private final AccountService accountService;
    private final TransferService transferService;
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getMyAccount(Authentication authentication) {
        return ResponseEntity.ok(accountService.getMyAccount(authentication.getName()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponse> deposit(Authentication authentication,
                                                   @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.deposit(authentication.getName(), request.getAmount()));
    }
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(Authentication authentication,
                                                     @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transferService.transfer(
                authentication.getName(),
                request.getReceiverAccountNumber(),
                request.getAmount()
        ));
    }


    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getMyTransactions(Authentication authentication) {
        return ResponseEntity.ok(accountService.getMyTransactions(authentication.getName()));
    }


    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponse> withdraw(Authentication authentication,
                                                    @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.withdraw(authentication.getName(), request.getAmount()));
    }
}