package com.matrixoptimizer.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Interactive Java 2D Heatmap component for visualizing the dynamic programming cost table dp[i][j].
 *
 * Features:
 * - Color gradient scaling from minimum cost (cool green/cyan) to maximum cost (warm amber/red).
 * - Diagonal cells dp[i][i] highlighted as zero cost base cases.
 * - Blank/shaded lower triangular region (j < i) representing non-evaluated subproblems.
 * - Optimal global solution cell dp[1][n] emphasized with golden accent border.
 * - Interactive cell hover and click showing split point k, left subproblem dp[i][k], and right subproblem dp[k+1][j].
 */
public class DPHeatmapPanel extends JPanel {

    private long[][] dpTable;
    private int[][] rootTable;
    private List<String> tableNames;
    private int n = 0;
    private int selectedI = -1;
    private int selectedJ = -1;
    private JLabel statusLabel;

    public DPHeatmapPanel() {
        setBackground(new Color(15, 23, 42)); // Slate-900
        setLayout(new BorderLayout());

        statusLabel = new JLabel("Click any cell to inspect interval recurrence and optimal split point k.");
        statusLabel.setForeground(new Color(148, 163, 184));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(statusLabel, BorderLayout.SOUTH);

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCellClick(e.getX(), e.getY());
            }
        };
        addMouseListener(mouseHandler);
    }

    public void updateData(long[][] dp, int[][] root, List<String> names) {
        this.dpTable = dp;
        this.rootTable = root;
        this.tableNames = names;
        this.n = (dp != null && dp.length > 0) ? dp.length - 1 : 0;
        this.selectedI = (n > 0) ? 1 : -1;
        this.selectedJ = (n > 0) ? n : -1;
        updateStatusText();
        repaint();
    }

    private void handleCellClick(int mouseX, int mouseY) {
        if (n <= 0) return;
        int cellSize = calculateCellSize();
        int offsetX = 100;
        int offsetY = 60;

        int j = (mouseX - offsetX) / cellSize + 1;
        int i = (mouseY - offsetY) / cellSize + 1;

        if (i >= 1 && i <= n && j >= 1 && j <= n && j >= i) {
            selectedI = i;
            selectedJ = j;
            updateStatusText();
            repaint();
        }
    }

    private void updateStatusText() {
        if (selectedI >= 1 && selectedJ >= selectedI && dpTable != null) {
            long cost = dpTable[selectedI][selectedJ];
            int split = (rootTable != null) ? rootTable[selectedI][selectedJ] : -1;
            String tI = (tableNames != null && selectedI <= tableNames.size()) ? tableNames.get(selectedI - 1) : ("T" + selectedI);
            String tJ = (tableNames != null && selectedJ <= tableNames.size()) ? tableNames.get(selectedJ - 1) : ("T" + selectedJ);

            if (selectedI == selectedJ) {
                statusLabel.setText(String.format("Base Case [%d..%d] (%s): Cost = 0 (single table, no join needed)", selectedI, selectedJ, tI));
            } else {
                statusLabel.setText(String.format("Selected Interval [%d..%d] (%s to %s) | Min Cost = %,d | Optimal Split k = %d (Left: [%d..%d], Right: [%d..%d])",
                        selectedI, selectedJ, tI, tJ, cost, split, selectedI, split, split + 1, selectedJ));
            }
        }
    }

    private int calculateCellSize() {
        int availW = Math.max(200, getWidth() - 140);
        int availH = Math.max(200, getHeight() - 100);
        int maxDim = Math.max(1, n);
        return Math.min(80, Math.max(45, Math.min(availW / maxDim, availH / maxDim)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        if (n <= 0 || dpTable == null) {
            g2.setColor(new Color(148, 163, 184));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2.drawString("No optimization data loaded. Run an optimization to render the DP table.", 40, 80);
            return;
        }

        // Find min and max for color normalization
        long minCost = Long.MAX_VALUE;
        long maxCost = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                if (dpTable[i][j] < Long.MAX_VALUE) {
                    if (dpTable[i][j] < minCost) minCost = dpTable[i][j];
                    if (dpTable[i][j] > maxCost) maxCost = dpTable[i][j];
                }
            }
        }
        if (minCost == Long.MAX_VALUE) minCost = 0;
        if (maxCost == 0) maxCost = 1;

        int cellSize = calculateCellSize();
        int offsetX = 110;
        int offsetY = 60;

        // Draw Column Headers (j)
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        for (int j = 1; j <= n; j++) {
            String colName = (tableNames != null && j <= tableNames.size()) ? tableNames.get(j - 1) : ("T" + j);
            if (colName.length() > 8) colName = colName.substring(0, 7) + "…";
            g2.setColor(new Color(203, 213, 225));
            g2.drawString("j=" + j, offsetX + (j - 1) * cellSize + 10, offsetY - 25);
            g2.drawString(colName, offsetX + (j - 1) * cellSize + 4, offsetY - 8);
        }

        // Draw Row Headers (i) and Cells
        for (int i = 1; i <= n; i++) {
            String rowName = (tableNames != null && i <= tableNames.size()) ? tableNames.get(i - 1) : ("T" + i);
            if (rowName.length() > 8) rowName = rowName.substring(0, 7) + "…";
            g2.setColor(new Color(203, 213, 225));
            g2.drawString("i=" + i + " " + rowName, 15, offsetY + (i - 1) * cellSize + cellSize / 2 + 5);

            for (int j = 1; j <= n; j++) {
                int x = offsetX + (j - 1) * cellSize;
                int y = offsetY + (i - 1) * cellSize;

                if (j < i) {
                    // Lower triangle - invalid
                    g2.setColor(new Color(30, 41, 59, 100));
                    g2.fillRect(x, y, cellSize, cellSize);
                    g2.setColor(new Color(51, 65, 85));
                    g2.drawRect(x, y, cellSize, cellSize);
                } else if (i == j) {
                    // Base case diagonal (cost 0)
                    g2.setColor(new Color(16, 185, 129, 30));
                    g2.fillRect(x, y, cellSize, cellSize);
                    g2.setColor(new Color(16, 185, 129, 120));
                    g2.drawRect(x, y, cellSize, cellSize);

                    g2.setColor(new Color(52, 211, 153));
                    g2.setFont(new Font("Monospaced", Font.BOLD, 13));
                    g2.drawString("0", x + cellSize / 2 - 4, y + cellSize / 2 + 5);
                } else {
                    // Upper triangle: populated DP subproblem
                    long cost = dpTable[i][j];
                    float ratio = (maxCost > minCost) ? (float) (cost - minCost) / (maxCost - minCost) : 0f;
                    ratio = Math.max(0f, Math.min(1f, ratio));

                    Color cellBg = interpolateColor(ratio);
                    g2.setColor(cellBg);
                    g2.fillRect(x, y, cellSize, cellSize);

                    // Cell border
                    g2.setColor(new Color(51, 65, 85));
                    g2.drawRect(x, y, cellSize, cellSize);

                    // Value text
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Monospaced", Font.BOLD, 11));
                    String valStr = formatCostCompact(cost);
                    FontMetrics fm = g2.getFontMetrics();
                    int textW = fm.stringWidth(valStr);
                    g2.drawString(valStr, x + (cellSize - textW) / 2, y + cellSize / 2 - 2);

                    // Root split text
                    if (rootTable != null && rootTable[i][j] > 0) {
                        g2.setColor(new Color(253, 224, 71));
                        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                        String kStr = "k=" + rootTable[i][j];
                        int kW = g2.getFontMetrics().stringWidth(kStr);
                        g2.drawString(kStr, x + (cellSize - kW) / 2, y + cellSize / 2 + 14);
                    }
                }

                // Global optimal cell highlight (i=1, j=n)
                if (i == 1 && j == n) {
                    g2.setColor(new Color(245, 158, 11));
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRect(x + 2, y + 2, cellSize - 4, cellSize - 4);
                }

                // Currently clicked cell highlight
                if (i == selectedI && j == selectedJ) {
                    g2.setColor(new Color(59, 130, 246));
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                }
            }
        }
    }

    private Color interpolateColor(float ratio) {
        // Gradient from dark teal (low cost) to deep purple/red (high cost)
        int r = (int) (20 + ratio * 160);
        int g = (int) (60 + (1.0f - ratio) * 70);
        int b = (int) (120 + (1.0f - ratio) * 50);
        return new Color(r, g, b, 210);
    }

    private String formatCostCompact(long cost) {
        if (cost >= 1_000_000_000) return String.format("%.1fB", cost / 1e9);
        if (cost >= 1_000_000) return String.format("%.1fM", cost / 1e6);
        if (cost >= 1_000) return String.format("%.1fK", cost / 1e3);
        return String.valueOf(cost);
    }
}
