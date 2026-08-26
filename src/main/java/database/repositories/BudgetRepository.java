package database.repositories;

import database.ConnectionFactory;
import models.Budget;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BudgetRepository {
    private final ConnectionFactory connectionFactory;

    public BudgetRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void save(Budget budget) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO budgets (id, name, category_id, limit_amount, period, closed)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, budget.id());
            stmt.setString(2, budget.name());
            stmt.setString(3, budget.categoryId());
            stmt.setString(4, budget.limitAmount() != null ? budget.limitAmount().toString() : null);
            stmt.setString(5, budget.period());
            stmt.setInt(6, budget.closed() ? 1 : 0);
            
            stmt.executeUpdate();
        }
    }

    public void saveAll(List<Budget> budgets) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO budgets (id, name, category_id, limit_amount, period, closed)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
            
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            conn.setAutoCommit(false);
            try {
                for (Budget budget : budgets) {
                    stmt.setString(1, budget.id());
                    stmt.setString(2, budget.name());
                    stmt.setString(3, budget.categoryId());
                    stmt.setString(4, budget.limitAmount() != null ? budget.limitAmount().toString() : null);
                    stmt.setString(5, budget.period());
                    stmt.setInt(6, budget.closed() ? 1 : 0);
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

    public Optional<Budget> findById(String id) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE id = ?";
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

    public List<Budget> findAll() throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budgets";
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                budgets.add(mapResultSet(rs));
            }
        }
        return budgets;
    }

    private Budget mapResultSet(ResultSet rs) throws SQLException {
        String limitStr = rs.getString("limit_amount");
        BigDecimal limit = limitStr != null ? new BigDecimal(limitStr) : null;
        return new Budget(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("category_id"),
            limit,
            rs.getString("period"),
            rs.getInt("closed") == 1
        );
    }
}
