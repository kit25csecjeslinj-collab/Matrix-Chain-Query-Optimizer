package com.matrixoptimizer.algorithm;

import com.matrixoptimizer.model.DatabaseTable;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements Mode B: Relational Join Estimator.
 * Unlike matrix multiplication, relational joins do not have rigid dimension rules.
 * Instead, cost is estimated using:
 * 1. Base table cardinalities (tuple counts |T|).
 * 2. Join predicate selectivity (s): intermediate cardinality |T1 JOIN T2| = |T1| * |T2| * s.
 * 3. Execution I/O and CPU cost model:
 *    Cost(T1 JOIN T2) = Cost(T1) + Cost(T2) + (|T1| + |T2| + |T1 JOIN T2|) * tuple_processing_weight.
 *
 * This provides students with a realistic comparison between theoretical matrix chain
 * multiplication and actual relational database query optimization (System-R DP approach).
 */
public class CostEstimator {

    public static class RelationalDPResult {
        public final long[][] dpCost;
        public final long[][] intermediateCardinality;
        public final int[][] root;
        public final List<String> steps;
        public final int n;

        public RelationalDPResult(long[][] dpCost, long[][] intermediateCardinality, int[][] root, List<String> steps, int n) {
            this.dpCost = dpCost;
            this.intermediateCardinality = intermediateCardinality;
            this.root = root;
            this.steps = steps;
            this.n = n;
        }
    }

    /**
     * Solves optimal join order using relational cardinality and selectivity estimates.
     */
    public static RelationalDPResult optimizeRelational(List<DatabaseTable> tables) {
        if (tables == null || tables.isEmpty()) {
            throw new IllegalArgumentException("Table list cannot be empty.");
        }
        int n = tables.size();
        long[][] dpCost = new long[n + 1][n + 1];
        long[][] card = new long[n + 1][n + 1];
        int[][] root = new int[n + 1][n + 1];
        List<String> steps = new ArrayList<>();

        // Base case: single tables
        for (int i = 1; i <= n; i++) {
            dpCost[i][i] = 0; // zero join cost for a single table
            card[i][i] = tables.get(i - 1).getRowCount();
        }

        // L is chain length
        for (int L = 2; L <= n; L++) {
            for (int i = 1; i <= n - L + 1; i++) {
                int j = i + L - 1;
                dpCost[i][j] = Long.MAX_VALUE;
                int bestSplit = -1;
                long bestIntermediateCard = 0;

                steps.add(String.format("Evaluating relational join interval [%d..%d] (length %d):", i, j, L));

                for (int k = i; k < j; k++) {
                    long cardLeft = card[i][k];
                    long cardRight = card[k + 1][j];

                    // Combined selectivity estimate between the two subsets
                    double combinedSelectivity = Math.min(tables.get(k - 1).getSelectivity(), tables.get(k).getSelectivity());
                    if (combinedSelectivity <= 0.0) combinedSelectivity = 0.05;

                    long resultCard = Math.max(1, (long) (cardLeft * combinedSelectivity * Math.log10(cardRight + 10)));
                    if (resultCard < 0 || resultCard > 1_000_000_000_000L) {
                        resultCard = 1_000_000_000_000L;
                    }

                    // Hash/Sort-Merge Join cost estimate: I/O scan of both inputs + output materialization
                    long joinWork = cardLeft + cardRight + resultCard;
                    long totalCost = dpCost[i][k] + dpCost[k + 1][j] + joinWork;

                    steps.add(String.format("   Split at k=%d: leftCard=%d, rightCard=%d -> outCard=%d, joinCost=%d, total=%d",
                            k, cardLeft, cardRight, resultCard, joinWork, totalCost));

                    if (totalCost < dpCost[i][j]) {
                        dpCost[i][j] = totalCost;
                        bestSplit = k;
                        bestIntermediateCard = resultCard;
                    }
                }

                root[i][j] = bestSplit;
                card[i][j] = bestIntermediateCard;
                steps.add(String.format("=> Optimal for [%d..%d]: minCost = %d, outCard = %d, split at k = %d",
                        i, j, dpCost[i][j], card[i][j], bestSplit));
            }
        }

        return new RelationalDPResult(dpCost, card, root, steps, n);
    }
}
