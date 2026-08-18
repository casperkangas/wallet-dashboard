package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Account(
    String id,
    String name,
    String currency,
    BigDecimal balance,
    String institution,
    LocalDateTime updatedAt
) {}
