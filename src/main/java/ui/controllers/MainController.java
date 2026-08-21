package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import ui.ViewLoader;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnSync;
    
    @FXML private Button btnTransactions;
    
    private Parent dashboardView;
    private Parent syncView;
    private Parent transactionsView;
    
    @FXML
    public void initialize() {
        // Pre-load views
        dashboardView = ViewLoader.loadView("/fxml/Dashboard.fxml");
        syncView = ViewLoader.loadView("/fxml/SyncView.fxml");
        transactionsView = ViewLoader.loadView("/fxml/Transactions.fxml");
        
        // Show default
        showDashboard();
    }
    
    @FXML
    private void showDashboard() {
        setActiveButton(btnDashboard);
        contentArea.getChildren().setAll(dashboardView);
    }
    
    @FXML
    private void showSync() {
        setActiveButton(btnSync);
        contentArea.getChildren().setAll(syncView);
    }
    
    @FXML
    private void showTransactions() {
        setActiveButton(btnTransactions);
        contentArea.getChildren().setAll(transactionsView);
    }
    
    private void setActiveButton(Button activeButton) {
        btnDashboard.getStyleClass().remove("active");
        btnSync.getStyleClass().remove("active");
        btnTransactions.getStyleClass().remove("active");
        activeButton.getStyleClass().add("active");
    }
}
