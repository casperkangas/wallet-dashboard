package services;

import database.repositories.AccountRepository;
import database.repositories.CategoryRepository;
import database.repositories.TransactionRepository;
import models.Category;
import models.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private DashboardService dashboardService;
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        dashboardService = new DashboardService(accountRepository, transactionRepository, categoryRepository);
    }

    @Test
    void getMonthlySavingsRate_ExcludesFinancialInvestments() throws SQLException {
        // Arrange
        LocalDate now = LocalDate.now();
        
        // Mock Categories
        Category incomeCat = new Category("cat-income", "Salary", null, "icon");
        Category expenseCat = new Category("cat-expense", "Groceries", null, "icon");
        Category investCat = new Category("cat-invest", "Fin. investments", null, "icon");
        Category transferCat = new Category("cat-transfer", "Transfer to Savings", null, "icon");
        
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(incomeCat, expenseCat, investCat, transferCat));
        
        // Mock Transactions (Current Month)
        Transaction tIncome = new Transaction("1", "acc1", "cat-income", new BigDecimal("2000.00"), "currency", now, "note", "payee", false, now.atStartOfDay());
        Transaction tExpense = new Transaction("2", "acc1", "cat-expense", new BigDecimal("-1500.00"), "currency", now, "note", "payee", false, now.atStartOfDay());
        Transaction tInvest = new Transaction("3", "acc1", "cat-invest", new BigDecimal("-1792.00"), "currency", now, "note", "payee", false, now.atStartOfDay()); // The test scenario
        Transaction tTransfer = new Transaction("4", "acc1", "cat-transfer", new BigDecimal("-500.00"), "currency", now, "note", "payee", false, now.atStartOfDay());
        
        when(transactionRepository.findAll()).thenReturn(Arrays.asList(tIncome, tExpense, tInvest, tTransfer));
        
        // Act
        String savingsRate = dashboardService.getMonthlySavingsRate();
        
        // Assert
        // Income = 2000
        // Expected Expenses (excluding Fin. investments and Transfers) = 1500
        // Savings = 2000 - 1500 = 500
        // Savings Rate = (500 / 2000) * 100 = 25.0%
        
        assertEquals("25.0%", savingsRate);
    }
}
