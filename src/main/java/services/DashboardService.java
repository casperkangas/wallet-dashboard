package services;

import database.ConnectionFactory;
import database.DatabaseConfiguration;
import database.repositories.AccountRepository;
import models.Account;
import java.math.BigDecimal;
import java.util.List;

public class DashboardService {
    private final AccountRepository accountRepository;

    public DashboardService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    public DashboardService() {
        this(new AccountRepository(new ConnectionFactory(new DatabaseConfiguration())));
    }

    public BigDecimal calculateTotalBalance() {
        try {
            List<Account> accounts = accountRepository.findAll();
            return accounts.stream()
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
}
