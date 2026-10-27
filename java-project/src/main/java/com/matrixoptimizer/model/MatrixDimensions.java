package com.matrixoptimizer.model;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates matrix dimensions for a chain of n matrices or table relations:
 * Matrix A_i has dimension p[i-1] x p[i], for i = 1 to n.
 * Total length of p array is n + 1.
 */
public class MatrixDimensions {
    private final int[] p;
    private final int matrixCount;

    public MatrixDimensions(int[] p) {
        if (p == null || p.length < 2) {
            throw new IllegalArgumentException("Dimension array p must contain at least 2 elements for at least 1 matrix");
        }
        for (int i = 0; i < p.length; i++) {
            if (p[i] <= 0) {
                throw new IllegalArgumentException("Dimension at index " + i + " must be positive, got: " + p[i]);
            }
        }
        this.p = Arrays.copyOf(p, p.length);
        this.matrixCount = p.length - 1;
    }

    public static MatrixDimensions fromTables(List<DatabaseTable> tables) {
        if (tables == null || tables.isEmpty()) {
            throw new IllegalArgumentException("Table list cannot be empty");
        }
        int n = tables.size();
        int[] dims = new int[n + 1];
        dims[0] = (int) Math.min(tables.get(0).getRowCount(), 100000);
        for (int i = 0; i < n; i++) {
            dims[i + 1] = tables.get(i).getAttributeCount();
        }
        return new MatrixDimensions(dims);
    }

    public int[] getP() {
        return Arrays.copyOf(p, p.length);
    }

    public int getMatrixCount() {
        return matrixCount;
    }

    public int getRows(int matrixIndex1Based) {
        return p[matrixIndex1Based - 1];
    }

    public int getCols(int matrixIndex1Based) {
        return p[matrixIndex1Based];
    }
}
