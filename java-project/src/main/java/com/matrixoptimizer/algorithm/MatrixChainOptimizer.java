package com.matrixoptimizer.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Core Dynamic Programming engine for Matrix Chain Multiplication.
 * Solves the optimal evaluation order of multiplying n matrices A1, A2, ..., An
 * with dimensions given by array p of length n + 1, where matrix A_i has dimension p[i-1] x p[i].
 *
 * Algorithm Properties:
 * - Time Complexity: O(n^3) - n(n-1)/2 subproblems, each evaluating up to n splits.
 * - Space Complexity: O(n^2) - for storing DP cost table dp[n+1][n+1] and root table root[n+1][n+1].
 * - Recurrence:
 *     dp[i][i] = 0
 *     dp[i][j] = min_{i <= k < j} { dp[i][k] + dp[k+1][j] + p[i-1] * p[k] * p[j] }
 */
public class MatrixChainOptimizer {

    public static class DPExecutionData {
        public final long[][] dp;     // dp[1..n][1..n]
        public final int[][] root;    // root[1..n][1..n]
        public final List<String> steps;
        public final int n;

        public DPExecutionData(long[][] dp, int[][] root, List<String> steps, int n) {
            this.dp = dp;
            this.root = root;
            this.steps = steps;
            this.n = n;
        }
    }

    /**
     * Executes DP on intervals for matrix chain dimensions p.
     * p must have length n + 1 where n is number of matrices.
     */
    public static DPExecutionData solve(int[] p) {
        if (p == null || p.length < 2) {
            throw new IllegalArgumentException("Dimension array p must have at least 2 entries.");
        }
        int n = p.length - 1;
        long[][] dp = new long[n + 1][n + 1];
        int[][] root = new int[n + 1][n + 1];
        List<String> steps = new ArrayList<>();

        // Base case: chains of length 1 cost 0 multiplications
        for (int i = 1; i <= n; i++) {
            dp[i][i] = 0;
        }

        // L is chain length: from 2 to n
        for (int L = 2; L <= n; L++) {
            for (int i = 1; i <= n - L + 1; i++) {
                int j = i + L - 1;
                dp[i][j] = Long.MAX_VALUE;
                int bestSplit = -1;

                steps.add(String.format("Evaluating interval [%d..%d] (Chain length L = %d):", i, j, L));

                for (int k = i; k < j; k++) {
                    long costMult;
                    try {
                        costMult = Math.multiplyExact((long) p[i - 1], Math.multiplyExact((long) p[k], (long) p[j]));
                    } catch (ArithmeticException ex) {
                        costMult = Long.MAX_VALUE / 2; // Guard against integer overflow
                    }

                    long subCost = dp[i][k] + dp[k + 1][j];
                    long totalCost = (subCost >= Long.MAX_VALUE - costMult) ? Long.MAX_VALUE : subCost + costMult;

                    steps.add(String.format("   Split k=%d: dp[%d][%d](%d) + dp[%d][%d](%d) + (%d x %d x %d = %d) -> sum = %d",
                            k, i, k, dp[i][k], k + 1, j, dp[k + 1][j], p[i - 1], p[k], p[j], costMult, totalCost));

                    if (totalCost < dp[i][j]) {
                        dp[i][j] = totalCost;
                        bestSplit = k;
                    }
                }
                root[i][j] = bestSplit;
                steps.add(String.format("=> Optimal for [%d..%d]: min cost = %d, split at k = %d", i, j, dp[i][j], bestSplit));
            }
        }

        return new DPExecutionData(dp, root, steps, n);
    }

    /**
     * Recursively reconstructs the parenthesized join string expression.
     */
    public static String reconstructParenthesization(int[][] root, int i, int j, List<String> tableNames) {
        if (i == j) {
            return (i - 1 < tableNames.size()) ? tableNames.get(i - 1) : ("A" + i);
        }
        int k = root[i][j];
        if (k < i || k >= j) {
            return "(" + tableNames.get(i - 1) + " JOIN " + tableNames.get(j - 1) + ")";
        }
        String left = reconstructParenthesization(root, i, k, tableNames);
        String right = reconstructParenthesization(root, k + 1, j, tableNames);
        return "(" + left + " JOIN " + right + ")";
    }

    /**
     * Calculates the estimated cost of standard Left-to-Right join:
     * (((A1 JOIN A2) JOIN A3) ... JOIN An)
     */
    public static long computeLeftToRightCost(int[] p) {
        int n = p.length - 1;
        if (n <= 1) return 0;
        long totalCost = 0;
        long currentRows = p[0];
        for (int i = 1; i < n; i++) {
            // intermediate matrix dimension is currentRows x p[i]
            // multiplied by matrix i+1 which is p[i] x p[i+1]
            long mult = currentRows * p[i] * p[i + 1];
            totalCost += mult;
            // resultant matrix has dimension currentRows x p[i+1]
        }
        return totalCost;
    }

    /**
     * Calculates the estimated cost of standard Right-to-Left join:
     * (A1 JOIN (A2 JOIN (... (An-1 JOIN An))))
     */
    public static long computeRightToLeftCost(int[] p) {
        int n = p.length - 1;
        if (n <= 1) return 0;
        long totalCost = 0;
        long currentCols = p[n];
        for (int i = n - 1; i >= 1; i--) {
            // matrix i has dim p[i-1] x p[i]
            // multiplied by intermediate result which is p[i] x currentCols
            long mult = (long) p[i - 1] * p[i] * currentCols;
            totalCost += mult;
        }
        return totalCost;
    }

    /**
     * Calculates percentage improvement safely: (baseline - optimal) / baseline * 100%
     */
    public static double computeImprovementPct(long baselineCost, long optimalCost) {
        if (baselineCost <= 0) return 0.0;
        double diff = (double) (baselineCost - optimalCost);
        return Math.max(0.0, (diff / baselineCost) * 100.0);
    }
}
