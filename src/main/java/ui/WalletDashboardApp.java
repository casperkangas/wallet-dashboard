package ui;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import database.ConnectionFactory;
import database.DatabaseConfiguration;
import database.DatabaseManager;
import database.MigrationManager;

public class WalletDashboardApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize Database
            DatabaseConfiguration dbConfig = new DatabaseConfiguration();
            ConnectionFactory connectionFactory = new ConnectionFactory(dbConfig);
            MigrationManager migrationManager = new MigrationManager();
            DatabaseManager dbManager = new DatabaseManager(connectionFactory, migrationManager);
            dbManager.initialize();
            
            Parent root = ViewLoader.loadView("/fxml/MainLayout.fxml");
            Scene scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            // Set App Icon
            try {
                // JavaFX Window Icon (Windows/Linux)
                primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/app-icon.jpg")));
                
                // macOS Dock Icon (when running un-packaged)
                if (java.awt.Taskbar.isTaskbarSupported()) {
                    java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                    if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                        java.net.URL iconUrl = getClass().getResource("/images/app-icon.jpg");
                        if (iconUrl != null) {
                            java.awt.Image icon = java.awt.Toolkit.getDefaultToolkit().getImage(iconUrl);
                            taskbar.setIconImage(icon);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not set app icon: " + e.getMessage());
            }
            
            primaryStage.setTitle("Wallet Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
