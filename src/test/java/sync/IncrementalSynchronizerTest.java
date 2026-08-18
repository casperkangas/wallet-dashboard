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
        when(accountRepository.getLastUpdated()).thenReturn(Optional.empty());
        Account[] mockAccounts = {
            new Account("1", "Cash", "USD", BigDecimal.TEN, "Bank", LocalDateTime.now())
        };
        when(apiClient.get("/accounts", Account[].class)).thenReturn(mockAccounts);

        synchronizer.syncAccountsIncrementally();

        verify(apiClient).get("/accounts", Account[].class);
        verify(accountRepository).saveAll(anyList());
    }

    @Test
    void syncAccountsIncrementally_withLastUpdated() throws Exception {
        LocalDateTime time = LocalDateTime.parse("2024-01-01T10:00:00");
        when(accountRepository.getLastUpdated()).thenReturn(Optional.of(time));
        Account[] mockAccounts = {
            new Account("1", "Cash", "USD", BigDecimal.TEN, "Bank", LocalDateTime.now())
        };
        when(apiClient.get("/accounts?updatedAt=gt." + time.toString(), Account[].class)).thenReturn(mockAccounts);

        synchronizer.syncAccountsIncrementally();

        verify(apiClient).get("/accounts?updatedAt=gt." + time.toString(), Account[].class);
        verify(accountRepository).saveAll(anyList());
    }

    @Test
    void syncTransactionsIncrementally_noLastUpdated() throws Exception {
        when(transactionRepository.getLastUpdated()).thenReturn(Optional.empty());
        Transaction[] mockTransactions = {
            new Transaction("1", "acc1", "cat1", BigDecimal.TEN, "USD", LocalDate.now(), "desc", "card", false, LocalDateTime.now())
        };
        when(apiClient.get("/records", Transaction[].class)).thenReturn(mockTransactions);

        synchronizer.syncTransactionsIncrementally();

        verify(apiClient).get("/records", Transaction[].class);
        verify(transactionRepository).saveAll(anyList());
    }

    @Test
    void syncTransactionsIncrementally_withLastUpdated() throws Exception {
        LocalDateTime time = LocalDateTime.parse("2024-01-01T10:00:00");
        when(transactionRepository.getLastUpdated()).thenReturn(Optional.of(time));
        Transaction[] mockTransactions = {
            new Transaction("1", "acc1", "cat1", BigDecimal.TEN, "USD", LocalDate.now(), "desc", "card", false, LocalDateTime.now())
        };
        when(apiClient.get("/records?createdAt=gt." + time.toString(), Transaction[].class)).thenReturn(mockTransactions);

        synchronizer.syncTransactionsIncrementally();

        verify(apiClient).get("/records?createdAt=gt." + time.toString(), Transaction[].class);
        verify(transactionRepository).saveAll(anyList());
    }

    @Test
    void syncAccountsIncrementally_apiException() throws Exception {
        when(accountRepository.getLastUpdated()).thenReturn(Optional.empty());
        when(apiClient.get("/accounts", Account[].class)).thenThrow(new ApiException("Error", 500, ""));

        assertThrows(SynchronizationException.class, () -> synchronizer.syncAccountsIncrementally());
    }
}
