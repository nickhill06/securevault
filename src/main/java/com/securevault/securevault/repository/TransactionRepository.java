package com.securevault.securevault.repository;

import com.securevault.securevault.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderAccountIdOrReceiverAccountIdOrderByTimestampDesc(Long senderId, Long receiverId);
    Optional<Transaction> findByIdempotencyKey(String key);
}