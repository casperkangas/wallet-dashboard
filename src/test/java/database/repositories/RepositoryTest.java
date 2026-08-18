package database.repositories;

import database.ConnectionFactory;
import database.DatabaseConfiguration;
import database.MigrationManager;
import models.Account;
import models.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTest {

    private Connection inMemoryConnection;
    private ConnectionFactory testConnectionFactory;

    @BeforeEach
    void setUp() throws SQLException {
        // Use a shared in-memory SQLite database for testing
        String dbUrl = "jdbc:sqlite:file:testdb_repos?mode=memory&cache=shared";
        inMemoryConnection = DriverManager.getConnection(dbUrl);

        testConnectionFactory = new ConnectionFactory(new DatabaseConfiguration()) {
            @Override
            public Connection getConnection() throws SQLException {
                return DriverManager.getConnection(dbUrl);
            }
        };

        // Run migrations
        new MigrationManager().runMigrations(testConnectionFactory);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (inMemoryConnection != null && !inMemoryConnection.isClosed()) {
            inMemoryConnection.close();
        }
    }

    @Test
    void testAccountRepositorySaveAndFind() throws SQLException {
        AccountRepository repo = new AccountRepository(testConnectionFactory);
        Account acc = new Account(
            "acc-1", "Test Account", "USD", new BigDecimal("100.5"), "TestBank", LocalDateTime.of(2023, 10, 1, 10, 0)
        );

        repo.save(acc);
        Optional<Account> found = repo.findById("acc-1");

        assertTrue(found.isPresent());
        assertEquals("Test Account", found.get().name());
        assertEquals(new BigDecimal("100.5"), found.get().balance());
        assertEquals(LocalDateTime.of(2023, 10, 1, 10, 0), found.get().updatedAt());
    }

    @Test
    void testCategoryRepositorySaveAndFind() throws SQLException {
        CategoryRepository repo = new CategoryRepository(testConnectionFactory);
        Category cat = new Category("cat-1", "Food", null, "icon-food");

        repo.save(cat);
        Optional<Category> found = repo.findById("cat-1");

        assertTrue(found.isPresent());
        assertEquals("Food", found.get().name());
        assertEquals("icon-food", found.get().icon());
    }
}
