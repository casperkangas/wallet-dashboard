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

## Setup and Installation

1. Launch the application.
2. The application will detect if you are a first-time user.
3. You will be prompted to securely enter your **BudgetBakers API Key**. (You can generate one from your BudgetBakers web dashboard).
4. The key will be securely saved to your operating system's native secure preferences.
5. The application will perform its initial synchronization and build your dashboard!

---

## Project structure

```text
wallet-dashboard/

├── .github/
│   └── docs/                 # Contains Technical Specs and Implementation Plans
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── api/
│   │   │   ├── database/
│   │   │   ├── models/
│   │   │   ├── services/
│   │   │   ├── sync/
│   │   │   ├── ui/
│   │   │   └── utils/
│   │   └── resources/
│   │       ├── css/
│   │       ├── fxml/
│   │       └── images/
│   └── test/                 # JUnit Test Suite
│
├── pom.xml
└── README.md
```

---

## Development roadmap

### ✅ Version 0.1
- [x] API integration

### ✅ Version 0.2
- [x] SQLite implementation

### ✅ Version 0.3
- [x] Synchronization engine

### ✅ Version 0.4
- [x] Dashboard, Budgets, and Transactions implementation
- [x] Secure API Key setup and persistence

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
