package models;

import java.math.BigDecimal;

public record MonthlyTrendRecord(
    String yearMonth,
    BigDecimal income,
    BigDecimal expenses
) {}
