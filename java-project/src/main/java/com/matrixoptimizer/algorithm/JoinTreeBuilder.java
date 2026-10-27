package com.matrixoptimizer.algorithm;

import com.matrixoptimizer.model.DatabaseTable;
import com.matrixoptimizer.model.JoinTreeNode;
import java.util.List;

/**
 * Builds a hierarchical binary join tree from the DP root split table.
 */
public class JoinTreeBuilder {

    public static JoinTreeNode buildTree(int[][] root, long[][] dp, long[][] card, 
                                        int i, int j, List<DatabaseTable> tables) {
        if (i == j) {
            DatabaseTable t = tables.get(i - 1);
            return new JoinTreeNode(t);
        }

        int k = root[i][j];
        if (k < i || k >= j) {
            k = i; // fallback
        }

        JoinTreeNode leftChild = buildTree(root, dp, card, i, k, tables);
        JoinTreeNode rightChild = buildTree(root, dp, card, k + 1, j, tables);

        long cost = dp != null ? dp[i][j] : 0;
        long cardinality = card != null ? card[i][j] : 0;

        String label = String.format("JOIN [%d..%d]", i, j);
        return new JoinTreeNode(label, leftChild, rightChild, cost, cardinality, k);
    }
}
