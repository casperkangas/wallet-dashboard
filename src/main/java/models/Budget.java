package models;

import java.math.BigDecimal;

public record Budget(
    String id,
    String categoryId,
    BigDecimal limitAmount,
    String period
) {}
