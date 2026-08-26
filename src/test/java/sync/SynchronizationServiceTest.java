package sync;

import api.WalletApiClient;
import database.repositories.BudgetRepository;
import database.repositories.CategoryRepository;
import models.Budget;
import models.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SynchronizationServiceTest {

    @Mock
    private WalletApiClient apiClient;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private IncrementalSynchronizer incrementalSynchronizer;

    private SynchronizationService syncService;

    @BeforeEach
    void setUp() {
        syncService = new SynchronizationService(
                apiClient,
                categoryRepository,
                budgetRepository,
                incrementalSynchronizer
        );
    }

    @Test
    void syncAccounts_delegatesToIncrementalSynchronizer() throws Exception {
        syncService.syncAccounts();
        verify(incrementalSynchronizer).syncAccountsIncrementally();
    }

    @Test
    void syncTransactions_delegatesToIncrementalSynchronizer() throws Exception {
        syncService.syncTransactions();
        verify(incrementalSynchronizer).syncTransactionsIncrementally();
    }

    @Test
    void syncCategories_success() throws Exception {
        Category[] mockCategories = {
            new Category("1", "Food", null, "icon")
        };
        when(apiClient.get("/categories", Category[].class)).thenReturn(mockCategories);

        syncService.syncCategories();

        verify(apiClient).get("/categories", Category[].class);
        verify(categoryRepository).saveAll(anyList());
    }

    @Test
    void syncBudgets_success() throws Exception {
        Budget[] mockBudgets = new Budget[]{
            new Budget("1", "Test Budget", "cat1", new BigDecimal("500.00"), "MONTHLY", false, null, null, null)
        };
        when(apiClient.get("/budgets", Budget[].class)).thenReturn(mockBudgets);

        syncService.syncBudgets();

        verify(apiClient).get("/budgets", Budget[].class);
        verify(budgetRepository).saveAll(anyList());
    }

    @Test
    void syncAll_success() throws Exception {
        when(apiClient.get("/categories", Category[].class)).thenReturn(new Category[0]);
        when(apiClient.get("/budgets", Budget[].class)).thenReturn(new Budget[0]);

        syncService.syncAll();

        verify(incrementalSynchronizer).syncAccountsIncrementally();
        verify(apiClient).get("/categories", Category[].class);
        verify(apiClient).get("/budgets", Budget[].class);
        verify(incrementalSynchronizer).syncTransactionsIncrementally();
    }
}
