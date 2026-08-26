package services;

import database.ConnectionFactory;
import database.DatabaseConfiguration;
import database.repositories.BudgetRepository;
import database.repositories.CategoryRepository;
import database.repositories.TransactionRepository;
import models.Budget;
import models.Category;
import models.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public BudgetService() {
        ConnectionFactory connFactory = new ConnectionFactory(new DatabaseConfiguration());
        this.budgetRepository = new BudgetRepository(connFactory);
        this.transactionRepository = new TransactionRepository(connFactory);
        this.categoryRepository = new CategoryRepository(connFactory);
    }

    public record BudgetProgress(String budgetName, BigDecimal spentAmount, BigDecimal limitAmount, double progressPercentage, boolean isClosed) {}

    public List<BudgetProgress> getBudgetProgressForCurrentMonth() {
        List<BudgetProgress> results = new ArrayList<>();
        try {
            LocalDate now = LocalDate.now();
            List<Budget> budgets = budgetRepository.findAll();
            List<Transaction> allTransactions = transactionRepository.findAll();
            
            for (Budget budget : budgets) {
                BigDecimal spent = BigDecimal.ZERO;
                
                // Determine the active period for this budget
                LocalDate periodStart = now.withDayOfMonth(1);
                LocalDate periodEnd = now.withDayOfMonth(now.lengthOfMonth());
                
                if (budget.period() != null && budget.period().equals("BUDGET_CUSTOM")) {
                    if (budget.startDate() != null && !budget.startDate().isBlank()) {
                        periodStart = LocalDate.parse(budget.startDate());
                    }
                    if (budget.endDate() != null && !budget.endDate().isBlank()) {
                        periodEnd = LocalDate.parse(budget.endDate());
                    }
                } else if (budget.closed()) {
                    if (budget.closedDate() != null && !budget.closedDate().isBlank()) {
                        LocalDate closedDate = LocalDate.parse(budget.closedDate());
                        periodStart = closedDate.withDayOfMonth(1);
                        periodEnd = closedDate.withDayOfMonth(closedDate.lengthOfMonth());
                    } else {
                        // If it's closed but we don't know when, we can't accurately calculate progress
                        // Skip adding any transactions so it shows 0 spent.
                        periodStart = LocalDate.MAX;
                        periodEnd = LocalDate.MIN;
                    }
                }
                
                for (Transaction t : allTransactions) {
                    if (t.transactionDate() != null 
                        && !t.transactionDate().isBefore(periodStart) 
                        && !t.transactionDate().isAfter(periodEnd)) {
                        
                        if (!t.isTransfer()) {
                            boolean matchesCategory = false;
                            if (budget.categoryId() == null || budget.categoryId().isBlank()) {
                                matchesCategory = true;
                            } else {
                                matchesCategory = java.util.Arrays.asList(budget.categoryId().split(",")).contains(t.categoryId());
                            }
                            
                            if (matchesCategory) {
                                spent = spent.add(t.amount());
                            }
                        }
                    }
                }
                
                // Convert to positive spent amount (expenses are negative in DB)
                spent = spent.negate();
                
                String budgetName = budget.name();
                if (budgetName == null || budgetName.isBlank()) {
                    if (budget.categoryId() != null && !budget.categoryId().isBlank()) {
                        String firstCategoryId = budget.categoryId().split(",")[0];
                        budgetName = categoryRepository.findById(firstCategoryId)
                                .map(Category::name)
                                .orElse("Unknown Category Budget");
                    } else {
                        budgetName = "All Categories Budget";
                    }
                }
                
                BigDecimal limit = budget.limitAmount() != null ? budget.limitAmount() : BigDecimal.ZERO;
                double percentage = 0.0;
                if (limit.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = spent.divide(limit, 4, RoundingMode.HALF_UP).doubleValue();
                } else if (spent.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = 1.0; // Over budget if limit is 0 but spent > 0
                }
                
                results.add(new BudgetProgress(budgetName, spent, limit, percentage, budget.closed()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}
