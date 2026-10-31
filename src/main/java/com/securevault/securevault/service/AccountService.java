package com.securevault.securevault.service;

import com.securevault.securevault.dto.AccountResponse;
import com.securevault.securevault.dto.AdminAccountView;
import com.securevault.securevault.dto.TransactionResponse;
import com.securevault.securevault.entity.Account;
import com.securevault.securevault.entity.Transaction;
import com.securevault.securevault.repository.AccountRepository;
import com.securevault.securevault.repository.TransactionRepository;
import com.securevault.securevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountResponse getMyAccount(String username) {
        Account account = getAccountByUsername(username);
        return new AccountResponse(account.getAccountNumber(), account.getBalance());
    }

    @Transactional
    public AccountResponse deposit(String username, BigDecimal amount) {
        Account account = getAccountByUsername(username);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        recordTransaction(null, account, amount, Transaction.TransactionStatus.SUCCESS);

        return new AccountResponse(account.getAccountNumber(), account.getBalance());
    }

    @Transactional
    public AccountResponse withdraw(String username, BigDecimal amount) {
        Account account = getAccountByUsername(username);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        recordTransaction(account, null, amount, Transaction.TransactionStatus.SUCCESS);

        return new AccountResponse(account.getAccountNumber(), account.getBalance());
    }

    public List<TransactionResponse> getMyTransactions(String username) {
        Account account = getAccountByUsername(username);

        return transactionRepository
                .findBySenderAccountIdOrReceiverAccountIdOrderByTimestampDesc(account.getId(), account.getId())
                .stream()
                .map(t -> new TransactionResponse(
                        t.getId(),
                        t.getSenderAccount() != null ? t.getSenderAccount().getAccountNumber() : null,
                        t.getReceiverAccount() != null ? t.getReceiverAccount().getAccountNumber() : null,
                        t.getAmount(),
                        t.getStatus().name(),
                        t.getTimestamp()
                ))
                .collect(Collectors.toList());
    }

    public List<AdminAccountView> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(account -> new AdminAccountView(
                        account.getUser().getId(),
                        account.getUser().getUsername(),
                        account.getUser().getEmail(),
                        account.getUser().getRole().name(),
                        account.getAccountNumber(),
                        account.getBalance()
                ))
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(t -> new TransactionResponse(
                        t.getId(),
                        t.getSenderAccount() != null ? t.getSenderAccount().getAccountNumber() : null,
                        t.getReceiverAccount() != null ? t.getReceiverAccount().getAccountNumber() : null,
                        t.getAmount(),
                        t.getStatus().name(),
                        t.getTimestamp()
                ))
                .collect(Collectors.toList());
    }

    private void recordTransaction(Account sender, Account receiver, BigDecimal amount, Transaction.TransactionStatus status) {
        Transaction transaction = Transaction.builder()
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(amount)
                .status(status)
                .idempotencyKey(UUID.randomUUID().toString())
                .build();

        transactionRepository.save(transaction);
    }

    private Account getAccountByUsername(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
}