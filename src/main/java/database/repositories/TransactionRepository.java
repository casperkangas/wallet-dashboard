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
        stmt.setString(4, transaction.amount() != null ? transaction.amount().toPlainString() : null);
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
        
        String amountStr = rs.getString("amount");
        return new Transaction(
            rs.getString("id"),
            rs.getString("account_id"),
            rs.getString("category_id"),
            amountStr != null ? new BigDecimal(amountStr) : BigDecimal.ZERO,
            rs.getString("currency"),
            tDate,
            rs.getString("description"),
            rs.getString("payment_method"),
            rs.getInt("is_transfer") == 1,
            cDate
        );
    }

    public List<models.MonthlyTrendRecord> getMonthlyTrends(int monthsBack) throws SQLException {
        List<models.MonthlyTrendRecord> trends = new ArrayList<>();
        String sql = """
            SELECT 
                year_month,
                SUM(CASE WHEN category_net > 0 AND is_excluded = 0 THEN category_net ELSE 0 END) as income,
                SUM(CASE WHEN category_net < 0 AND is_excluded = 0 THEN ABS(category_net) ELSE 0 END) as expenses
            FROM (
                SELECT 
                    strftime('%Y-%m', t.transaction_date) as year_month,
                    IFNULL(t.category_id, 'UNGROUPED') as cat_id,
                    SUM(t.amount) as category_net,
                    CASE 
                        WHEN LOWER(IFNULL(c.name, '')) LIKE '%invest%' OR LOWER(IFNULL(parent_c.name, '')) LIKE '%invest%' THEN 1 
                        WHEN LOWER(IFNULL(c.name, '')) LIKE '%transfer%' OR LOWER(IFNULL(parent_c.name, '')) LIKE '%transfer%' THEN 1
                        ELSE 0 
                    END as is_excluded
                FROM transactions t
                LEFT JOIN categories c ON t.category_id = c.id
                LEFT JOIN categories parent_c ON c.parent_id = parent_c.id
                WHERE t.is_transfer = 0
                GROUP BY year_month, t.category_id
            )
            GROUP BY year_month
            ORDER BY year_month DESC
            LIMIT ?
            """;
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, monthsBack);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String ym = rs.getString("year_month");
                    if (ym == null) continue;
                    
                    String incStr = rs.getString("income");
                    String expStr = rs.getString("expenses");
                    
                    BigDecimal inc = incStr != null ? new BigDecimal(incStr) : BigDecimal.ZERO;
                    BigDecimal exp = expStr != null ? new BigDecimal(expStr) : BigDecimal.ZERO;
                    
                    trends.add(new models.MonthlyTrendRecord(ym, inc, exp));
                }
            }
        }
        java.util.Collections.reverse(trends);
        return trends;
    }
}
