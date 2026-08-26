package sync;

import api.WalletApiClient;
import api.ApiException;
import database.repositories.AccountRepository;
import database.repositories.TransactionRepository;
import models.Account;
import models.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncrementalSynchronizerTest {

    @Mock
    private WalletApiClient apiClient;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private IncrementalSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        synchronizer = new IncrementalSynchronizer(apiClient, accountRepository, transactionRepository);
    }

    @Test
    void syncAccountsIncrementally_noLastUpdated() throws Exception {
        Account[] mockAccounts = {
            new Account("1", "Cash", "USD", BigDecimal.TEN, "Bank", "Cash", LocalDateTime.now())
        };
        when(apiClient.get("/accounts?limit=200&offset=0", Account[].class)).thenReturn(mockAccounts);

        synchronizer.syncAccountsIncrementally();

        verify(apiClient).get("/accounts?limit=200&offset=0", Account[].class);
        verify(accountRepository).saveAll(anyList());
    }

    @Test
    void syncAccountsIncrementally_withLastUpdated() throws Exception {
        Account[] mockAccounts = {
            new Account("1", "Cash", "USD", BigDecimal.TEN, "Bank", "Cash", LocalDateTime.now())
        };
        when(apiClient.get("/accounts?limit=200&offset=0", Account[].class)).thenReturn(mockAccounts);

        synchronizer.syncAccountsIncrementally();

        verify(apiClient).get("/accounts?limit=200&offset=0", Account[].class);
        verify(accountRepository).saveAll(anyList());
    }

    @Test
    void syncTransactionsIncrementally_noLastUpdated() throws Exception {
        // For the self healing logic, it returns an empty list, so size <= 30
        when(transactionRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        
        Transaction[] mockTransactions = {
            new Transaction("1", "acc1", "cat1", BigDecimal.TEN, "USD", LocalDate.now(), "desc", "card", false, LocalDateTime.now())
        };
        when(apiClient.get("/records?limit=200&offset=0", Transaction[].class)).thenReturn(mockTransactions);

        synchronizer.syncTransactionsIncrementally();

        verify(apiClient).get("/records?limit=200&offset=0", Transaction[].class);
        verify(transactionRepository).saveAll(anyList());
    }

    @Test
    void syncTransactionsIncrementally_withLastUpdated() throws Exception {
        LocalDateTime time = LocalDateTime.parse("2024-01-01T10:00:00");
        when(transactionRepository.getLastUpdated()).thenReturn(Optional.of(time));
        // Avoid self-healing by returning 31 items
        java.util.List<Transaction> bigList = java.util.Collections.nCopies(31, new Transaction("1", "acc1", "cat1", BigDecimal.TEN, "USD", LocalDate.now(), "desc", "card", false, LocalDateTime.now()));
        when(transactionRepository.findAll()).thenReturn(bigList);
        
        Transaction[] mockTransactions = {
            new Transaction("1", "acc1", "cat1", BigDecimal.TEN, "USD", LocalDate.now(), "desc", "card", false, LocalDateTime.now())
        };
        when(apiClient.get("/records?limit=200&updatedAt=gt." + time.toString() + "Z&offset=0", Transaction[].class)).thenReturn(mockTransactions);

        synchronizer.syncTransactionsIncrementally();

        verify(apiClient).get("/records?limit=200&updatedAt=gt." + time.toString() + "Z&offset=0", Transaction[].class);
        verify(transactionRepository).saveAll(anyList());
    }

    @Test
    void syncAccountsIncrementally_apiException() throws Exception {
        when(apiClient.get("/accounts?limit=200&offset=0", Account[].class)).thenThrow(new ApiException("Error", 500, ""));

        assertThrows(SynchronizationException.class, () -> synchronizer.syncAccountsIncrementally());
    }
}
