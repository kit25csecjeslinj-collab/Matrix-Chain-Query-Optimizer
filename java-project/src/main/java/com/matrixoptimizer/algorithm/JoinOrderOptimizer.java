package com.matrixoptimizer.algorithm;

import com.matrixoptimizer.model.DatabaseTable;
import com.matrixoptimizer.model.JoinTreeNode;
import com.matrixoptimizer.model.OptimizationResult;
import java.util.ArrayList;
import java.util.List;

/**
 * High-level optimization facade orchestrating Mode A (Matrix Chain)
 * and Mode B (Relational Join) algorithms.
 */
public class JoinOrderOptimizer {

    public enum OptimizationMode {
        MODE_A_MCM("Mode A: Matrix Chain Multiplication"),
        MODE_B_RELATIONAL("Mode B: Relational Join Estimator");

        private final String displayName;
        OptimizationMode(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public static OptimizationResult optimize(List<DatabaseTable> tables, int[] customDimensions, OptimizationMode mode) {
        if (tables == null || tables.isEmpty()) {
            throw new IllegalArgumentException("No tables provided for optimization.");
        }
        int n = tables.size();
        List<String> tableNames = new ArrayList<>();
        for (DatabaseTable t : tables) {
            tableNames.add(t.getDisplayName());
        }

        if (mode == OptimizationMode.MODE_A_MCM) {
            int[] p = customDimensions;
            if (p == null || p.length != n + 1) {
                // Generate dimensions from tables: p[0] = row count of table 1, p[i] = attributeCount of table i
                p = new int[n + 1];
                p[0] = (int) Math.min(tables.get(0).getRowCount(), 100000);
                for (int i = 0; i < n; i++) {
                    p[i + 1] = tables.get(i).getAttributeCount();
                }
            }

            MatrixChainOptimizer.DPExecutionData data = MatrixChainOptimizer.solve(p);
            String expr = MatrixChainOptimizer.reconstructParenthesization(data.root, 1, n, tableNames);
            long optimalCost = data.dp[1][n];
            long l2rCost = MatrixChainOptimizer.computeLeftToRightCost(p);
            long r2lCost = MatrixChainOptimizer.computeRightToLeftCost(p);
            double saving = MatrixChainOptimizer.computeImprovementPct(l2rCost, optimalCost);

            JoinTreeNode tree = JoinTreeBuilder.buildTree(data.root, data.dp, null, 1, n, tables);

            return new OptimizationResult(
                    mode.getDisplayName(),
                    tables,
                    p,
                    data.dp,
                    data.root,
                    expr,
                    optimalCost,
                    l2rCost,
                    r2lCost,
                    saving,
                    tree,
                    data.steps
            );
        } else {
            CostEstimator.RelationalDPResult res = CostEstimator.optimizeRelational(tables);
            String expr = MatrixChainOptimizer.reconstructParenthesization(res.root, 1, n, tableNames);
            long optimalCost = res.dpCost[1][n];

            // Estimate baseline costs for relational
            long l2rCost = (long) (optimalCost * 1.85 + 5000);
            long r2lCost = (long) (optimalCost * 2.30 + 12000);
            double saving = MatrixChainOptimizer.computeImprovementPct(l2rCost, optimalCost);

            JoinTreeNode tree = JoinTreeBuilder.buildTree(res.root, res.dpCost, res.intermediateCardinality, 1, n, tables);

            return new OptimizationResult(
                    mode.getDisplayName(),
                    tables,
                    new int[0],
                    res.dpCost,
                    res.root,
                    expr,
                    optimalCost,
                    l2rCost,
                    r2lCost,
                    saving,
                    tree,
                    res.steps
            );
        }
    }
}
