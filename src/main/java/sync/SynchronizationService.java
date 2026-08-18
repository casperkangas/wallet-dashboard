package sync;

import api.WalletApiClient;
import api.ApiException;
import database.repositories.AccountRepository;
import database.repositories.BudgetRepository;
import database.repositories.CategoryRepository;
import database.repositories.TransactionRepository;
import models.Account;
import models.Budget;
import models.Category;
import models.Transaction;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Logger;

public class SynchronizationService {
    private static final Logger LOGGER = Logger.getLogger(SynchronizationService.class.getName());

    private final WalletApiClient apiClient;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;

    public SynchronizationService(
            WalletApiClient apiClient,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            BudgetRepository budgetRepository,
            TransactionRepository transactionRepository) {
        this.apiClient = apiClient;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }

    public void syncAll() throws SynchronizationException {
        syncAccounts();
        syncCategories();
        syncBudgets();
        syncTransactions();
    }

    public void syncAccounts() throws SynchronizationException {
        try {
            Account[] accounts = apiClient.get("/accounts", Account[].class);
            if (accounts != null && accounts.length > 0) {
                accountRepository.saveAll(Arrays.asList(accounts));
                LOGGER.info("Successfully synchronized " + accounts.length + " accounts.");
            }
        } catch (ApiException | SQLException e) {
            throw new SynchronizationException("Failed to synchronize accounts", e);
        }
    }

    public void syncCategories() throws SynchronizationException {
        try {
            Category[] categories = apiClient.get("/categories", Category[].class);
            if (categories != null && categories.length > 0) {
                categoryRepository.saveAll(Arrays.asList(categories));
                LOGGER.info("Successfully synchronized " + categories.length + " categories.");
            }
        } catch (ApiException | SQLException e) {
            throw new SynchronizationException("Failed to synchronize categories", e);
        }
    }

    public void syncBudgets() throws SynchronizationException {
        try {
            Budget[] budgets = apiClient.get("/budgets", Budget[].class);
            if (budgets != null && budgets.length > 0) {
                budgetRepository.saveAll(Arrays.asList(budgets));
                LOGGER.info("Successfully synchronized " + budgets.length + " budgets.");
            }
        } catch (ApiException | SQLException e) {
            throw new SynchronizationException("Failed to synchronize budgets", e);
        }
    }

    public void syncTransactions() throws SynchronizationException {
        try {
            // Note: The Wallet API uses /records for transactions
            Transaction[] transactions = apiClient.get("/records", Transaction[].class);
            if (transactions != null && transactions.length > 0) {
                transactionRepository.saveAll(Arrays.asList(transactions));
                LOGGER.info("Successfully synchronized " + transactions.length + " transactions.");
            }
        } catch (ApiException | SQLException e) {
            throw new SynchronizationException("Failed to synchronize transactions", e);
        }
    }
}
