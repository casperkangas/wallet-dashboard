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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import models.Account;
import models.Category;
import models.DailySnapshot;
import models.MonthlyTrendRecord;
import models.Transaction;
import services.AnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AnalyticsController {

    @FXML private VBox cardSavingsRate;
    @FXML private VBox cardEmergencyFund;
    @FXML private VBox cardFiRatio;
    @FXML private VBox cardInvestments;
    @FXML private VBox cardVolatility;

    @FXML private Label lblSavingsRate;
    @FXML private Label lblEmergencyFund;
    @FXML private Label lblFiRatio;
    @FXML private Label lblInvestments;
    @FXML private Label lblVolatility;
    @FXML private Label lblPieChartTitle;
    
    @FXML private LineChart<String, Number> netWorthChart;
    @FXML private BarChart<String, Number> incomeExpenseChart;
    @FXML private PieChart categoryPieChart;

    private AnalyticsService analyticsService;
    private TransactionRepository transactionRepository;
    private DailySnapshotRepository dailySnapshotRepository;
    private CategoryRepository categoryRepository;
    private database.repositories.AccountRepository accountRepository;

    @FXML
    public void initialize() {
        ConnectionFactory connectionFactory = new ConnectionFactory(new DatabaseConfiguration());
        transactionRepository = new TransactionRepository(connectionFactory);
        dailySnapshotRepository = new DailySnapshotRepository(connectionFactory);
        categoryRepository = new CategoryRepository(connectionFactory);
        accountRepository = new database.repositories.AccountRepository(connectionFactory);
        analyticsService = new AnalyticsService(transactionRepository, dailySnapshotRepository);
        
        // Add explanatory tooltips to KPI cards
        Tooltip.install(cardSavingsRate, new Tooltip("Percentage of income saved this month. Formula: (Income - Expenses) / Income"));
        Tooltip.install(cardEmergencyFund, new Tooltip("Months of runway available. Formula: Cash / Average Monthly Expenses"));
        Tooltip.install(cardFiRatio, new Tooltip("Progress towards Financial Independence. Formula: Investments / Annualized Expenses"));
        Tooltip.install(cardInvestments, new Tooltip("Total money moved to investment categories this month."));
        Tooltip.install(cardVolatility, new Tooltip("How much your expenses fluctuate. Formula: Standard Deviation of expenses over the last 6 months"));
        
        loadDataAsync();
    }

    private void loadDataAsync() {
        Thread thread = new Thread(() -> {
            try {
                // Fetch data
                List<MonthlyTrendRecord> last6Months = transactionRepository.getMonthlyTrends(6);
                List<MonthlyTrendRecord> last3Months = transactionRepository.getMonthlyTrends(3);
                List<DailySnapshot> snapshots = dailySnapshotRepository.getHistoricalSnapshots(30); // Last 30 days
                
                // Fetch all categories to identify "Investments"
                List<Category> allCategories = categoryRepository.findAll();
                List<String> investmentCategoryIds = new ArrayList<>();
                for (Category c : allCategories) {
                    if ((c.name() != null && c.name().equalsIgnoreCase("Investments")) || 
                        (c.parentId() != null && c.parentId().equalsIgnoreCase("investments"))) {
                        investmentCategoryIds.add(c.id());
                    }
                }

                // Get current balances from AccountRepository
                List<Account> allAccounts = accountRepository.findAll();
                BigDecimal currentNetWorth = BigDecimal.ZERO;
                BigDecimal cash = BigDecimal.ZERO;
                BigDecimal investments = BigDecimal.ZERO;
                
                for (Account a : allAccounts) {
                    if (a.balance() != null) {
                        currentNetWorth = currentNetWorth.add(a.balance());
                        // Simplistic categorization based on name or type
                        String type = a.accountType() != null ? a.accountType().toLowerCase() : "";
                        String name = a.name() != null ? a.name().toLowerCase() : "";
                        if (type.contains("investment") || name.contains("invest") || name.contains("brokerage")) {
                            investments = investments.add(a.balance());
                        } else if (!type.contains("loan") && !type.contains("credit") && !name.contains("loan") && !name.contains("credit")) {
                            cash = cash.add(a.balance());
                        }
                    }
                }

                // Calculations
                BigDecimal currentMonthIncome = BigDecimal.ZERO;
                BigDecimal currentMonthExpenses = BigDecimal.ZERO;
                if (!last3Months.isEmpty()) {
                    MonthlyTrendRecord current = last3Months.get(last3Months.size() - 1);
                    currentMonthIncome = current.income();
                    currentMonthExpenses = current.expenses();
                }

                // Try to save a new daily snapshot today to build history
                try {
                    dailySnapshotRepository.save(new DailySnapshot(
                        LocalDate.now(),
                        currentNetWorth,
                        cash,
                        investments,
                        currentNetWorth.subtract(cash).subtract(investments).abs(), // estimate debt
                        currentMonthIncome,
                        currentMonthExpenses
                    ));
                } catch (Exception e) {}

                // Reload snapshots
                snapshots = dailySnapshotRepository.getHistoricalSnapshots(365); // Last year

                // Pie chart data and investment spending adjustment
                List<Transaction> allTransactions = transactionRepository.findAll();
                LocalDate now = LocalDate.now();
                Map<String, BigDecimal> categorySpending = new HashMap<>();
                BigDecimal totalSpending = BigDecimal.ZERO;
                BigDecimal investmentSpending = BigDecimal.ZERO;
                
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
                            
                            // Adjust for investments
                            if (investmentCategoryIds.contains(t.categoryId())) {
                                investmentSpending = investmentSpending.add(amt);
                            } else if (!categoryName.equalsIgnoreCase("Transfer") && !categoryName.equalsIgnoreCase("Transfers")) {
                                // Exclude Transfer categories and Investment categories explicitly from pie chart
                                categorySpending.put(categoryName, categorySpending.getOrDefault(categoryName, BigDecimal.ZERO).add(amt));
                                totalSpending = totalSpending.add(amt);
                            }
                        }
                    }
                }

                BigDecimal adjustedExpenses = currentMonthExpenses.subtract(investmentSpending);
                if (adjustedExpenses.compareTo(BigDecimal.ZERO) < 0) adjustedExpenses = BigDecimal.ZERO;
                
                BigDecimal savingsRate = analyticsService.calculateSavingsRate(currentMonthIncome, adjustedExpenses);
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
                
                // 1 point per month grouping
                List<MonthlyTrendRecord> last12Months = transactionRepository.getMonthlyTrends(12);
                BigDecimal runningNW = currentNetWorth;
                List<XYChart.Data<String, Number>> dataPoints = new ArrayList<>();
                
                // Map to quickly look up if a snapshot exists for a given YearMonth string
                Map<String, BigDecimal> snapshotMap = new HashMap<>();
                for (DailySnapshot s : snapshots) {
                    // This will naturally store the latest snapshot's value for the month if they are sorted ascending
                    String ym = s.snapshotDate().getYear() + "-" + String.format("%02d", s.snapshotDate().getMonthValue());
                    snapshotMap.put(ym, s.netWorth());
                }
                
                for (int i = last12Months.size() - 1; i >= 0; i--) {
                    MonthlyTrendRecord record = last12Months.get(i);
                    String ym = record.yearMonth();
                    
                    // If we have a snapshot for this month, use it. Otherwise, use reverse-engineered math.
                    if (snapshotMap.containsKey(ym)) {
                        runningNW = snapshotMap.get(ym);
                    }
                    
                    dataPoints.add(new XYChart.Data<>(ym, runningNW));
                    
                    // Step back for the next iteration (unless we overwrite it via snapshot in the next loop)
                    BigDecimal cashFlow = record.income().subtract(record.expenses());
                    runningNW = runningNW.subtract(cashFlow);
                }
                
                java.util.Collections.reverse(dataPoints); // Chronological
                nwSeries.getData().addAll(dataPoints);

                XYChart.Series<String, Number> cashFlowSeries = new XYChart.Series<>();
                cashFlowSeries.setName("Cash Flow");
                
                for (MonthlyTrendRecord record : last3Months) {
                    BigDecimal cashFlow = record.income().subtract(record.expenses());
                    cashFlowSeries.getData().add(new XYChart.Data<>(record.yearMonth(), cashFlow));
                }

                // Update UI on JavaFX application thread
                final BigDecimal finalTotalSpending = totalSpending;
                final BigDecimal finalInvestmentSpending = investmentSpending;
                Platform.runLater(() -> {
                    lblSavingsRate.setText(String.format("%.1f%%", savingsRate.doubleValue()));
                    lblEmergencyFund.setText(String.format("%.1f Months", emergencyFundRatio.doubleValue()));
                    lblFiRatio.setText(String.format("%.1f%%", fiRatio.doubleValue()));
                    lblInvestments.setText(String.format("$%.2f", finalInvestmentSpending.doubleValue()));
                    lblVolatility.setText(String.format("$%.2f", volatility.doubleValue()));
                    
                    netWorthChart.getData().clear();
                    netWorthChart.getData().add(nwSeries);
                    
                    incomeExpenseChart.getData().clear();
                    incomeExpenseChart.getData().add(cashFlowSeries);
                    
                    // Color bars green if positive, red if negative
                    for (XYChart.Data<String, Number> data : cashFlowSeries.getData()) {
                        javafx.scene.Node node = data.getNode();
                        if (node != null) {
                            if (data.getYValue().doubleValue() < 0) {
                                node.setStyle("-fx-bar-fill: #F44336;");
                            } else {
                                node.setStyle("-fx-bar-fill: #4CAF50;");
                            }
                        }
                    }
                    
                    lblPieChartTitle.setText(String.format("Spending by Category (Current Month) - Total: $%.2f", finalTotalSpending.doubleValue()));
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
