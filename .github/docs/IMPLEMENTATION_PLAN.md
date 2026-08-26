# Implementation Plan

## Phase 1 — API Integration

- [x] API configuration (`.env` loading, base URL)
- [x] API authentication (Bearer token via `AuthenticationService`)
- [x] HTTP client (`WalletApiClient` with GET support)
- [x] API error handling (`ApiException` with status codes)
- [x] DTO models (`Account`, `Transaction`, `Category`, `Budget`, `DailySnapshot` as Java records)
- [x] JSON parsing (Jackson with `JavaTimeModule`, `FAIL_ON_UNKNOWN_PROPERTIES` disabled)
- [x] Unit tests (`ApiConfigurationTest`, `AuthenticationServiceTest`, `JsonParsingTest`, `WalletApiClientTest`)

---

## Phase 2 — Database

- [x] SQLite setup (`ConnectionFactory`, `DatabaseConfiguration` with OS-specific paths)
- [x] Database migrations (`MigrationManager` with inline DDL for 5 tables)
- [x] Repository layer (`AccountRepository`, `TransactionRepository`, `CategoryRepository`, `BudgetRepository`, `DailySnapshotRepository`)
- [x] Batch save with transactions (`saveAll()` with rollback support)
- [x] Unit tests (`DatabaseSetupTest`, `RepositoryTest`)

---

## Phase 3 — Synchronization

- [x] Synchronization service (`SynchronizationService` — full sync for accounts, transactions, categories, budgets)
- [x] Incremental synchronization (`IncrementalSynchronizer` — timestamp-based sync for accounts and transactions)
- [x] Synchronization scheduler (`SynchronizationScheduler` — manual, automatic, startup modes)
- [x] Synchronization error handling (`SynchronizationException`)
- [x] Unit tests (`SynchronizationServiceTest`, `IncrementalSynchronizerTest`, `SynchronizationSchedulerTest`)

---

## Phase 3.5 — Technical Debt (Pre-Dashboard)

> Items discovered during code review that should be addressed before or during Phase 4.

### Critical

- [x] Fix `REAL` storage for monetary values — store as `TEXT` to preserve `BigDecimal` precision
- [x] Add schema versioning to `MigrationManager` (version tracking table, incremental migration support)
- [x] Fix resource leak in `ApiConfiguration.loadApiKey()` — use try-with-resources for `InputStream`
- [x] Fix incremental sync logic (`updatedAt` filtering for records and full sync for accounts to fix stale dashboard balances)

### Important

- [ ] Add foreign key constraints to schema (`transactions.account_id`, `transactions.category_id`, `budgets.category_id`, `categories.parent_id`)
- [ ] Enable `PRAGMA foreign_keys = ON` in `ConnectionFactory`
- [ ] Add database indexes on frequently queried columns (`transactions.account_id`, `transactions.transaction_date`, `budgets.category_id`)
- [x] Add null/blank validation in `ApiConfiguration(String, String)` constructor
- [ ] Replace `INSERT OR REPLACE` with `INSERT ... ON CONFLICT DO UPDATE` (safer with foreign keys)

### Nice to Have

- [ ] Extract environment loading from `ApiConfiguration` into a dedicated config loader (SRP)
- [ ] Add repository interfaces for dependency inversion
- [ ] Add missing repository methods (`deleteById`, `count`, `existsById`)
- [ ] Expand repository test coverage (Budget, Transaction, DailySnapshot repositories untested)
- [ ] Add `TypeReference` overload to `WalletApiClient.get()` for generic collection deserialization

---

## Phase 4 — Dashboard

### 4.1 — JavaFX Setup

- [x] Add JavaFX dependencies to `pom.xml`
- [x] Configure `module-info.java` (if using Java modules) or add JavaFX Maven plugin
- [x] Create main application class (`WalletDashboardApp extends Application`)
- [x] Create application entry point with `Launcher` class (for classpath compatibility)
- [x] Set up FXML loading infrastructure
- [x] Add CSS stylesheet for consistent theming

### 4.2 — Navigation & Layout

- [x] Create main window layout with sidebar navigation
- [x] Create navigation controller for screen switching
- [x] Define FXML views for each screen (Dashboard, Transactions, Budgets, Analytics)
- [x] Implement screen transition logic

### 4.3 — Dashboard Screen

- [x] Net worth widget
- [x] Cash balance widget
- [x] Investment balance widget
- [x] Debt widget
- [x] Savings rate widget
- [x] Budget remaining widget
- [x] Dashboard data service (bridge between repositories and UI)

### 4.4 — Transactions Screen

- [x] Transaction list view with table
- [x] Search functionality
- [x] Filtering by date range, category, account
- [x] Sorting by date, amount, category

### 4.5 — Budget Screen

- [ ] Category budget list
- [ ] Spending progress bars
- [ ] Budget vs actual comparison

### 4.6 — Synchronization UI

- [x] Sync status indicator in the UI
- [x] Manual sync button
- [x] Last synced timestamp display
- [x] Sync progress feedback

---

## Phase 5 — Charts

- [ ] Net worth over time (line chart)
- [ ] Monthly expenses (bar chart)
- [ ] Monthly income (bar chart)
- [ ] Spending by category (pie chart)
- [ ] Spending heatmap

---

## Phase 6 — Analytics

- [ ] Analytics engine service
- [ ] Savings rate calculation
- [ ] Financial independence ratio calculation
- [ ] Expense volatility calculation (standard deviation)
- [ ] Emergency fund ratio calculation
- [ ] Analytics dashboard screen integration

---

## Phase 7 — Forecasting (v2.0)

- [ ] Spending prediction
- [ ] Subscription detection
- [ ] Anomaly detection

> Note: Forecasting is disabled in v1.0 per PROJECT_SPEC.md.

---

## Phase 8 — Packaging

- [ ] Configure `jpackage` via Maven plugin
- [ ] macOS `.dmg` output
- [ ] Windows `.exe` output
- [ ] Linux `.deb` output
- [ ] GitHub Actions CI/CD pipeline for automated builds

---

## Phase 9 — Polish & Release

- [ ] Error handling and user-facing error messages
- [ ] Application settings screen
- [ ] Unit test coverage to ≥80%
- [ ] README screenshots and documentation
- [ ] Version 1.0 release
