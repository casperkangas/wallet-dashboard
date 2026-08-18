package database.repositories;

import database.ConnectionFactory;
import models.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepository {
    private final ConnectionFactory connectionFactory;

    public TransactionRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void save(Transaction transaction) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO transactions (
                id, account_id, category_id, amount, currency, transaction_date, 
                description, payment_method, is_transfer, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, transaction);
            stmt.executeUpdate();
        }
    }

    public void saveAll(List<Transaction> transactions) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO transactions (
                id, account_id, category_id, amount, currency, transaction_date, 
                description, payment_method, is_transfer, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            conn.setAutoCommit(false);
            try {
                for (Transaction transaction : transactions) {
                    setParameters(stmt, transaction);
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

    public Optional<Transaction> findById(String id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
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

    public List<Transaction> findAll() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                transactions.add(mapResultSet(rs));
            }
        }
        return transactions;
    }

    public Optional<LocalDateTime> getLastUpdated() throws SQLException {
        String sql = "SELECT MAX(created_at) as max_date FROM transactions";
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

    private void setParameters(PreparedStatement stmt, Transaction transaction) throws SQLException {
        stmt.setString(1, transaction.id());
        stmt.setString(2, transaction.accountId());
        stmt.setString(3, transaction.categoryId());
        stmt.setDouble(4, transaction.amount() != null ? transaction.amount().doubleValue() : 0.0);
        stmt.setString(5, transaction.currency());
        stmt.setString(6, transaction.transactionDate() != null ? transaction.transactionDate().toString() : null);
        stmt.setString(7, transaction.description());
        stmt.setString(8, transaction.paymentMethod());
        stmt.setInt(9, transaction.isTransfer() ? 1 : 0);
        stmt.setString(10, transaction.createdAt() != null ? transaction.createdAt().toString() : null);
    }

    private Transaction mapResultSet(ResultSet rs) throws SQLException {
        String tDateStr = rs.getString("transaction_date");
        LocalDate tDate = tDateStr != null ? LocalDate.parse(tDateStr) : null;
        
        String cDateStr = rs.getString("created_at");
        LocalDateTime cDate = cDateStr != null ? LocalDateTime.parse(cDateStr) : null;
        
        return new Transaction(
            rs.getString("id"),
            rs.getString("account_id"),
            rs.getString("category_id"),
            BigDecimal.valueOf(rs.getDouble("amount")),
            rs.getString("currency"),
            tDate,
            rs.getString("description"),
            rs.getString("payment_method"),
            rs.getInt("is_transfer") == 1,
            cDate
        );
    }
}
