package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySnapshot(
    LocalDate snapshotDate,
    BigDecimal netWorth,
    BigDecimal cash,
    BigDecimal investments,
    BigDecimal debt,
    BigDecimal monthlyIncome,
    BigDecimal monthlyExpenses
) {}
