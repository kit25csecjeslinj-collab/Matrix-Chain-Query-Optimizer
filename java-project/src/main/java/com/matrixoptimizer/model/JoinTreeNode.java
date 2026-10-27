package com.matrixoptimizer.model;

/**
 * Represents a node in the binary join tree.
 * Leaf nodes represent base database tables.
 * Internal nodes represent intermediate join operations with estimated cost and cardinality.
 */
public class JoinTreeNode {
    private String label;
    private boolean isLeaf;
    private DatabaseTable table; // null for internal nodes
    private JoinTreeNode left;
    private JoinTreeNode right;
    private long estimatedCost;
    private long intermediateCardinality;
    private int splitIndex; // DP root split point

    public JoinTreeNode(DatabaseTable table) {
        this.label = table.getName();
        this.isLeaf = true;
        this.table = table;
        this.intermediateCardinality = table.getRowCount();
        this.estimatedCost = 0;
    }

    public JoinTreeNode(String label, JoinTreeNode left, JoinTreeNode right, long estimatedCost, long intermediateCardinality, int splitIndex) {
        this.label = label;
        this.isLeaf = false;
        this.left = left;
        this.right = right;
        this.estimatedCost = estimatedCost;
        this.intermediateCardinality = intermediateCardinality;
        this.splitIndex = splitIndex;
    }

    public String getLabel() { return label; }
    public boolean isLeaf() { return isLeaf; }
    public DatabaseTable getTable() { return table; }
    public JoinTreeNode getLeft() { return left; }
    public JoinTreeNode getRight() { return right; }
    public long getEstimatedCost() { return estimatedCost; }
    public long getIntermediateCardinality() { return intermediateCardinality; }
    public int getSplitIndex() { return splitIndex; }
}
