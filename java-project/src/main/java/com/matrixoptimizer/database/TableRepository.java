package com.matrixoptimizer.database;

import com.matrixoptimizer.model.DatabaseTable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * DAO / Repository for database table configurations.
 * Supports persistent MySQL storage with seamless in-memory fallback.
 */
public class TableRepository {

    private static final List<DatabaseTable> inMemoryStore = new CopyOnWriteArrayList<>();

    static {
        // Seed default 5-table dataset
        inMemoryStore.add(new DatabaseTable(1, "Customers", "C", 10000, 10, 0.02, "customer_id", "Customer user profiles"));
        inMemoryStore.add(new DatabaseTable(2, "Orders", "O", 50000, 8, 0.05, "order_id", "Order placement headers"));
        inMemoryStore.add(new DatabaseTable(3, "OrderItems", "I", 200000, 6, 0.01, "order_id", "Order line items"));
        inMemoryStore.add(new DatabaseTable(4, "Products", "P", 5000, 15, 0.10, "product_id", "Catalog products"));
        inMemoryStore.add(new DatabaseTable(5, "Payments", "Y", 45000, 7, 0.05, "order_id", "Payment transaction logs"));
    }

    public List<DatabaseTable> findAll() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return new ArrayList<>(inMemoryStore);
        }
        List<DatabaseTable> list = new ArrayList<>();
        String sql = "SELECT table_id, table_name, row_count, attribute_count, selectivity, primary_join_key, description FROM database_tables ORDER BY table_id";
        try (conn; PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new DatabaseTable(
                        rs.getInt("table_id"),
                        rs.getString("table_name"),
                        "",
                        rs.getLong("row_count"),
                        rs.getInt("attribute_count"),
                        rs.getDouble("selectivity"),
                        rs.getString("primary_join_key"),
                        rs.getString("description")
                ));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("Database query failed: " + e.getMessage() + ". Using in-memory store.");
            return new ArrayList<>(inMemoryStore);
        }
    }

    public boolean save(DatabaseTable table) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            if (table.getId() == 0) {
                table.setId(inMemoryStore.size() + 1);
            }
            // Remove existing if update
            inMemoryStore.removeIf(t -> t.getName().equalsIgnoreCase(table.getName()));
            inMemoryStore.add(table);
            return true;
        }

        String sql = "INSERT INTO database_tables (table_name, row_count, attribute_count, selectivity, primary_join_key, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE row_count = VALUES(row_count), " +
                     "attribute_count = VALUES(attribute_count), selectivity = VALUES(selectivity), " +
                     "primary_join_key = VALUES(primary_join_key), description = VALUES(description)";
        try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table.getName());
            ps.setLong(2, table.getRowCount());
            ps.setInt(3, table.getAttributeCount());
            ps.setDouble(4, table.getSelectivity());
            ps.setString(5, table.getPrimaryJoinKey());
            ps.setString(6, table.getDescription());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving table to MySQL: " + e.getMessage());
            inMemoryStore.removeIf(t -> t.getName().equalsIgnoreCase(table.getName()));
            inMemoryStore.add(table);
            return true;
        }
    }

    public boolean deleteByName(String tableName) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return inMemoryStore.removeIf(t -> t.getName().equalsIgnoreCase(tableName));
        }
        String sql = "DELETE FROM database_tables WHERE table_name = ?";
        try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            int rows = ps.executeUpdate();
            inMemoryStore.removeIf(t -> t.getName().equalsIgnoreCase(tableName));
            return rows > 0;
        } catch (SQLException e) {
            inMemoryStore.removeIf(t -> t.getName().equalsIgnoreCase(tableName));
            return true;
        }
    }

    public void resetToDefault() {
        inMemoryStore.clear();
        inMemoryStore.add(new DatabaseTable(1, "Customers", "C", 10000, 10, 0.02, "customer_id", "Customer user profiles"));
        inMemoryStore.add(new DatabaseTable(2, "Orders", "O", 50000, 8, 0.05, "order_id", "Order placement headers"));
        inMemoryStore.add(new DatabaseTable(3, "OrderItems", "I", 200000, 6, 0.01, "order_id", "Order line items"));
        inMemoryStore.add(new DatabaseTable(4, "Products", "P", 5000, 15, 0.10, "product_id", "Catalog products"));
        inMemoryStore.add(new DatabaseTable(5, "Payments", "Y", 45000, 7, 0.05, "order_id", "Payment transaction logs"));
    }
}
