package com.matrixoptimizer.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Repository for storing and retrieving past optimization execution runs.
 */
public class OptimizationRepository {

    public static class RunRecord {
        private int id;
        private String mode;
        private String tableSequence;
        private String optimalJoinOrder;
        private long optimalCost;
        private long leftToRightCost;
        private double costSavingPct;
        private String timestamp;

        public RunRecord(int id, String mode, String tableSequence, String optimalJoinOrder,
                         long optimalCost, long leftToRightCost, double costSavingPct, String timestamp) {
            this.id = id;
            this.mode = mode;
            this.tableSequence = tableSequence;
            this.optimalJoinOrder = optimalJoinOrder;
            this.optimalCost = optimalCost;
            this.leftToRightCost = leftToRightCost;
            this.costSavingPct = costSavingPct;
            this.timestamp = timestamp;
        }

        public int getId() { return id; }
        public String getMode() { return mode; }
        public String getTableSequence() { return tableSequence; }
        public String getOptimalJoinOrder() { return optimalJoinOrder; }
        public long getOptimalCost() { return optimalCost; }
        public long getLeftToRightCost() { return leftToRightCost; }
        public double getCostSavingPct() { return costSavingPct; }
        public String getTimestamp() { return timestamp; }
    }

    private static final List<RunRecord> inMemoryRuns = new CopyOnWriteArrayList<>();

    public List<RunRecord> findAll() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return new ArrayList<>(inMemoryRuns);
        }
        List<RunRecord> list = new ArrayList<>();
        String sql = "SELECT run_id, optimization_mode, table_sequence, optimal_join_order, optimal_cost, left_to_right_cost, cost_saving_pct, created_at FROM optimization_runs ORDER BY run_id DESC LIMIT 50";
        try (conn; PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new RunRecord(
                        rs.getInt("run_id"),
                        rs.getString("optimization_mode"),
                        rs.getString("table_sequence"),
                        rs.getString("optimal_join_order"),
                        rs.getLong("optimal_cost"),
                        rs.getLong("left_to_right_cost"),
                        rs.getDouble("cost_saving_pct"),
                        rs.getString("created_at")
                ));
            }
            return list;
        } catch (SQLException e) {
            return new ArrayList<>(inMemoryRuns);
        }
    }

    public void recordRun(String mode, String tableSequence, String optimalOrder, long optimalCost, long l2rCost, double savingPct) {
        String now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        RunRecord rec = new RunRecord(inMemoryRuns.size() + 1, mode, tableSequence, optimalOrder, optimalCost, l2rCost, savingPct, now);
        inMemoryRuns.add(0, rec);

        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "INSERT INTO optimization_runs (optimization_mode, table_sequence, dimension_sequence, optimal_join_order, optimal_cost, left_to_right_cost, right_to_left_cost, cost_saving_pct) VALUES (?, ?, '', ?, ?, ?, ?, ?)";
            try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, mode);
                ps.setString(2, tableSequence);
                ps.setString(3, optimalOrder);
                ps.setLong(4, optimalCost);
                ps.setLong(5, l2rCost);
                ps.setLong(6, l2rCost);
                ps.setDouble(7, savingPct);
                ps.executeUpdate();
            } catch (SQLException e) {
                // Ignore silently, in-memory record already saved
            }
        }
    }
}
