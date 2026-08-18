package sync;

import api.WalletApiClient;
import api.ApiException;
import database.repositories.BudgetRepository;
import database.repositories.CategoryRepository;
import models.Budget;
import models.Category;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Logger;

public class SynchronizationService {
    private static final Logger LOGGER = Logger.getLogger(SynchronizationService.class.getName());

    private final WalletApiClient apiClient;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final IncrementalSynchronizer incrementalSynchronizer;

    public SynchronizationService(
            WalletApiClient apiClient,
            CategoryRepository categoryRepository,
            BudgetRepository budgetRepository,
            IncrementalSynchronizer incrementalSynchronizer) {
        this.apiClient = apiClient;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
        this.incrementalSynchronizer = incrementalSynchronizer;
    }

    public void syncAll() throws SynchronizationException {
        syncAccounts();
        syncCategories();
        syncBudgets();
        syncTransactions();
    }

    public void syncAccounts() throws SynchronizationException {
        incrementalSynchronizer.syncAccountsIncrementally();
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
        incrementalSynchronizer.syncTransactionsIncrementally();
    }
}
