package ui.controllers;

import api.ApiConfiguration;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class SettingsController {

    @FXML private PasswordField txtApiKey;
    @FXML private Label lblStatus;

    private final ApiConfiguration apiConfig;

    public SettingsController() {
        this.apiConfig = new ApiConfiguration();
    }

    @FXML
    public void initialize() {
        // Pre-fill if exists, but since it's a PasswordField, it will show as bullets.
        if (apiConfig.getApiKey() != null) {
            txtApiKey.setText(apiConfig.getApiKey());
        }
    }

    @FXML
    public void saveKey() {
        String newKey = txtApiKey.getText();
        
        if (newKey == null || newKey.isBlank()) {
            lblStatus.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;"); // Red
            lblStatus.setText("Error: API key cannot be empty.");
            return;
        }
        
        try {
            apiConfig.saveApiKey(newKey);
            lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;"); // Green
            lblStatus.setText("Success! Please restart the app for the new key to take effect.");
        } catch (Exception e) {
            lblStatus.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;"); // Red
            lblStatus.setText("Error: Could not save API key.");
            e.printStackTrace();
        }
    }
}
