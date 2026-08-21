package database.repositories;

import database.ConnectionFactory;
import models.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRepository {
    private final ConnectionFactory connectionFactory;

    public AccountRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void save(Account account) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO accounts (id, name, currency, balance, institution, account_type, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, account.id());
            stmt.setString(2, account.name());
            stmt.setString(3, account.currency());
            stmt.setString(4, account.balance() != null ? account.balance().toPlainString() : null);
            stmt.setString(5, account.institution());
            stmt.setString(6, account.accountType());
            stmt.setString(7, account.updatedAt() != null ? account.updatedAt().toString() : null);
            
            stmt.executeUpdate();
        }
    }

    public void saveAll(List<Account> accounts) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO accounts (id, name, currency, balance, institution, account_type, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            conn.setAutoCommit(false);
            try {
                for (Account account : accounts) {
                    stmt.setString(1, account.id());
                    stmt.setString(2, account.name());
                    stmt.setString(3, account.currency());
                    stmt.setString(4, account.balance() != null ? account.balance().toPlainString() : null);
                    stmt.setString(5, account.institution());
                    stmt.setString(6, account.accountType());
                    stmt.setString(7, account.updatedAt() != null ? account.updatedAt().toString() : null);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Optional<Account> findById(String id) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Account> findAll() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts";
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                accounts.add(mapResultSet(rs));
            }
        }
        return accounts;
    }
    
    public Optional<LocalDateTime> getLastUpdated() throws SQLException {
        String sql = "SELECT MAX(updated_at) as max_date FROM accounts";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            if (rs.next()) {
                String maxDate = rs.getString("max_date");
                if (maxDate != null && !maxDate.trim().isEmpty()) {
                    return Optional.of(LocalDateTime.parse(maxDate));
                }
            }
        }
        return Optional.empty();
    }

    private Account mapResultSet(ResultSet rs) throws SQLException {
        String updatedAtStr = rs.getString("updated_at");
        LocalDateTime updatedAt = updatedAtStr != null ? LocalDateTime.parse(updatedAtStr) : null;
        
        String balanceStr = rs.getString("balance");
        return new Account(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("currency"),
            balanceStr != null ? new BigDecimal(balanceStr) : BigDecimal.ZERO,
            rs.getString("institution"),
            rs.getString("account_type"),
            updatedAt
        );
    }
}
