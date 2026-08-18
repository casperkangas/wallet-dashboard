package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private final DatabaseConfiguration configuration;

    public ConnectionFactory(DatabaseConfiguration configuration) {
        this.configuration = configuration;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(configuration.getJdbcUrl());
    }
}
