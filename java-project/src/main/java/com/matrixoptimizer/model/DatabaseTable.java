package com.matrixoptimizer.model;

import java.util.Objects;

/**
 * Represents a relational database table with cardinality, column dimensions,
 * selectivity factors, and join keys for query optimization.
 */
public class DatabaseTable {
    private int id;
    private String name;
    private String alias;
    private long rowCount;        // Tuple count (cardinality)
    private int attributeCount;   // Number of columns / attribute width
    private double selectivity;   // Estimated filter/join selectivity (0.0 < s <= 1.0)
    private String primaryJoinKey;
    private String description;

    public DatabaseTable() {
        this.selectivity = 0.05;
    }

    public DatabaseTable(String name, long rowCount, int attributeCount) {
        this(0, name, "", rowCount, attributeCount, 0.05, "", "");
    }

    public DatabaseTable(String name, String alias, long rowCount, int attributeCount) {
        this(0, name, alias, rowCount, attributeCount, 0.05, "", "");
    }

    public DatabaseTable(int id, String name, String alias, long rowCount, int attributeCount, 
                         double selectivity, String primaryJoinKey, String description) {
        if (rowCount <= 0) {
            throw new IllegalArgumentException("Row count must be a positive integer, got: " + rowCount);
        }
        if (attributeCount <= 0) {
            throw new IllegalArgumentException("Attribute count must be a positive integer, got: " + attributeCount);
        }
        if (selectivity <= 0.0 || selectivity > 1.0) {
            throw new IllegalArgumentException("Selectivity must be in range (0.0, 1.0], got: " + selectivity);
        }
        this.id = id;
        this.name = Objects.requireNonNull(name, "Table name cannot be null").trim();
        this.alias = alias != null ? alias.trim() : "";
        this.rowCount = rowCount;
        this.attributeCount = attributeCount;
        this.selectivity = selectivity;
        this.primaryJoinKey = primaryJoinKey != null ? primaryJoinKey.trim() : "";
        this.description = description != null ? description.trim() : "";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getDisplayName() {
        if (alias != null && !alias.isEmpty()) {
            return name + " (" + alias + ")";
        }
        return name;
    }

    public long getRowCount() { return rowCount; }
    public void setRowCount(long rowCount) {
        if (rowCount <= 0) throw new IllegalArgumentException("Row count must be positive");
        this.rowCount = rowCount;
    }

    public int getAttributeCount() { return attributeCount; }
    public void setAttributeCount(int attributeCount) {
        if (attributeCount <= 0) throw new IllegalArgumentException("Attribute count must be positive");
        this.attributeCount = attributeCount;
    }

    public double getSelectivity() { return selectivity; }
    public void setSelectivity(double selectivity) {
        if (selectivity <= 0.0 || selectivity > 1.0) {
            throw new IllegalArgumentException("Selectivity must be in range (0.0, 1.0]");
        }
        this.selectivity = selectivity;
    }

    public String getPrimaryJoinKey() { return primaryJoinKey; }
    public void setPrimaryJoinKey(String primaryJoinKey) { this.primaryJoinKey = primaryJoinKey; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("%s [rows: %d, cols: %d]", getDisplayName(), rowCount, attributeCount);
    }
}
