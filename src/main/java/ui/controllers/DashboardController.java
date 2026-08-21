package ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import services.DashboardService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardController {

    @FXML private Label lblTotalBalance;
    @FXML private Label lblInvestmentBalance;
    @FXML private Label lblDebtBalance;
    @FXML private Label lblSavingsRate;
    @FXML private Label lblAccountCount;
    @FXML private Label lblBudgetRemaining;

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
                BigDecimal investmentBalance = dashboardService.getInvestmentBalance();
                BigDecimal debtBalance = dashboardService.getDebtBalance();
                String savingsRate = dashboardService.getMonthlySavingsRate();
                int accountCount = dashboardService.getAccountCount();
                
                // Format balances
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

                Platform.runLater(() -> {
                    lblTotalBalance.setText(currencyFormat.format(totalBalance));
                    lblInvestmentBalance.setText(currencyFormat.format(investmentBalance));
                    lblDebtBalance.setText(currencyFormat.format(debtBalance));
                    lblSavingsRate.setText(savingsRate);
                    lblAccountCount.setText(String.valueOf(accountCount));
                    lblBudgetRemaining.setText("WIP"); // Budget logic will come in Phase 4.5
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblTotalBalance.setText("Error");
                    lblAccountCount.setText("-");
                });
                e.printStackTrace();
            }
        }).start();
    }
}
