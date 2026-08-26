package ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import services.BudgetService;
import utils.CurrencyFormatter;

import java.util.List;

public class BudgetsController {

    @FXML private FlowPane activeContainer;
    @FXML private FlowPane archivedContainer;
    
    private final BudgetService budgetService;

    public BudgetsController() {
        this.budgetService = new BudgetService();
    }

    @FXML
    public void initialize() {
        loadData();
    }

    public void loadData() {
        new Thread(() -> {
            List<BudgetService.BudgetProgress> progressList = budgetService.getBudgetProgressForCurrentMonth();
            
            Platform.runLater(() -> {
                activeContainer.getChildren().clear();
                archivedContainer.getChildren().clear();
                
                for (BudgetService.BudgetProgress progress : progressList) {
                    VBox card = new VBox(10);
                    card.getStyleClass().add("widget-card");
                    card.setMinWidth(300);
                    
                    Label titleLabel = new Label(progress.budgetName());
                    titleLabel.getStyleClass().add("widget-title");
                    
                    ProgressBar progressBar = new ProgressBar(progress.progressPercentage());
                    progressBar.setMaxWidth(Double.MAX_VALUE);
                    progressBar.setMinHeight(20);
                    
                    if (progress.progressPercentage() > 1.0) {
                        progressBar.getStyleClass().add("over-budget");
                    }
                    
                    String formattedSpent = CurrencyFormatter.format(progress.spentAmount(), "EUR");
                    String formattedLimit = CurrencyFormatter.format(progress.limitAmount(), "EUR");
                    int percentInt = (int) (progress.progressPercentage() * 100);
                    
                    Label detailLabel = new Label(String.format("%s / %s (%d%%)", formattedSpent, formattedLimit, percentInt));
                    detailLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 12px;");
                    
                    card.getChildren().addAll(titleLabel, progressBar, detailLabel);
                    
                    if (progress.isClosed()) {
                        archivedContainer.getChildren().add(card);
                    } else {
                        activeContainer.getChildren().add(card);
                    }
                }
            });
        }).start();
    }
}
