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
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
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
