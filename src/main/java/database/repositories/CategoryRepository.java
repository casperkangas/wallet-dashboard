package database.repositories;

import database.ConnectionFactory;
import models.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {
    private final ConnectionFactory connectionFactory;

    public CategoryRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void save(Category category) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO categories (id, name, parent_id, icon)
            VALUES (?, ?, ?, ?)
            """;
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.id());
            stmt.setString(2, category.name());
            stmt.setString(3, category.parentId());
            stmt.setString(4, category.icon());
            
            stmt.executeUpdate();
        }
    }

    public void saveAll(List<Category> categories) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO categories (id, name, parent_id, icon)
            VALUES (?, ?, ?, ?)
            """;
            
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            conn.setAutoCommit(false);
            try {
                for (Category category : categories) {
                    stmt.setString(1, category.id());
                    stmt.setString(2, category.name());
                    stmt.setString(3, category.parentId());
                    stmt.setString(4, category.icon());
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

    public Optional<Category> findById(String id) throws SQLException {
        String sql = "SELECT * FROM categories WHERE id = ?";
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

    public List<Category> findAll() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                categories.add(mapResultSet(rs));
            }
        }
        return categories;
    }

    private Category mapResultSet(ResultSet rs) throws SQLException {
        return new Category(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("parent_id"),
            rs.getString("icon")
        );
    }
}
