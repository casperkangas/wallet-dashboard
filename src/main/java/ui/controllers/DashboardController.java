package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import services.DashboardService;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardController {

    @FXML private Label lblTotalBalance;
    @FXML private Label lblAccountCount;
    
    private final DashboardService dashboardService;
    
    public DashboardController() {
        this.dashboardService = new DashboardService();
    }
    
    @FXML
    public void initialize() {
        loadData();
    }
    
    public void loadData() {
        try {
            BigDecimal totalBalance = dashboardService.calculateTotalBalance();
            int accountCount = dashboardService.getAccountCount();
            
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US); // Adjust as needed
            lblTotalBalance.setText(currencyFormat.format(totalBalance));
            lblAccountCount.setText(String.valueOf(accountCount));
            
        } catch (Exception e) {
            lblTotalBalance.setText("Error");
            lblAccountCount.setText("-");
            e.printStackTrace();
        }
    }
}
