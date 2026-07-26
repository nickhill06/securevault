# SecureVault — Digital Banking & Fund Transfer System

A full-stack banking simulation demonstrating atomic fund transfers, 
concurrency-safe locking, JWT authentication, and role-based access control.

## Tech Stack
- Spring Boot 4.1.0, Spring Security, Spring Data JPA
- MySQL
- JWT (jjwt)

## Key Features
- Atomic transfers using @Transactional with automatic rollback
- Pessimistic locking with deadlock-safe lock ordering (prevents double-spending)
- Role-based access control (CUSTOMER / ADMIN)
- Full transaction audit logging
- Clean, consistent JSON error handling

## API Endpoints
- POST /api/auth/register
- POST /api/auth/login
- GET /api/accounts/me
- POST /api/accounts/deposit
- POST /api/accounts/withdraw
- POST /api/accounts/transfer
- GET /api/accounts/transactions
- GET /api/admin/accounts (ADMIN only)
- GET /api/admin/transactions (ADMIN only)

## Setup
1. Create MySQL database: `securevault_db`
2. Update `src/main/resources/application.properties` with your DB credentials
3. Run `SecurevaultApplication.java`
