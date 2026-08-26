package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import ui.ViewLoader;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnTransactions;
    @FXML private Button btnBudgets;
    @FXML private Button btnSettings;
    
    private Parent dashboardView;
    private Parent transactionsView;
    private Parent budgetsView;
    private Parent settingsView;
    
    @FXML
    public void initialize() {
        // Show default
        showDashboard();
    }
    
    @FXML
    private void showDashboard() {
        setActiveButton(btnDashboard);
        dashboardView = ViewLoader.loadView("/fxml/Dashboard.fxml");
        contentArea.getChildren().setAll(dashboardView);
    }
    
    @FXML
    private void showTransactions() {
        setActiveButton(btnTransactions);
        transactionsView = ViewLoader.loadView("/fxml/Transactions.fxml");
        contentArea.getChildren().setAll(transactionsView);
    }
    
    @FXML
    private void showBudgets() {
        setActiveButton(btnBudgets);
        budgetsView = ViewLoader.loadView("/fxml/Budgets.fxml");
        contentArea.getChildren().setAll(budgetsView);
    }
    
    @FXML
    private void showSettings() {
        setActiveButton(btnSettings);
        settingsView = ViewLoader.loadView("/fxml/Settings.fxml");
        contentArea.getChildren().setAll(settingsView);
    }
    
    private void setActiveButton(Button activeButton) {
        btnDashboard.getStyleClass().remove("active");
        btnTransactions.getStyleClass().remove("active");
        btnBudgets.getStyleClass().remove("active");
        btnSettings.getStyleClass().remove("active");
        activeButton.getStyleClass().add("active");
    }
}
