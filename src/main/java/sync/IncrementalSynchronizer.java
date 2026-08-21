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
            boolean forceFullSync = accountRepository.findAll().stream().anyMatch(a -> a.accountType() == null);
            Optional<LocalDateTime> lastUpdated = forceFullSync ? Optional.empty() : accountRepository.getLastUpdated();
            
            String baseEndpoint = "/accounts?limit=200";
            if (lastUpdated.isPresent()) {
                baseEndpoint += "&updatedAt=gt." + lastUpdated.get().toString();
                LOGGER.info("Fetching accounts updated after " + lastUpdated.get());
            } else {
                LOGGER.info("Performing full sync of accounts...");
            }

            int offset = 0;
            int totalSynced = 0;
            Account[] accounts;
            
            do {
                String endpoint = baseEndpoint + "&offset=" + offset;
                accounts = apiClient.get(endpoint, Account[].class);
                
                if (accounts != null && accounts.length > 0) {
                    accountRepository.saveAll(Arrays.asList(accounts));
                    totalSynced += accounts.length;
                    offset += 200;
                }
            } while (accounts != null && accounts.length == 200);

            if (totalSynced > 0) {
                LOGGER.info("Synchronized " + totalSynced + " accounts.");
            } else {
                LOGGER.info("No new accounts to synchronize.");
            }
        } catch (ApiException | SQLException e) {
            throw new SynchronizationException("Failed to incrementally synchronize accounts", e);
        }
    }

    public void syncTransactionsIncrementally() throws SynchronizationException {
        try {
            boolean forceFullSync = transactionRepository.findAll().size() <= 30;
            Optional<LocalDateTime> lastUpdated = forceFullSync ? Optional.empty() : transactionRepository.getLastUpdated();
            
            String baseEndpoint = "/records?limit=200";
            if (lastUpdated.isPresent()) {
                baseEndpoint += "&createdAt=gt." + lastUpdated.get().toString();
                LOGGER.info("Fetching transactions created after " + lastUpdated.get());
            } else {
                LOGGER.info("Performing full sync of historical transactions...");
            }

            int offset = 0;
            int totalSynced = 0;
            Transaction[] transactions;
            
            do {
                String endpoint = baseEndpoint + "&offset=" + offset;
                transactions = apiClient.get(endpoint, Transaction[].class);
                
                if (transactions != null && transactions.length > 0) {
                    transactionRepository.saveAll(Arrays.asList(transactions));
                    totalSynced += transactions.length;
                    offset += 200;
                }
            } while (transactions != null && transactions.length == 200);

            if (totalSynced > 0) {
                LOGGER.info("Synchronized " + totalSynced + " transactions.");
            } else {
                LOGGER.info("No new transactions to synchronize.");
            }
        } catch (ApiException | SQLException e) {
            throw new SynchronizationException("Failed to incrementally synchronize transactions", e);
        }
    }
}
