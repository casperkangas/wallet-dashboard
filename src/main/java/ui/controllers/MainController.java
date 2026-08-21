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
    
    private Parent dashboardView;
    private Parent syncView;
    
    @FXML
    public void initialize() {
        // Pre-load views
        dashboardView = ViewLoader.loadView("/fxml/Dashboard.fxml");
        syncView = ViewLoader.loadView("/fxml/SyncView.fxml");
        
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
    
    private void setActiveButton(Button activeButton) {
        btnDashboard.getStyleClass().remove("active");
        btnSync.getStyleClass().remove("active");
        activeButton.getStyleClass().add("active");
    }
}
