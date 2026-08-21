## Goal Description
We will fix the sorting bug in the Transactions table, improve currency formatting to support Euros (and other native currencies), add Date and Category filters to the Transactions screen, and dynamically generate Dashboard widgets based on the types of accounts you actually own (e.g., Savings, Checking, Cash).

## Proposed Changes

### 1. Amount Sorting Bug & Currency Formatting
#### [MODIFY] src/main/java/ui/controllers/TransactionsController.java
- **Sorting Fix**: Change the `colAmount` to bind directly to the `BigDecimal` amount (for mathematically correct sorting) rather than the pre-formatted `String`.
- **Currency Formatting**: Add a custom `CellFactory` to the `colAmount` column that formats the `BigDecimal` into a localized currency string using the transaction's specific currency code (e.g., appending "€" for EUR, "$" for USD).

### 2. Transaction Filters (Date & Category)
#### [MODIFY] src/main/resources/fxml/Transactions.fxml
- Add two `DatePicker` controls (Start Date & End Date).
- Add a `ComboBox<String>` for Category filtering alongside the existing filters.

#### [MODIFY] src/main/java/ui/controllers/TransactionsController.java
- Wire up the `DatePicker` and `cmbCategory` to the `FilteredList` predicate.

### 3. Dynamic Dashboard Widgets & Currency
#### [MODIFY] src/main/java/services/DashboardService.java
- Create a new method `Map<String, BigDecimal> getBalancesByType()` that groups your accounts by `accountType` (e.g., `CurrentAccount`, `Savings`, `CreditCard`, `Cash`) and sums their balances.

#### [MODIFY] src/main/resources/fxml/Dashboard.fxml
- Remove the hardcoded "Investment" and "Debt" boxes.
- Replace them with a `FlowPane` (a responsive container that wraps items) called `dynamicWidgetsContainer` where we will inject widgets dynamically.

#### [MODIFY] src/main/java/ui/controllers/DashboardController.java
- Iterate over the `balancesByType` map from the service. For each account type (e.g., "Savings"), dynamically generate a UI card and add it to the `dynamicWidgetsContainer`. 
- Ensure all dashboard balances are formatted dynamically according to the user's currency (or fallback to € if mixed).

### 4. Documentation Updates
#### [MODIFY] .github/docs/IMPLEMENTATION_PLAN.md
- Ensure "Filtering by date range, category, account" is checked off in Phase 4.4 once complete.

## Verification Plan
1. **Transactions**: Verify that clicking the "Amount" header sorts numerically (e.g., 5.00 before 10.00). Verify Date and Category filters properly filter the table. Verify currencies show the correct symbol (e.g., €).
2. **Dashboard**: Verify that dynamic widgets appear for "CurrentAccount", "CreditCard", "Savings", etc., without showing empty widgets for account types you don't own.
