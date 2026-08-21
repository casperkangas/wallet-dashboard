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

public class DashboardController {

    @FXML private Label lblTotalBalance;
    @FXML private Label lblSavingsRate;
    @FXML private Label lblBudgetRemaining;
    @FXML private FlowPane dynamicWidgetsContainer;

    private final DashboardService dashboardService;

    public DashboardController() {
        this.dashboardService = new DashboardService();
    }

    @FXML
    public void initialize() {
        loadData();
    }

    @FXML
    public void loadData() {
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
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblTotalBalance.setText("Error");
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
