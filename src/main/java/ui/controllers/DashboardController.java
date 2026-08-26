package ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import services.DashboardService;
import utils.CurrencyFormatter;

import java.math.BigDecimal;
import java.util.Map;

import javafx.scene.control.Button;
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
import javafx.concurrent.Task;

public class DashboardController {

    @FXML private Label lblTotalBalance;
    @FXML private Label lblSavingsRate;
    @FXML private Label lblBudgetRemaining;
    @FXML private FlowPane dynamicWidgetsContainer;
    @FXML private Button btnRefresh;
    @FXML private Label lblSyncStatus;

    private final DashboardService dashboardService;
    private SynchronizationService syncService;

    public DashboardController() {
        this.dashboardService = new DashboardService();
    }

    @FXML
    public void initialize() {
        initializeServices();
    }

    private void initializeServices() {
        try {
            ApiConfiguration apiConfig = new ApiConfiguration();
            
            if (apiConfig.getApiKey() == null || apiConfig.getApiKey().isBlank()) {
                Platform.runLater(() -> {
                    javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
                    dialog.setTitle("API Key Required");
                    dialog.setHeaderText("Welcome to Wallet Dashboard!\nTo sync your data, please provide your BudgetBakers API key.");
                    dialog.setContentText("API Key:");
                    
                    java.util.Optional<String> result = dialog.showAndWait();
                    if (result.isPresent() && !result.get().isBlank()) {
                        apiConfig.saveApiKey(result.get());
                        setupServices(apiConfig);
                        loadData();
                    } else {
                        if (lblSyncStatus != null) lblSyncStatus.setText("API Key missing. Operating offline.");
                        fetchAndDisplayData();
                    }
                });
            } else {
                setupServices(apiConfig);
                loadData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fetchAndDisplayData();
        }
    }

    private void setupServices(ApiConfiguration apiConfig) {
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
        
        this.syncService = new SynchronizationService(
            apiClient, categoryRepo, budgetRepo, incrementalSync
        );
    }

    @FXML
    public void loadData() {
        if (syncService != null && btnRefresh != null) {
            btnRefresh.setDisable(true);
            lblSyncStatus.setText("Syncing...");
            
            Task<Void> syncTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    syncService.syncAll();
                    return null;
                }
            };
            
            syncTask.setOnSucceeded(e -> {
                lblSyncStatus.setText("Sync successful. Loading data...");
                fetchAndDisplayData();
            });
            
            syncTask.setOnFailed(e -> {
                btnRefresh.setDisable(false);
                lblSyncStatus.setText("Sync failed.");
                fetchAndDisplayData(); // Load data anyway
            });
            
            new Thread(syncTask).start();
        } else {
            fetchAndDisplayData();
        }
    }
    
    private void fetchAndDisplayData() {
        // Run database queries on a background thread
        new Thread(() -> {
            try {
                BigDecimal totalBalance = dashboardService.calculateTotalBalance();
                String savingsRate = dashboardService.getMonthlySavingsRate();
                Map<String, BigDecimal> balancesByType = dashboardService.getBalancesByType();
                
                Platform.runLater(() -> {
                    lblTotalBalance.setText(CurrencyFormatter.format(totalBalance, "EUR"));
                    lblSavingsRate.setText(savingsRate);
                    lblBudgetRemaining.setText("WIP"); // Budget logic will come in Phase 4.5
                    
                    dynamicWidgetsContainer.getChildren().clear();
                    
                    // Add dynamic widgets
                    for (Map.Entry<String, BigDecimal> entry : balancesByType.entrySet()) {
                        String type = entry.getKey();
                        BigDecimal balance = entry.getValue();
                        
                        VBox widget = new VBox();
                        widget.getStyleClass().add("widget-card");
                        widget.setMinWidth(200);
                        
                        Label titleLabel = new Label(formatAccountTypeTitle(type));
                        titleLabel.getStyleClass().add("widget-title");
                        
                        Label balanceLabel = new Label(CurrencyFormatter.format(balance, "EUR"));
                        balanceLabel.getStyleClass().add("widget-value");
                        
                        if (balance.compareTo(BigDecimal.ZERO) < 0 || type.toLowerCase().contains("debt") || type.toLowerCase().contains("loan") || type.toLowerCase().contains("credit")) {
                            balanceLabel.getStyleClass().add("widget-negative");
                        }
                        
                        widget.getChildren().addAll(titleLabel, balanceLabel);
                        dynamicWidgetsContainer.getChildren().add(widget);
                    }
                    
                    if (btnRefresh != null) {
                        btnRefresh.setDisable(false);
                    }
                    if (lblSyncStatus != null) {
                        lblSyncStatus.setText("");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblTotalBalance.setText("Error");
                    if (btnRefresh != null) btnRefresh.setDisable(false);
                    if (lblSyncStatus != null) lblSyncStatus.setText("Error loading data.");
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private String formatAccountTypeTitle(String type) {
        if (type == null) return "Unknown";
        // Prettify common BudgetBakers types
        return switch (type) {
            case "CurrentAccount" -> "Checking Balance";
            case "CreditCard" -> "Credit Card Debt";
            case "SavingsAccount" -> "Savings Balance";
            case "Cash" -> "Available Cash";
            case "Loan" -> "Outstanding Loans";
            case "Investment" -> "Investment Balance";
            default -> type + " Balance";
        };
    }
}
