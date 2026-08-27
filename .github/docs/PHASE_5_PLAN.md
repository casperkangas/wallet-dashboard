# Phase 5: Analytics & Charts Plan

This document outlines the technical strategy for Phase 5 of the Wallet Dashboard project, focusing on building a robust analytics engine and visualizing financial data through interactive charts.

This plan consolidates both Phase 5 (Charts) and Phase 6 (Analytics) from the original `PROJECT_SPEC.md` into a single cohesive "Analytics" tab, as the charts natively depend on the analytical metrics.

## 1. Goal

Enable the currently disabled "Analytics" tab in the sidebar and populate it with a dynamic, data-rich view. We will implement a new `AnalyticsService` to perform complex calculations (Savings Rate, Expense Volatility, etc.) and use JavaFX Chart components to visualize historical trends.

## 2. Target Features

### Analytics Engine (`AnalyticsService.java`)

- **Savings Rate:** `(monthly income - monthly expenses) / monthly income`
- **Emergency Fund Ratio:** `cash / monthly expenses` (Months of runway)
- **Financial Independence Ratio:** `investments / annual expenses`
- **Expense Volatility:** Standard deviation of monthly expenses over the past 6 months to determine spending consistency.

### Data Visualization (`Analytics.fxml`)

- **Net Worth Over Time:** An interactive `LineChart` showing the trajectory of the user's total net worth over the past year.
- **Monthly Income vs. Expenses:** A grouped `BarChart` comparing inflow and outflow month-by-month.
- **Spending by Category:** A `PieChart` breaking down the current month's expenses.
- **Metric Cards:** Clean widgets across the top of the screen displaying the calculated KPIs from the Analytics Engine.

## 3. Architecture & Data Flow

1. **Database Layer:**
   - Ensure the `TransactionRepository` and `DailySnapshotRepository` have methods to aggregate data by month (`GROUP BY strftime('%Y-%m', transaction_date)`).
2. **Service Layer:**
   - Create `AnalyticsService` to query the database, perform standard deviation math, and build robust DTOs (e.g., `MonthlyTrendRecord`).
3. **UI Layer:**
   - Create `AnalyticsController` to asynchronously request these DTOs from `AnalyticsService`.
   - Use `Platform.runLater()` to inject the data into JavaFX `XYChart.Series` and `PieChart.Data` collections.

## 4. Implementation Steps for the Next Session

### Step 1: Analytics Service Foundation

- Scaffold `AnalyticsService.java`.
- Implement aggregation queries in repositories to fetch historical net worth and grouped monthly transactions.
- Write unit tests for `AnalyticsService` to ensure standard deviation and ratio maths are perfectly accurate, guarding against divide-by-zero errors.

### Step 2: The Analytics View

- Create `Analytics.fxml` utilizing a responsive grid layout.
- Add summary KPI cards at the top.
- Add the JavaFX `<LineChart>`, `<BarChart>`, and `<PieChart>` components into designated sections.

### Step 3: Controller & Integration

- Wire up `AnalyticsController.java`.
- Enable the Analytics button in `MainLayout.fxml` and map it to `showAnalytics()` in `MainController`.
- Apply CSS styling (`style.css`) to match the charts with the application's clean, modern aesthetic (custom chart colors, hidden chart backgrounds, rounded tooltips).

## 5. Potential Roadblocks & Considerations

- **Historical Accuracy:** BudgetBakers API only fetches the last 90 days by default unless explicitly overridden. If the user hasn't forced a deep historical sync, the "Net Worth Over Time" line chart might be short. We will design the charts to dynamically scale to whatever data is available.
- **JavaFX Chart Styling:** JavaFX default charts are notoriously "corporate" looking. Significant CSS overrides will be required to make the charts look as modern as the rest of the dashboard.
