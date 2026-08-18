package database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseSetupTest {

    private Connection inMemoryConnection;
    private ConnectionFactory testConnectionFactory;
    private MigrationManager migrationManager;

    @BeforeEach
    void setUp() throws SQLException {
        // Use a shared in-memory SQLite database for testing
        String dbUrl = "jdbc:sqlite:file:testdb?mode=memory&cache=shared";
        inMemoryConnection = DriverManager.getConnection(dbUrl);
        
        testConnectionFactory = new ConnectionFactory(new DatabaseConfiguration()) {
            @Override
            public Connection getConnection() throws SQLException {
                return DriverManager.getConnection(dbUrl);
            }
        };
        
        migrationManager = new MigrationManager();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (inMemoryConnection != null && !inMemoryConnection.isClosed()) {
            inMemoryConnection.close();
        }
    }

    @Test
    void testMigrationsCreateAllTables() throws SQLException {
        DatabaseManager dbManager = new DatabaseManager(testConnectionFactory, migrationManager);
        dbManager.initialize();

        Set<String> tables = new HashSet<>();
        try (ResultSet rs = inMemoryConnection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }

        assertTrue(tables.contains("accounts"), "Accounts table should exist");
        assertTrue(tables.contains("transactions"), "Transactions table should exist");
        assertTrue(tables.contains("categories"), "Categories table should exist");
        assertTrue(tables.contains("budgets"), "Budgets table should exist");
        assertTrue(tables.contains("daily_snapshots"), "Daily snapshots table should exist");
    }
}
