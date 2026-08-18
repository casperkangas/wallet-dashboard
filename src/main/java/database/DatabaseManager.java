package database;

import java.sql.SQLException;

public class DatabaseManager {
    private final ConnectionFactory connectionFactory;
    private final MigrationManager migrationManager;

    public DatabaseManager(ConnectionFactory connectionFactory, MigrationManager migrationManager) {
        this.connectionFactory = connectionFactory;
        this.migrationManager = migrationManager;
    }

    public void initialize() throws SQLException {
        migrationManager.runMigrations(connectionFactory);
    }
}
