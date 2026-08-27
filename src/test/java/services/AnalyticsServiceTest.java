package services;

import database.repositories.DailySnapshotRepository;
import database.repositories.TransactionRepository;
import models.MonthlyTrendRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AnalyticsServiceTest {

    private AnalyticsService analyticsService;
    private TransactionRepository transactionRepository;
    private DailySnapshotRepository dailySnapshotRepository;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        dailySnapshotRepository = mock(DailySnapshotRepository.class);
        analyticsService = new AnalyticsService(transactionRepository, dailySnapshotRepository);
    }

    @Test
    void calculateSavingsRate_PositiveSavings() {
        BigDecimal income = new BigDecimal("5000");
        BigDecimal expenses = new BigDecimal("4000");
        
        BigDecimal rate = analyticsService.calculateSavingsRate(income, expenses);
        
        assertEquals(new BigDecimal("20.0000"), rate);
    }

    @Test
    void calculateSavingsRate_NegativeSavings() {
        BigDecimal income = new BigDecimal("5000");
        BigDecimal expenses = new BigDecimal("6000");
        
        BigDecimal rate = analyticsService.calculateSavingsRate(income, expenses);
        
        assertEquals(new BigDecimal("-20.0000"), rate);
    }

    @Test
    void calculateSavingsRate_ZeroIncome() {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = new BigDecimal("4000");
        
        BigDecimal rate = analyticsService.calculateSavingsRate(income, expenses);
        
        assertEquals(BigDecimal.ZERO, rate);
    }

    @Test
    void calculateSavingsRate_NullIncome() {
        BigDecimal rate = analyticsService.calculateSavingsRate(null, new BigDecimal("4000"));
        
        assertEquals(BigDecimal.ZERO, rate);
    }

    @Test
    void calculateEmergencyFundRatio_NormalCase() {
        BigDecimal cash = new BigDecimal("12000");
        List<MonthlyTrendRecord> trends = Arrays.asList(
            new MonthlyTrendRecord("2026-06", new BigDecimal("5000"), new BigDecimal("4000")),
            new MonthlyTrendRecord("2026-07", new BigDecimal("5000"), new BigDecimal("3800")),
            new MonthlyTrendRecord("2026-08", new BigDecimal("5000"), new BigDecimal("4200"))
        ); // average expenses = 4000
        
        BigDecimal ratio = analyticsService.calculateEmergencyFundRatio(cash, trends);
        
        assertEquals(new BigDecimal("3.00"), ratio);
    }

    @Test
    void calculateEmergencyFundRatio_ZeroExpenses() {
        BigDecimal cash = new BigDecimal("12000");
        List<MonthlyTrendRecord> trends = Arrays.asList(
            new MonthlyTrendRecord("2026-06", new BigDecimal("5000"), BigDecimal.ZERO)
        );
        
        BigDecimal ratio = analyticsService.calculateEmergencyFundRatio(cash, trends);
        
        assertEquals(BigDecimal.ZERO, ratio);
    }

    @Test
    void calculateEmergencyFundRatio_EmptyTrends() {
        BigDecimal ratio = analyticsService.calculateEmergencyFundRatio(new BigDecimal("12000"), Collections.emptyList());
        assertEquals(BigDecimal.ZERO, ratio);
    }

    @Test
    void calculateFinancialIndependenceRatio_NormalCase() {
        BigDecimal investments = new BigDecimal("1000000");
        BigDecimal annualExpenses = new BigDecimal("40000");
        
        BigDecimal ratio = analyticsService.calculateFinancialIndependenceRatio(investments, annualExpenses);
        
        assertEquals(new BigDecimal("2500.0000"), ratio); // 25 times -> 2500%
    }

    @Test
    void calculateFinancialIndependenceRatio_ZeroExpenses() {
        BigDecimal ratio = analyticsService.calculateFinancialIndependenceRatio(new BigDecimal("1000000"), BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, ratio);
    }

    @Test
    void calculateExpenseVolatility_NormalCase() {
        List<MonthlyTrendRecord> trends = Arrays.asList(
            new MonthlyTrendRecord("2026-05", new BigDecimal("5000"), new BigDecimal("3800")),
            new MonthlyTrendRecord("2026-06", new BigDecimal("5000"), new BigDecimal("4000")),
            new MonthlyTrendRecord("2026-07", new BigDecimal("5000"), new BigDecimal("4200"))
        ); 
        // Mean = 4000
        // Variance: (3800-4000)^2 + (4000-4000)^2 + (4200-4000)^2 / (3-1)
        // = (40000 + 0 + 40000) / 2 = 40000
        // Std Dev = sqrt(40000) = 200.00
        
        BigDecimal volatility = analyticsService.calculateExpenseVolatility(trends);
        
        assertEquals(new BigDecimal("200.00"), volatility);
    }

    @Test
    void calculateExpenseVolatility_InsufficientData() {
        List<MonthlyTrendRecord> trends = Collections.singletonList(
            new MonthlyTrendRecord("2026-05", new BigDecimal("5000"), new BigDecimal("3800"))
        ); 
        
        BigDecimal volatility = analyticsService.calculateExpenseVolatility(trends);
        
        assertEquals(BigDecimal.ZERO, volatility);
    }
}
