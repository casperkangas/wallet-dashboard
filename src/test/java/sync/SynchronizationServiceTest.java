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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SynchronizationServiceTest {

    @Mock
    private WalletApiClient apiClient;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private SynchronizationService syncService;

    @BeforeEach
    void setUp() {
        syncService = new SynchronizationService(
                apiClient,
                accountRepository,
                categoryRepository,
                budgetRepository,
                transactionRepository
        );
    }

    @Test
    void syncAccounts_success() throws Exception {
        Account[] mockAccounts = {
            new Account("1", "Cash", "USD", BigDecimal.TEN, "Bank", LocalDateTime.now())
        };
        when(apiClient.get("/accounts", Account[].class)).thenReturn(mockAccounts);

        syncService.syncAccounts();

        verify(apiClient).get("/accounts", Account[].class);
        verify(accountRepository).saveAll(anyList());
    }

    @Test
    void syncAccounts_nullResponse() throws Exception {
        when(apiClient.get("/accounts", Account[].class)).thenReturn(null);

        syncService.syncAccounts();

        verify(accountRepository, org.mockito.Mockito.never()).saveAll(anyList());
    }

    @Test
    void syncAccounts_apiException() {
        when(apiClient.get("/accounts", Account[].class)).thenThrow(new ApiException("API Error", 500, ""));

        assertThrows(SynchronizationException.class, () -> syncService.syncAccounts());
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
        Budget[] mockBudgets = {
            new Budget("1", "cat1", BigDecimal.valueOf(100), "monthly")
        };
        when(apiClient.get("/budgets", Budget[].class)).thenReturn(mockBudgets);

        syncService.syncBudgets();

        verify(apiClient).get("/budgets", Budget[].class);
        verify(budgetRepository).saveAll(anyList());
    }

    @Test
    void syncTransactions_success() throws Exception {
        Transaction[] mockTransactions = {
            new Transaction("1", "acc1", "cat1", BigDecimal.TEN, "USD", LocalDate.now(), "desc", "card", false, LocalDateTime.now())
        };
        when(apiClient.get("/records", Transaction[].class)).thenReturn(mockTransactions);

        syncService.syncTransactions();

        verify(apiClient).get("/records", Transaction[].class);
        verify(transactionRepository).saveAll(anyList());
    }

    @Test
    void syncAll_success() throws Exception {
        when(apiClient.get("/accounts", Account[].class)).thenReturn(new Account[0]);
        when(apiClient.get("/categories", Category[].class)).thenReturn(new Category[0]);
        when(apiClient.get("/budgets", Budget[].class)).thenReturn(new Budget[0]);
        when(apiClient.get("/records", Transaction[].class)).thenReturn(new Transaction[0]);

        syncService.syncAll();

        verify(apiClient).get("/accounts", Account[].class);
        verify(apiClient).get("/categories", Category[].class);
        verify(apiClient).get("/budgets", Budget[].class);
        verify(apiClient).get("/records", Transaction[].class);
    }
}
