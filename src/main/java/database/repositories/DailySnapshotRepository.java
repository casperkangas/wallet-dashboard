package database.repositories;

import database.ConnectionFactory;
import models.DailySnapshot;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DailySnapshotRepository {
    private final ConnectionFactory connectionFactory;

    public DailySnapshotRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void save(DailySnapshot snapshot) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO daily_snapshots (
                snapshot_date, net_worth, cash, investments, debt, monthly_income, monthly_expenses
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, snapshot.snapshotDate() != null ? snapshot.snapshotDate().toString() : null);
            stmt.setString(2, snapshot.netWorth() != null ? snapshot.netWorth().toPlainString() : null);
            stmt.setString(3, snapshot.cash() != null ? snapshot.cash().toPlainString() : null);
            stmt.setString(4, snapshot.investments() != null ? snapshot.investments().toPlainString() : null);
            stmt.setString(5, snapshot.debt() != null ? snapshot.debt().toPlainString() : null);
            stmt.setString(6, snapshot.monthlyIncome() != null ? snapshot.monthlyIncome().toPlainString() : null);
            stmt.setString(7, snapshot.monthlyExpenses() != null ? snapshot.monthlyExpenses().toPlainString() : null);
            
            stmt.executeUpdate();
        }
    }

    public void saveAll(List<DailySnapshot> snapshots) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO daily_snapshots (
                snapshot_date, net_worth, cash, investments, debt, monthly_income, monthly_expenses
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            conn.setAutoCommit(false);
            try {
                for (DailySnapshot snapshot : snapshots) {
                    stmt.setString(1, snapshot.snapshotDate() != null ? snapshot.snapshotDate().toString() : null);
                    stmt.setString(2, snapshot.netWorth() != null ? snapshot.netWorth().toPlainString() : null);
                    stmt.setString(3, snapshot.cash() != null ? snapshot.cash().toPlainString() : null);
                    stmt.setString(4, snapshot.investments() != null ? snapshot.investments().toPlainString() : null);
                    stmt.setString(5, snapshot.debt() != null ? snapshot.debt().toPlainString() : null);
                    stmt.setString(6, snapshot.monthlyIncome() != null ? snapshot.monthlyIncome().toPlainString() : null);
                    stmt.setString(7, snapshot.monthlyExpenses() != null ? snapshot.monthlyExpenses().toPlainString() : null);
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

    public Optional<DailySnapshot> findByDate(LocalDate date) throws SQLException {
        String sql = "SELECT * FROM daily_snapshots WHERE snapshot_date = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, date.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<DailySnapshot> findAll() throws SQLException {
        List<DailySnapshot> snapshots = new ArrayList<>();
        String sql = "SELECT * FROM daily_snapshots";
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                snapshots.add(mapResultSet(rs));
            }
        }
        return snapshots;
    }

    private DailySnapshot mapResultSet(ResultSet rs) throws SQLException {
        String dateStr = rs.getString("snapshot_date");
        LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : null;
        
        String netWorthStr = rs.getString("net_worth");
        String cashStr = rs.getString("cash");
        String investmentsStr = rs.getString("investments");
        String debtStr = rs.getString("debt");
        String monthlyIncomeStr = rs.getString("monthly_income");
        String monthlyExpensesStr = rs.getString("monthly_expenses");
        
        return new DailySnapshot(
            date,
            netWorthStr != null ? new BigDecimal(netWorthStr) : BigDecimal.ZERO,
            cashStr != null ? new BigDecimal(cashStr) : BigDecimal.ZERO,
            investmentsStr != null ? new BigDecimal(investmentsStr) : BigDecimal.ZERO,
            debtStr != null ? new BigDecimal(debtStr) : BigDecimal.ZERO,
            monthlyIncomeStr != null ? new BigDecimal(monthlyIncomeStr) : BigDecimal.ZERO,
            monthlyExpensesStr != null ? new BigDecimal(monthlyExpensesStr) : BigDecimal.ZERO
        );
    }

    public List<DailySnapshot> getHistoricalSnapshots(int daysBack) throws SQLException {
        List<DailySnapshot> snapshots = new ArrayList<>();
        String sql = """
            SELECT * FROM daily_snapshots
            ORDER BY snapshot_date DESC
            LIMIT ?
            """;
            
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, daysBack);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    snapshots.add(mapResultSet(rs));
                }
            }
        }
        java.util.Collections.reverse(snapshots);
        return snapshots;
    }
}
