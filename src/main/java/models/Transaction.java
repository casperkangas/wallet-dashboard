package models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Transaction(
    String id,
    String accountId,
    String categoryId,
    BigDecimal amount,
    String currency,
    LocalDate transactionDate,
    String description,
    String paymentMethod,
    boolean isTransfer,
    LocalDateTime createdAt
) {}
