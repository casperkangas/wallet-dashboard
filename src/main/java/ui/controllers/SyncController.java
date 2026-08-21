package ui.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import sync.SynchronizationService;
import sync.IncrementalSynchronizer;
import api.WalletApiClient;
import api.ApiConfiguration;
import api.AuthenticationService;
import database.ConnectionFactory;
import database.DatabaseConfiguration;
import database.repositories.AccountRepository;
import database.repositories.TransactionRepository;
import database.repositories.CategoryRepository;
import database.repositories.BudgetRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SyncController {
    
    @FXML private Button btnSyncNow;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label lblStatus;
    @FXML private Label lblLastSynced;
    
    private SynchronizationService syncService;
    
    public SyncController() {
        try {
            ApiConfiguration apiConfig = new ApiConfiguration();
            AuthenticationService authService = new AuthenticationService(apiConfig);
            WalletApiClient apiClient = new WalletApiClient(apiConfig, authService);
            
            DatabaseConfiguration dbConfig = new DatabaseConfiguration();
            ConnectionFactory connectionFactory = new ConnectionFactory(dbConfig);
            
            AccountRepository accountRepo = new AccountRepository(connectionFactory);
            TransactionRepository transactionRepo = new TransactionRepository(connectionFactory);
            CategoryRepository categoryRepo = new CategoryRepository(connectionFactory);
            BudgetRepository budgetRepo = new BudgetRepository(connectionFactory);
            
            IncrementalSynchronizer incrementalSync = new IncrementalSynchronizer(
                apiClient, accountRepo, transactionRepo
            );
            
            syncService = new SynchronizationService(
                apiClient, categoryRepo, budgetRepo, incrementalSync
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void initialize() {
        progressIndicator.setVisible(false);
    }
    
    @FXML
    public void performSync() {
        if (syncService == null) {
            lblStatus.setText("Initialization error. Check API configuration.");
            return;
        }
        
        btnSyncNow.setDisable(true);
        progressIndicator.setVisible(true);
        lblStatus.setText("Syncing...");
        
        Task<Void> syncTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                syncService.syncAll();
                return null;
            }
        };
        
        syncTask.setOnSucceeded(e -> {
            btnSyncNow.setDisable(false);
            progressIndicator.setVisible(false);
            lblStatus.setText("Synchronization successful.");
            lblLastSynced.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });
        
        syncTask.setOnFailed(e -> {
            btnSyncNow.setDisable(false);
            progressIndicator.setVisible(false);
            Throwable ex = syncTask.getException();
            lblStatus.setText("Synchronization failed: " + (ex != null ? ex.getMessage() : "Unknown error"));
            if (ex != null) ex.printStackTrace();
        });
        
        new Thread(syncTask).start();
    }
}
