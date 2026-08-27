package services;

import database.repositories.DailySnapshotRepository;
import database.repositories.TransactionRepository;
import models.DailySnapshot;
import models.MonthlyTrendRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class AnalyticsService {
    private final TransactionRepository transactionRepository;
    private final DailySnapshotRepository dailySnapshotRepository;

    public AnalyticsService(TransactionRepository transactionRepository, DailySnapshotRepository dailySnapshotRepository) {
        this.transactionRepository = transactionRepository;
        this.dailySnapshotRepository = dailySnapshotRepository;
    }

    public BigDecimal calculateSavingsRate(BigDecimal monthlyIncome, BigDecimal monthlyExpenses) {
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (monthlyExpenses == null) {
            monthlyExpenses = BigDecimal.ZERO;
        }
        
        BigDecimal savings = monthlyIncome.subtract(monthlyExpenses);
        
        return savings.divide(monthlyIncome, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    public BigDecimal calculateEmergencyFundRatio(BigDecimal cash, List<MonthlyTrendRecord> recentTrends) {
        if (cash == null || cash.compareTo(BigDecimal.ZERO) <= 0 || recentTrends == null || recentTrends.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalExpenses = BigDecimal.ZERO;
        for (MonthlyTrendRecord record : recentTrends) {
            totalExpenses = totalExpenses.add(record.expenses());
        }

        BigDecimal averageExpenses = totalExpenses.divide(new BigDecimal(recentTrends.size()), 4, RoundingMode.HALF_UP);
        if (averageExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return cash.divide(averageExpenses, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateFinancialIndependenceRatio(BigDecimal investments, BigDecimal annualExpenses) {
        if (investments == null || investments.compareTo(BigDecimal.ZERO) <= 0 || annualExpenses == null || annualExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return investments.divide(annualExpenses, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    public BigDecimal calculateExpenseVolatility(List<MonthlyTrendRecord> recentTrends) {
        if (recentTrends == null || recentTrends.size() < 2) {
            return BigDecimal.ZERO;
        }

        // Calculate mean
        BigDecimal totalExpenses = BigDecimal.ZERO;
        for (MonthlyTrendRecord record : recentTrends) {
            totalExpenses = totalExpenses.add(record.expenses());
        }
        BigDecimal mean = totalExpenses.divide(new BigDecimal(recentTrends.size()), 4, RoundingMode.HALF_UP);

        // Calculate variance
        BigDecimal sumSquaredDifferences = BigDecimal.ZERO;
        for (MonthlyTrendRecord record : recentTrends) {
            BigDecimal difference = record.expenses().subtract(mean);
            sumSquaredDifferences = sumSquaredDifferences.add(difference.multiply(difference));
        }
        BigDecimal variance = sumSquaredDifferences.divide(new BigDecimal(recentTrends.size() - 1), 4, RoundingMode.HALF_UP);

        // Standard deviation
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue())).setScale(2, RoundingMode.HALF_UP);
    }
}
