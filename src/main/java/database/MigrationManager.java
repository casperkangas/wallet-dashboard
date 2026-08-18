package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

public class MigrationManager {

    private static final String CREATE_SCHEMA_VERSION_TABLE = """
        CREATE TABLE IF NOT EXISTS schema_version (
            version INTEGER PRIMARY KEY,
            applied_at TEXT
        );
        """;

    private static final String V1_ACCOUNTS = """
        CREATE TABLE IF NOT EXISTS accounts (
            id TEXT PRIMARY KEY,
            name TEXT,
            currency TEXT,
            balance TEXT,
            institution TEXT,
            updated_at TEXT
        );
        """;

    private static final String V1_TRANSACTIONS = """
        CREATE TABLE IF NOT EXISTS transactions (
            id TEXT PRIMARY KEY,
            account_id TEXT,
            category_id TEXT,
            amount TEXT,
            currency TEXT,
            transaction_date TEXT,
            description TEXT,
            payment_method TEXT,
            is_transfer INTEGER,
            created_at TEXT
        );
        """;

    private static final String V1_CATEGORIES = """
        CREATE TABLE IF NOT EXISTS categories (
            id TEXT PRIMARY KEY,
            name TEXT,
            parent_id TEXT,
            icon TEXT
        );
        """;

    private static final String V1_BUDGETS = """
        CREATE TABLE IF NOT EXISTS budgets (
            id TEXT PRIMARY KEY,
            category_id TEXT,
            limit_amount TEXT,
            period TEXT
        );
        """;

    private static final String V1_DAILY_SNAPSHOTS = """
        CREATE TABLE IF NOT EXISTS daily_snapshots (
            snapshot_date TEXT PRIMARY KEY,
            net_worth TEXT,
            cash TEXT,
            investments TEXT,
            debt TEXT,
            monthly_income TEXT,
            monthly_expenses TEXT
        );
        """;

    public void runMigrations(ConnectionFactory connectionFactory) throws SQLException {
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(CREATE_SCHEMA_VERSION_TABLE);
            
            int currentVersion = getCurrentVersion(conn);
            
            if (currentVersion < 1) {
                stmt.execute(V1_ACCOUNTS);
                stmt.execute(V1_TRANSACTIONS);
                stmt.execute(V1_CATEGORIES);
                stmt.execute(V1_BUDGETS);
                stmt.execute(V1_DAILY_SNAPSHOTS);
                
                recordMigration(conn, 1);
            }
        }
    }

    private int getCurrentVersion(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(version) FROM schema_version")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            // Table might not exist or other error, return 0
        }
        return 0;
    }

    private void recordMigration(Connection conn, int version) throws SQLException {
        String sql = "INSERT INTO schema_version (version, applied_at) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, version);
            pstmt.setString(2, Instant.now().toString());
            pstmt.executeUpdate();
        }
    }
}
