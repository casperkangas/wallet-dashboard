# README.md

# Wallet Dashboard

A cross-platform desktop application that transforms data from the BudgetBakers Wallet API into a modern financial analytics dashboard.

The application synchronizes financial data from Wallet, stores it locally in SQLite, performs advanced analytics, and presents the results through an interactive JavaFX-based dashboard.

The project is designed to be:

- Local-first
- Privacy-focused
- Cross-platform
- Extensible
- Open-source
- AI-assisted during development

---

## Features

### Data synchronization

- Connect to the BudgetBakers Wallet API
- Synchronize accounts
- Synchronize transactions
- Synchronize categories
- Synchronize budgets
- Synchronize labels
- Incremental synchronization
- Manual synchronization
- Automatic synchronization

---

### Dashboard

- Net worth overview
- Cash flow overview
- Budget tracking
- Spending by category
- Spending trends
- Monthly reports
- Historical reports

---

### Analytics

- Savings rate
- Expense volatility
- Financial independence ratio
- Spending predictions
- Subscription detection
- Anomaly detection

---

### Storage

- SQLite local database
- Daily snapshots
- Historical data preservation
- Offline support

---

## Technology stack

| Component    | Technology      |
| ------------ | --------------- |
| Language     | Java 21         |
| UI           | JavaFX          |
| HTTP         | Java HttpClient |
| Database     | SQLite          |
| JSON         | Jackson         |
| Build system | Maven           |
| Testing      | JUnit           |
| Packaging    | jpackage        |
| CI/CD        | GitHub Actions  |

---

## Supported operating systems

- macOS
- Windows
- Linux

---

## Project structure

```text
wallet-dashboard/

├── docs/
│
├── screenshots/
│
├── src/
│
│   ├── main/
│   │
│   │   ├── java/
│   │   │
│   │   │   ├── api/
│   │   │   ├── analytics/
│   │   │   ├── database/
│   │   │   ├── forecasting/
│   │   │   ├── models/
│   │   │   ├── services/
│   │   │   ├── settings/
│   │   │   ├── sync/
│   │   │   ├── ui/
│   │   │   └── utils/
│   │
│   └── resources/
│
├── tests/
│
├── README.md
│
└── PROJECT_SPEC.md
```

---

## Development roadmap

### Version 0.1

- API integration

### Version 0.2

- SQLite implementation

### Version 0.3

- Synchronization engine

### Version 0.4

- Dashboard implementation

### Version 0.5

- Chart implementation

### Version 0.6

- Budget analytics

### Version 0.7

- Forecasting

### Version 0.8

- Investment tracking

### Version 0.9

- Packaging

### Version 1.0

- Public release

---

## Development philosophy

This project follows four principles:

1. API-first development
2. Local-first storage
3. Privacy-first architecture
4. Modular design

---

## License

MIT License
