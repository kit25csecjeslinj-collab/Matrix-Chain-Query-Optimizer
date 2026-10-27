package com.matrixoptimizer.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Repository for storing and retrieving sample SQL queries.
 */
public class QueryRepository {

    public static class SavedQuery {
        private int id;
        private String name;
        private String sqlText;
        private int tableCount;
        private String description;

        public SavedQuery(int id, String name, String sqlText, int tableCount, String description) {
            this.id = id;
            this.name = name;
            this.sqlText = sqlText;
            this.tableCount = tableCount;
            this.description = description;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSqlText() { return sqlText; }
        public int getTableCount() { return tableCount; }
        public String getDescription() { return description; }

        @Override
        public String toString() { return name + " (" + tableCount + " tables)"; }
    }

    private static final List<SavedQuery> inMemoryQueries = new CopyOnWriteArrayList<>();

    static {
        inMemoryQueries.add(new SavedQuery(
                1,
                "E-Commerce 5-Table Join",
                "SELECT *\nFROM Customers C\nJOIN Orders O ON C.customer_id = O.customer_id\nJOIN OrderItems I ON O.order_id = I.order_id\nJOIN Products P ON I.product_id = P.product_id\nJOIN Payments Y ON O.order_id = Y.order_id;",
                5,
                "Standard 5-table relational join representing full customer fulfillment"
        ));
        inMemoryQueries.add(new SavedQuery(
                2,
                "Classic 3-Matrix Textbook Benchmark",
                "SELECT *\nFROM Customers C\nJOIN Orders O ON C.customer_id = O.customer_id\nJOIN Products P ON O.product_id = P.product_id;",
                3,
                "Classic dynamic programming interval benchmark"
        ));
        inMemoryQueries.add(new SavedQuery(
                3,
                "Orders & Items 2-Table Join",
                "SELECT *\nFROM Orders O\nJOIN OrderItems I ON O.order_id = I.order_id;",
                2,
                "Minimal 2-table join"
        ));
    }

    public List<SavedQuery> findAll() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return new ArrayList<>(inMemoryQueries);
        }
        List<SavedQuery> list = new ArrayList<>();
        String sql = "SELECT query_id, query_name, sql_text, table_count, description FROM saved_queries ORDER BY query_id";
        try (conn; PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new SavedQuery(
                        rs.getInt("query_id"),
                        rs.getString("query_name"),
                        rs.getString("sql_text"),
                        rs.getInt("table_count"),
                        rs.getString("description")
                ));
            }
            return list;
        } catch (SQLException e) {
            return new ArrayList<>(inMemoryQueries);
        }
    }

    public void save(SavedQuery query) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            inMemoryQueries.add(query);
            return;
        }
        String sql = "INSERT INTO saved_queries (query_name, sql_text, table_count, description) VALUES (?, ?, ?, ?)";
        try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, query.getName());
            ps.setString(2, query.getSqlText());
            ps.setInt(3, query.getTableCount());
            ps.setString(4, query.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            inMemoryQueries.add(query);
        }
    }
}
