package sync;

import api.WalletApiClient;
import api.ApiException;
import database.repositories.AccountRepository;
import database.repositories.TransactionRepository;
import models.Account;
import models.Transaction;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

public class IncrementalSynchronizer {
    private static final Logger LOGGER = Logger.getLogger(IncrementalSynchronizer.class.getName());

    private final WalletApiClient apiClient;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public IncrementalSynchronizer(
            WalletApiClient apiClient,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {
        this.apiClient = apiClient;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public void syncAccountsIncrementally() throws SynchronizationException {
        try {
            Optional<LocalDateTime> lastUpdated = accountRepository.getLastUpdated();
            String endpoint = "/accounts";
            
            if (lastUpdated.isPresent()) {
                endpoint += "?updatedAt=gt." + lastUpdated.get().toString();
                LOGGER.info("Fetching accounts updated after " + lastUpdated.get());
            }

            Account[] accounts = apiClient.get(endpoint, Account[].class);
            if (accounts != null && accounts.length > 0) {
                accountRepository.saveAll(Arrays.asList(accounts));
                LOGGER.info("Incrementally synchronized " + accounts.length + " accounts.");
            } else {
                LOGGER.info("No new accounts to synchronize.");
            }
        } catch (ApiException | SQLException e) {
            throw new SynchronizationException("Failed to incrementally synchronize accounts", e);
        }
    }

    public void syncTransactionsIncrementally() throws SynchronizationException {
        try {
            Optional<LocalDateTime> lastUpdated = transactionRepository.getLastUpdated();
            // Note: The Wallet API uses /records for transactions
            String endpoint = "/records";
            
            if (lastUpdated.isPresent()) {
                endpoint += "?createdAt=gt." + lastUpdated.get().toString();
                LOGGER.info("Fetching transactions created after " + lastUpdated.get());
            }

            Transaction[] transactions = apiClient.get(endpoint, Transaction[].class);
            if (transactions != null && transactions.length > 0) {
                transactionRepository.saveAll(Arrays.asList(transactions));
                LOGGER.info("Incrementally synchronized " + transactions.length + " transactions.");
            } else {
                LOGGER.info("No new transactions to synchronize.");
            }
        } catch (ApiException | SQLException e) {
            throw new SynchronizationException("Failed to incrementally synchronize transactions", e);
        }
    }
}
