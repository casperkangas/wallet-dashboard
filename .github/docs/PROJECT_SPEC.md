# PROJECT_SPEC.md

# Wallet Dashboard - Technical Specification

Version: 1.0

---

# 1. Project objective

Create a cross-platform desktop application that uses the BudgetBakers Wallet API to provide advanced financial analytics unavailable in the official Wallet application.

The application must run on:

- macOS
- Windows
- Linux

The application must be distributable as a native desktop application.

---

# 2. Architecture

```text
Wallet API
      ↓
Synchronization Layer
      ↓
SQLite Database
      ↓
Analytics Engine
      ↓
Forecasting Engine
      ↓
JavaFX Dashboard
```

---

# 3. Development rules

The codebase must follow these principles:

- SOLID principles
- Separation of concerns
- Single responsibility principle
- Dependency injection
- Immutable models whenever possible

Avoid:

- Business logic inside UI controllers
- Database queries inside UI components
- Large classes
- Static global state

---

# 4. Technology requirements

## Language

Java 21

---

## Build system

Maven

---

## GUI

JavaFX

---

## Database

SQLite

---

## HTTP

Java HttpClient

---

## JSON serialization

Jackson

---

## Testing

JUnit

---

# 5. API module

Package:

```text
api/
```

Required classes:

```text
WalletApiClient
AuthenticationService
ApiException
ApiConfiguration
```

Responsibilities:

- Authentication
- Request execution
- Error handling
- Response parsing

The UI must never directly communicate with the API.

All API requests must pass through the API module.

---

# 6. Database module

Package:

```text
database/
```

Required classes:

```text
DatabaseManager
ConnectionFactory
MigrationManager
```

---

# 7. Database schema

## Accounts

```sql
accounts

id
name
currency
balance
institution
updated_at
```

---

## Transactions

```sql
transactions

id
account_id
category_id
amount
currency
transaction_date
description
payment_method
is_transfer
created_at
```

---

## Categories

```sql
categories

id
name
parent_id
icon
```

---

## Budgets

```sql
budgets

id
category_id
limit_amount
period
```

---

## Daily snapshots

```sql
daily_snapshots

snapshot_date
net_worth
cash
investments
debt
monthly_income
monthly_expenses
```

---

# 8. Synchronization module

Package:

```text
sync/
```

Required classes:

```text
SynchronizationService
SynchronizationScheduler
IncrementalSynchronizer
```

Synchronization modes:

- Manual
- Automatic
- Startup

Synchronization must use timestamps to avoid unnecessary API requests.

---

# 9. Analytics module

Package:

```text
analytics/
```

Required metrics:

## Savings rate

```text
(monthly income - monthly expenses) / monthly income
```

---

## Financial independence ratio

```text
investments / annual expenses
```

---

## Expense volatility

```text
standard deviation of monthly expenses
```

---

## Emergency fund ratio

```text
cash / monthly expenses
```

---

# 10. Forecasting module

Package:

```text
forecasting/
```

Version 1.0:

- Disabled

Version 2.0:

- Spending prediction
- Subscription detection
- Anomaly detection

---

# 11. User interface

Package:

```text
ui/
```

Required screens:

---

## Dashboard screen

Widgets:

- Net worth
- Cash balance
- Investment balance
- Debt
- Savings rate
- Budget remaining

---

## Transactions screen

Features:

- Search
- Filtering
- Sorting

---

## Budget screen

Features:

- Category budgets
- Spending progress

---

## Analytics screen

Features:

- Charts
- Trends
- Financial metrics

---

# 12. Chart requirements

Required charts:

- Net worth over time
- Monthly expenses
- Monthly income
- Spending by category
- Spending heatmap

---

# 13. Packaging

Use:

```text
jpackage
```

Required outputs:

macOS:

```text
.dmg
```

Windows:

```text
.exe
```

Linux:

```text
.deb
```

---

# 14. Testing requirements

Every service must have unit tests.

Minimum coverage:

```text
80%
```

---

# 15. AI development instructions

The AI assistant must:

- Never invent architecture
- Never move files into different packages
- Never introduce additional frameworks
- Never replace JavaFX
- Never replace SQLite
- Never modify the database schema without approval
- Never modify the project structure without approval

All generated code must follow PROJECT_SPEC.md exactly.

When uncertain, the AI must ask for clarification instead of making assumptions.
