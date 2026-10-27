package com.matrixoptimizer.util;

import com.matrixoptimizer.model.DatabaseTable;
import com.matrixoptimizer.model.OptimizationResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Exports optimization runs, DP cost matrices, root tables,
 * and cost comparisons to CSV or plain text reports.
 */
public class CSVExporter {

    public static void exportResultToCSV(OptimizationResult result, File targetFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            pw.println("# MATRIX CHAIN QUERY OPTIMIZER - EXPORT REPORT");
            pw.println("# Mode: " + result.getMode());
            pw.println("# Optimal Join Expression: " + result.getOptimalExpression());
            pw.println("# Optimal Cost: " + result.getOptimalCost());
            pw.println("# Baseline Left-to-Right Cost: " + result.getLeftToRightCost());
            pw.println("# Baseline Right-to-Left Cost: " + result.getRightToLeftCost());
            pw.println(String.format("# Improvement Over Baseline: %.2f%%", result.getCostSavingPct()));
            pw.println();

            // Configured tables
            pw.println("Table Index,Table Name,Row Count,Attribute Count,Selectivity");
            int idx = 1;
            for (DatabaseTable t : result.getTables()) {
                pw.printf("%d,%s,%d,%d,%.4f%n", idx++, t.getName(), t.getRowCount(), t.getAttributeCount(), t.getSelectivity());
            }
            pw.println();

            // DP Cost Table
            long[][] dp = result.getDpTable();
            int n = result.getTables().size();
            pw.println("DP Cost Table dp[i][j]:");
            pw.print("i / j");
            for (int j = 1; j <= n; j++) {
                pw.print("," + result.getTables().get(j - 1).getName());
            }
            pw.println();

            for (int i = 1; i <= n; i++) {
                pw.print(result.getTables().get(i - 1).getName());
                for (int j = 1; j <= n; j++) {
                    if (j < i) {
                        pw.print(",-");
                    } else {
                        pw.print("," + dp[i][j]);
                    }
                }
                pw.println();
            }
            pw.println();

            // Root Split Table
            int[][] root = result.getRootTable();
            pw.println("Root Split Table root[i][j]:");
            pw.print("i / j");
            for (int j = 1; j <= n; j++) {
                pw.print("," + result.getTables().get(j - 1).getName());
            }
            pw.println();

            for (int i = 1; i <= n; i++) {
                pw.print(result.getTables().get(i - 1).getName());
                for (int j = 1; j <= n; j++) {
                    if (j <= i) {
                        pw.print(",-");
                    } else {
                        pw.print("," + root[i][j]);
                    }
                }
                pw.println();
            }
        }
    }

    public static String generateTextReport(OptimizationResult result, String sql) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append("           MATRIX CHAIN QUERY OPTIMIZER - AUDIT REPORT            \n");
        sb.append("=================================================================\n\n");
        sb.append("Optimization Mode : ").append(result.getMode()).append("\n");
        sb.append("SQL Query Input   : \n").append(sql != null ? sql : "N/A").append("\n\n");
        sb.append("Tables In Join Chain:\n");
        for (int i = 0; i < result.getTables().size(); i++) {
            DatabaseTable t = result.getTables().get(i);
            sb.append(String.format("  [%d] %-15s (Tuples: %,d, Attributes: %d, Sel: %.3f)\n",
                    i + 1, t.getName(), t.getRowCount(), t.getAttributeCount(), t.getSelectivity()));
        }

        sb.append("\nOptimal Join Plan:\n");
        sb.append("  ").append(result.getOptimalExpression()).append("\n\n");

        sb.append("Cost Evaluation Summary:\n");
        sb.append(String.format("  Optimal Plan Cost       : %,d\n", result.getOptimalCost()));
        sb.append(String.format("  Left-to-Right Plan Cost : %,d\n", result.getLeftToRightCost()));
        sb.append(String.format("  Right-to-Left Plan Cost : %,d\n", result.getRightToLeftCost()));
        sb.append(String.format("  Performance Improvement : %.2f%%\n\n", result.getCostSavingPct()));

        sb.append("DP Complexity Reference:\n");
        sb.append("  Time Complexity  : O(n^3) where n = number of join tables\n");
        sb.append("  Space Complexity : O(n^2) for memoization tables\n");
        sb.append("=================================================================\n");
        return sb.toString();
    }
}
