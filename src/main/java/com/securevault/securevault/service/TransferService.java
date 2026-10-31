package com.securevault.securevault.service;

import com.securevault.securevault.dto.TransferResponse;
import com.securevault.securevault.entity.Account;
import com.securevault.securevault.entity.Transaction;
import com.securevault.securevault.entity.User;
import com.securevault.securevault.repository.AccountRepository;
import com.securevault.securevault.repository.TransactionRepository;
import com.securevault.securevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransferResponse transfer(String senderUsername, String receiverAccountNumber, BigDecimal amount) {

        User senderUser = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Account senderAccount = accountRepository.findByUserId(senderUser.getId())
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        Account receiverAccount = accountRepository.findByAccountNumber(receiverAccountNumber)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new RuntimeException("Cannot transfer to your own account");
        }

        if (senderAccount.getBalance().compareTo(amount) < 0) {
            recordTransaction(senderAccount, receiverAccount, amount, Transaction.TransactionStatus.FAILED);
            throw new RuntimeException("Insufficient balance");
        }

        // Lock rows in a consistent order (lower account ID first) to prevent deadlocks
        Account first = senderAccount.getId() < receiverAccount.getId() ? senderAccount : receiverAccount;
        Account second = senderAccount.getId() < receiverAccount.getId() ? receiverAccount : senderAccount;

        Account lockedFirst = accountRepository.findByIdForUpdate(first.getId())
                .orElseThrow(() -> new RuntimeException("Account not found during lock"));
        Account lockedSecond = accountRepository.findByIdForUpdate(second.getId())
                .orElseThrow(() -> new RuntimeException("Account not found during lock"));

        Account lockedSender = lockedFirst.getId().equals(senderAccount.getId()) ? lockedFirst : lockedSecond;
        Account lockedReceiver = lockedFirst.getId().equals(receiverAccount.getId()) ? lockedFirst : lockedSecond;

        if (lockedSender.getBalance().compareTo(amount) < 0) {
            recordTransaction(lockedSender, lockedReceiver, amount, Transaction.TransactionStatus.FAILED);
            throw new RuntimeException("Insufficient balance");
        }

        lockedSender.setBalance(lockedSender.getBalance().subtract(amount));
        lockedReceiver.setBalance(lockedReceiver.getBalance().add(amount));

        accountRepository.save(lockedSender);
        accountRepository.save(lockedReceiver);

        String idempotencyKey = recordTransaction(lockedSender, lockedReceiver, amount, Transaction.TransactionStatus.SUCCESS);

        return new TransferResponse(
                "SUCCESS",
                lockedSender.getAccountNumber(),
                lockedSender.getBalance(),
                lockedReceiver.getAccountNumber(),
                idempotencyKey
        );
    }

    private String recordTransaction(Account sender, Account receiver, BigDecimal amount, Transaction.TransactionStatus status) {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction = Transaction.builder()
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(amount)
                .status(status)
                .idempotencyKey(idempotencyKey)
                .build();

        transactionRepository.save(transaction);
        return idempotencyKey;
    }
}