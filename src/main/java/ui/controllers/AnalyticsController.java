package ui.controllers;

import database.ConnectionFactory;
import database.DatabaseConfiguration;
import database.repositories.DailySnapshotRepository;
import database.repositories.TransactionRepository;
import database.repositories.CategoryRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import models.DailySnapshot;
import models.MonthlyTrendRecord;
import models.Transaction;
import models.Category;
import services.AnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnalyticsController {

    @FXML private Label lblSavingsRate;
    @FXML private Label lblEmergencyFund;
    @FXML private Label lblFiRatio;
    @FXML private Label lblVolatility;
    
    @FXML private LineChart<String, Number> netWorthChart;
    @FXML private BarChart<String, Number> incomeExpenseChart;
    @FXML private PieChart categoryPieChart;

    private AnalyticsService analyticsService;
    private TransactionRepository transactionRepository;
    private DailySnapshotRepository dailySnapshotRepository;
    private CategoryRepository categoryRepository;

    @FXML
    public void initialize() {
        ConnectionFactory connectionFactory = new ConnectionFactory(new DatabaseConfiguration());
        transactionRepository = new TransactionRepository(connectionFactory);
        dailySnapshotRepository = new DailySnapshotRepository(connectionFactory);
        categoryRepository = new CategoryRepository(connectionFactory);
        analyticsService = new AnalyticsService(transactionRepository, dailySnapshotRepository);
        
        loadDataAsync();
    }

    private void loadDataAsync() {
        Thread thread = new Thread(() -> {
            try {
                // Fetch data
                List<MonthlyTrendRecord> last6Months = transactionRepository.getMonthlyTrends(6);
                List<MonthlyTrendRecord> last3Months = transactionRepository.getMonthlyTrends(3);
                List<DailySnapshot> snapshots = dailySnapshotRepository.getHistoricalSnapshots(30); // Last 30 days
                
                // Get cash and investments from the latest snapshot
                BigDecimal cash = BigDecimal.ZERO;
                BigDecimal investments = BigDecimal.ZERO;
                if (!snapshots.isEmpty()) {
                    DailySnapshot latest = snapshots.get(snapshots.size() - 1);
                    cash = latest.cash() != null ? latest.cash() : BigDecimal.ZERO;
                    investments = latest.investments() != null ? latest.investments() : BigDecimal.ZERO;
                }

                // Calculations
                BigDecimal currentMonthIncome = BigDecimal.ZERO;
                BigDecimal currentMonthExpenses = BigDecimal.ZERO;
                if (!last3Months.isEmpty()) {
                    MonthlyTrendRecord current = last3Months.get(last3Months.size() - 1);
                    currentMonthIncome = current.income();
                    currentMonthExpenses = current.expenses();
                }

                BigDecimal savingsRate = analyticsService.calculateSavingsRate(currentMonthIncome, currentMonthExpenses);
                BigDecimal emergencyFundRatio = analyticsService.calculateEmergencyFundRatio(cash, last6Months); // Use 6 months for averages
                
                // Estimate annual expenses
                BigDecimal annualExpenses = BigDecimal.ZERO;
                if (!last6Months.isEmpty()) {
                    BigDecimal total6MoExp = last6Months.stream().map(MonthlyTrendRecord::expenses).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal avgMoExp = total6MoExp.divide(new BigDecimal(last6Months.size()), 2, java.math.RoundingMode.HALF_UP);
                    annualExpenses = avgMoExp.multiply(new BigDecimal(12));
                }
                BigDecimal fiRatio = analyticsService.calculateFinancialIndependenceRatio(investments, annualExpenses);
                
                BigDecimal volatility = analyticsService.calculateExpenseVolatility(last6Months);
                
                // Charts Data
                XYChart.Series<String, Number> nwSeries = new XYChart.Series<>();
                nwSeries.setName("Net Worth");
                for (DailySnapshot snapshot : snapshots) {
                    nwSeries.getData().add(new XYChart.Data<>(snapshot.snapshotDate().toString(), snapshot.netWorth()));
                }

                XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
                incomeSeries.setName("Income");
                XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
                expenseSeries.setName("Expenses");
                
                for (MonthlyTrendRecord record : last3Months) { // 3 months as per user request
                    incomeSeries.getData().add(new XYChart.Data<>(record.yearMonth(), record.income()));
                    expenseSeries.getData().add(new XYChart.Data<>(record.yearMonth(), record.expenses()));
                }
                
                // Pie chart data
                List<Transaction> allTransactions = transactionRepository.findAll();
                LocalDate now = LocalDate.now();
                Map<String, BigDecimal> categorySpending = new HashMap<>();
                
                for (Transaction t : allTransactions) {
                    if (t.transactionDate() != null && t.transactionDate().getYear() == now.getYear() && t.transactionDate().getMonthValue() == now.getMonthValue()) {
                        if (!t.isTransfer() && t.amount().compareTo(BigDecimal.ZERO) < 0) {
                            String categoryName = "Unknown";
                            if (t.categoryId() != null && !t.categoryId().isEmpty()) {
                                try {
                                    Optional<Category> catOpt = categoryRepository.findById(t.categoryId());
                                    if (catOpt.isPresent()) {
                                        categoryName = catOpt.get().name();
                                    }
                                } catch (Exception e) {}
                            }
                            BigDecimal amt = t.amount().abs();
                            categorySpending.put(categoryName, categorySpending.getOrDefault(categoryName, BigDecimal.ZERO).add(amt));
                        }
                    }
                }

                // Update UI on JavaFX application thread
                Platform.runLater(() -> {
                    lblSavingsRate.setText(String.format("%.1f%%", savingsRate.doubleValue()));
                    lblEmergencyFund.setText(String.format("%.1f Months", emergencyFundRatio.doubleValue()));
                    lblFiRatio.setText(String.format("%.1f%%", fiRatio.doubleValue()));
                    lblVolatility.setText(String.format("$%.2f", volatility.doubleValue()));
                    
                    netWorthChart.getData().clear();
                    netWorthChart.getData().add(nwSeries);
                    
                    incomeExpenseChart.getData().clear();
                    incomeExpenseChart.getData().addAll(incomeSeries, expenseSeries);
                    
                    categoryPieChart.getData().clear();
                    for (Map.Entry<String, BigDecimal> entry : categorySpending.entrySet()) {
                        categoryPieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
