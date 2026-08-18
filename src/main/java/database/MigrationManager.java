package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MigrationManager {

    private static final String CREATE_ACCOUNTS_TABLE = """
        CREATE TABLE IF NOT EXISTS accounts (
            id TEXT PRIMARY KEY,
            name TEXT,
            currency TEXT,
            balance REAL,
            institution TEXT,
            updated_at TEXT
        );
        """;

    private static final String CREATE_TRANSACTIONS_TABLE = """
        CREATE TABLE IF NOT EXISTS transactions (
            id TEXT PRIMARY KEY,
            account_id TEXT,
            category_id TEXT,
            amount REAL,
            currency TEXT,
            transaction_date TEXT,
            description TEXT,
            payment_method TEXT,
            is_transfer INTEGER,
            created_at TEXT
        );
        """;

    private static final String CREATE_CATEGORIES_TABLE = """
        CREATE TABLE IF NOT EXISTS categories (
            id TEXT PRIMARY KEY,
            name TEXT,
            parent_id TEXT,
            icon TEXT
        );
        """;

    private static final String CREATE_BUDGETS_TABLE = """
        CREATE TABLE IF NOT EXISTS budgets (
            id TEXT PRIMARY KEY,
            category_id TEXT,
            limit_amount REAL,
            period TEXT
        );
        """;

    private static final String CREATE_DAILY_SNAPSHOTS_TABLE = """
        CREATE TABLE IF NOT EXISTS daily_snapshots (
            snapshot_date TEXT PRIMARY KEY,
            net_worth REAL,
            cash REAL,
            investments REAL,
            debt REAL,
            monthly_income REAL,
            monthly_expenses REAL
        );
        """;

    public void runMigrations(ConnectionFactory connectionFactory) throws SQLException {
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(CREATE_ACCOUNTS_TABLE);
            stmt.execute(CREATE_TRANSACTIONS_TABLE);
            stmt.execute(CREATE_CATEGORIES_TABLE);
            stmt.execute(CREATE_BUDGETS_TABLE);
            stmt.execute(CREATE_DAILY_SNAPSHOTS_TABLE);
            
        }
    }
}
