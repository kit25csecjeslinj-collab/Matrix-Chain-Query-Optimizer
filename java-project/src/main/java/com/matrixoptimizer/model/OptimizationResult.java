package com.matrixoptimizer.model;

import java.util.List;

/**
 * Encapsulates the complete result of dynamic programming query optimization,
 * including cost tables, root tables, parenthesization, join tree, and baseline comparisons.
 */
public class OptimizationResult {
    private final String mode; // "Mode A: Matrix Chain Multiplication" or "Mode B: Relational Join Estimator"
    private final List<DatabaseTable> tables;
    private final int[] dimensions;
    private final long[][] dpTable;
    private final int[][] rootTable;
    private final String optimalExpression;
    private final long optimalCost;
    private final long leftToRightCost;
    private final long rightToLeftCost;
    private final double costSavingPct;
    private final JoinTreeNode joinTreeRoot;
    private final List<String> stepLogs;

    public OptimizationResult(String mode, List<DatabaseTable> tables, int[] dimensions,
                              long[][] dpTable, int[][] rootTable, String optimalExpression,
                              long optimalCost, long leftToRightCost, long rightToLeftCost,
                              double costSavingPct, JoinTreeNode joinTreeRoot, List<String> stepLogs) {
        this.mode = mode;
        this.tables = tables;
        this.dimensions = dimensions;
        this.dpTable = dpTable;
        this.rootTable = rootTable;
        this.optimalExpression = optimalExpression;
        this.optimalCost = optimalCost;
        this.leftToRightCost = leftToRightCost;
        this.rightToLeftCost = rightToLeftCost;
        this.costSavingPct = costSavingPct;
        this.joinTreeRoot = joinTreeRoot;
        this.stepLogs = stepLogs;
    }

    public String getMode() { return mode; }
    public List<DatabaseTable> getTables() { return tables; }
    public int[] getDimensions() { return dimensions; }
    public long[][] getDpTable() { return dpTable; }
    public int[][] getRootTable() { return rootTable; }
    public String getOptimalExpression() { return optimalExpression; }
    public long getOptimalCost() { return optimalCost; }
    public long getLeftToRightCost() { return leftToRightCost; }
    public long getRightToLeftCost() { return rightToLeftCost; }
    public double getCostSavingPct() { return costSavingPct; }
    public JoinTreeNode getJoinTreeRoot() { return joinTreeRoot; }
    public List<String> getStepLogs() { return stepLogs; }
}
