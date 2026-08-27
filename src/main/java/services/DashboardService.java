package services;

import database.ConnectionFactory;
import database.DatabaseConfiguration;
import database.repositories.AccountRepository;
import database.repositories.CategoryRepository;
import database.repositories.TransactionRepository;
import models.Account;
import models.Category;
import models.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public DashboardService(AccountRepository accountRepository, TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }
    
    public DashboardService() {
        this(
            new AccountRepository(new ConnectionFactory(new DatabaseConfiguration())),
            new TransactionRepository(new ConnectionFactory(new DatabaseConfiguration())),
            new CategoryRepository(new ConnectionFactory(new DatabaseConfiguration()))
        );
    }

    public BigDecimal calculateTotalBalance() {
        try {
            return accountRepository.findAll().stream()
                    .map(Account::balance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }
    
    public int getAccountCount() {
        try {
            return accountRepository.findAll().size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Map<String, BigDecimal> getBalancesByType() {
        try {
            Map<String, BigDecimal> balances = new HashMap<>();
            
            for (Account account : accountRepository.findAll()) {
                String type = account.accountType();
                if (type == null || type.isEmpty()) {
                    type = "Unknown";
                }
                
                BigDecimal balance = account.balance() != null ? account.balance() : BigDecimal.ZERO;
                balances.put(type, balances.getOrDefault(type, BigDecimal.ZERO).add(balance));
            }
            
            return balances;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public String getMonthlySavingsRate() {
        try {
            LocalDate now = LocalDate.now();
            List<Transaction> transactions = transactionRepository.findAll();
            
            Map<String, BigDecimal> categoryNetFlow = new HashMap<>();

            for (Transaction t : transactions) {
                if (t.transactionDate() != null && t.transactionDate().getYear() == now.getYear() && t.transactionDate().getMonthValue() == now.getMonthValue()) {
                    if (!t.isTransfer()) {
                        String catId = t.categoryId() != null ? t.categoryId() : "UNGROUPED";
                        BigDecimal amount = t.amount() != null ? t.amount() : BigDecimal.ZERO;
                        categoryNetFlow.put(catId, categoryNetFlow.getOrDefault(catId, BigDecimal.ZERO).add(amount));
                    }
                }
            }

            List<Category> allCategories = categoryRepository.findAll();
            List<String> excludedCategoryIds = new ArrayList<>();
            List<String> rootExcludedIds = new ArrayList<>();
            
            for (Category c : allCategories) {
                String name = c.name() != null ? c.name().toLowerCase() : "";
                if (name.contains("invest") || name.contains("transfer")) {
                    rootExcludedIds.add(c.id());
                    excludedCategoryIds.add(c.id());
                }
            }
            
            for (Category c : allCategories) {
                if (c.parentId() != null && rootExcludedIds.contains(c.parentId())) {
                    excludedCategoryIds.add(c.id());
                }
            }

            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expenses = BigDecimal.ZERO;

            for (Map.Entry<String, BigDecimal> entry : categoryNetFlow.entrySet()) {
                String catId = entry.getKey();
                if (excludedCategoryIds.contains(catId)) {
                    continue;
                }
                
                BigDecimal net = entry.getValue();
                if (net.compareTo(BigDecimal.ZERO) > 0) {
                    income = income.add(net);
                } else {
                    expenses = expenses.add(net.abs());
                }
            }

            if (income.compareTo(BigDecimal.ZERO) == 0) {
                return "0%";
            }

            BigDecimal savings = income.subtract(expenses);
            if (savings.compareTo(BigDecimal.ZERO) <= 0) {
                return "0%";
            }

            BigDecimal rate = savings.divide(income, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
            return String.format("%.1f%%", rate);
        } catch (Exception e) {
            e.printStackTrace();
            return "0%";
        }
    }
}
