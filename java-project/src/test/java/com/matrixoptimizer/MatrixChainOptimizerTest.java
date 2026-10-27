package com.matrixoptimizer;

import com.matrixoptimizer.algorithm.MatrixChainOptimizer;
import com.matrixoptimizer.model.DatabaseTable;
import com.matrixoptimizer.model.MatrixDimensions;
import com.matrixoptimizer.parser.SQLQueryParser;
import com.matrixoptimizer.util.InputValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test Suite for Matrix Chain Dynamic Programming and SQL Parser.
 * Covers all 10 college project testing requirements:
 * 1. Single matrix
 * 2. Two matrices
 * 3. Three matrices with standard benchmark: 10x30, 30x5, 5x60 => cost = 4500
 * 4. Five or more matrices
 * 5. Invalid dimensions
 * 6. Empty input
 * 7. Overflow protection for large dimensions
 * 8. Correct parenthesization reconstruction
 * 9. Comparison between optimal and baseline plans
 * 10. Unsupported SQL syntax and unknown table detection
 */
public class MatrixChainOptimizerTest {

    @Test
    @DisplayName("Test 1: Single Matrix (Base Case, 0 Multiplications)")
    public void testSingleMatrix() {
        int[] p = {10, 20}; // 1 matrix: 10 x 20
        MatrixChainOptimizer.DPExecutionData res = MatrixChainOptimizer.solve(p);
        assertEquals(0, res.dp[1][1], "A single matrix requires 0 scalar multiplications.");
    }

    @Test
    @DisplayName("Test 2: Two Matrices (Single Multiplication Choice)")
    public void testTwoMatrices() {
        int[] p = {10, 20, 30}; // A1(10x20), A2(20x30)
        MatrixChainOptimizer.DPExecutionData res = MatrixChainOptimizer.solve(p);
        long expectedCost = 10L * 20L * 30L; // 6000
        assertEquals(expectedCost, res.dp[1][2]);
        assertEquals(1, res.root[1][2], "Split point must be at index 1.");

        String paren = MatrixChainOptimizer.reconstructParenthesization(res.root, 1, 2, List.of("T1", "T2"));
        assertEquals("(T1 JOIN T2)", paren);
    }

    @Test
    @DisplayName("Test 3: Standard 3-Matrix Benchmark (10x30, 30x5, 5x60 -> 4,500 Cost)")
    public void testStandardThreeMatrixBenchmark() {
        // Dimensions: A1(10x30), A2(30x5), A3(5x60)
        // Array p = [10, 30, 5, 60]
        // Plan 1: (A1 * A2) * A3 = (10*30*5) + (10*5*60) = 1500 + 3000 = 4500
        // Plan 2: A1 * (A2 * A3) = (30*5*60) + (10*30*60) = 9000 + 18000 = 27000
        // Optimal cost: 4500, split at k = 2
        int[] p = {10, 30, 5, 60};
        MatrixChainOptimizer.DPExecutionData res = MatrixChainOptimizer.solve(p);

        assertEquals(4500, res.dp[1][3], "Optimal multiplication cost for [10, 30, 5, 60] must be exactly 4,500.");
        assertEquals(2, res.root[1][3], "Split point k must be 2: (A1 x A2) x A3");

        String paren = MatrixChainOptimizer.reconstructParenthesization(res.root, 1, 3, List.of("A1", "A2", "A3"));
        assertEquals("((A1 JOIN A2) JOIN A3)", paren);
    }

    @Test
    @DisplayName("Test 4: Five or More Matrices (Multi-Table Chain)")
    public void testFiveMatrices() {
        int[] p = {40, 20, 30, 10, 30, 50}; // 5 matrices
        MatrixChainOptimizer.DPExecutionData res = MatrixChainOptimizer.solve(p);

        assertTrue(res.dp[1][5] > 0, "Cost should be computed and positive.");
        assertTrue(res.root[1][5] >= 1 && res.root[1][5] < 5, "Root split must be within valid range [1..4].");
    }

    @Test
    @DisplayName("Test 5: Invalid Dimensions (Non-positive or null handling)")
    public void testInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> {
            new MatrixDimensions(new int[]{10, -5, 20});
        }, "Negative dimension must throw IllegalArgumentException.");

        assertThrows(IllegalArgumentException.class, () -> {
            new MatrixDimensions(new int[]{0, 20});
        }, "Zero dimension must throw IllegalArgumentException.");
    }

    @Test
    @DisplayName("Test 6: Empty Input (Array with fewer than 2 elements)")
    public void testEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            MatrixChainOptimizer.solve(new int[]{10});
        }, "Array with fewer than 2 elements must throw IllegalArgumentException.");

        assertThrows(IllegalArgumentException.class, () -> {
            MatrixChainOptimizer.solve(null);
        }, "Null array must throw IllegalArgumentException.");
    }

    @Test
    @DisplayName("Test 7: Overflow Protection with Very Large Dimensions")
    public void testOverflowProtection() {
        // Large dimensions that would overflow 32-bit int multiplication (e.g. 100,000^3)
        int[] p = {100000, 100000, 100000};
        MatrixChainOptimizer.DPExecutionData res = MatrixChainOptimizer.solve(p);

        // 100,000 * 100,000 * 100,000 = 10^15 (fits safely in 64-bit signed long)
        long expected = 1_000_000_000_000_000L;
        assertEquals(expected, res.dp[1][2], "Large dimension multiplication must fit safely in 64-bit long without integer overflow.");
    }

    @Test
    @DisplayName("Test 8: Correct Reconstruction of Parenthesization")
    public void testParenthesizationReconstruction() {
        int[] p = {10, 20, 30, 40};
        MatrixChainOptimizer.DPExecutionData res = MatrixChainOptimizer.solve(p);
        String expr = MatrixChainOptimizer.reconstructParenthesization(res.root, 1, 3, List.of("T1", "T2", "T3"));

        assertNotNull(expr);
        assertTrue(expr.contains("T1") && expr.contains("T2") && expr.contains("T3"));
        assertTrue(expr.startsWith("(") && expr.endsWith(")"));
    }

    @Test
    @DisplayName("Test 9: Correct Cost Comparison Between Optimal and Baseline Orders")
    public void testCostComparison() {
        int[] p = {10, 30, 5, 60};
        MatrixChainOptimizer.DPExecutionData res = MatrixChainOptimizer.solve(p);

        long optCost = res.dp[1][3]; // 4500
        long r2lCost = MatrixChainOptimizer.computeRightToLeftCost(p); // 27000

        assertTrue(optCost <= r2lCost, "Optimal DP cost must be less than or equal to arbitrary Right-to-Left baseline.");

        double saving = MatrixChainOptimizer.computeImprovementPct(r2lCost, optCost);
        // (27000 - 4500) / 27000 = 22500 / 27000 = 83.33%
        assertEquals(83.33, saving, 0.01, "Savings percentage should be approximately 83.33%.");
    }

    @Test
    @DisplayName("Test 10: SQL Parser - Valid, Unsupported Syntax and Unknown Tables")
    public void testSQLParsingAndValidation() {
        // 1. Valid multi-table join
        String validSql = "SELECT * FROM Customers C JOIN Orders O ON C.customer_id = O.customer_id JOIN Products P ON O.product_id = P.product_id;";
        SQLQueryParser.ParseResult pr = SQLQueryParser.parse(validSql);
        assertTrue(pr.isSuccess());
        assertEquals(3, pr.getTables().size());

        // 2. Unsupported SQL (no SELECT)
        String badSql = "UPDATE Customers SET active = 1;";
        SQLQueryParser.ParseResult badPr = SQLQueryParser.parse(badSql);
        assertFalse(badPr.isSuccess());

        // 3. Unknown table validation against catalog
        List<DatabaseTable> catalog = List.of(new DatabaseTable("Customers", 1000, 5));
        InputValidator.ValidationResult vr = InputValidator.validateQueryTables(List.of("Customers", "NonExistentTable"), catalog);
        assertFalse(vr.isValid(), "Non-existent table must fail validation against catalog.");
    }
}
