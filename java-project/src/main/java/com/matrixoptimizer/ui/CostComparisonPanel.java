package com.matrixoptimizer.ui;

import com.matrixoptimizer.model.OptimizationResult;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Visual cost comparison panel:
 * Compares the optimal DP join order against Left-to-Right and Right-to-Left baselines.
 * Displays comparative table and Java 2D horizontal bar charts.
 */
public class CostComparisonPanel extends JPanel {

    private final JTable comparisonTable;
    private final DefaultTableModel tableModel;
    private final ChartCanvas chartCanvas;

    public CostComparisonPanel() {
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Table Model
        String[] columnNames = {"Join Plan Evaluation Strategy", "Estimated Cost", "Cost Difference", "Savings / Improvement"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        comparisonTable = new JTable(tableModel);
        comparisonTable.setRowHeight(32);
        comparisonTable.setBackground(new Color(30, 41, 59));
        comparisonTable.setForeground(Color.WHITE);
        comparisonTable.getTableHeader().setBackground(new Color(51, 65, 85));
        comparisonTable.getTableHeader().setForeground(new Color(226, 232, 240));
        comparisonTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(comparisonTable);
        scrollPane.setPreferredSize(new Dimension(800, 160));
        scrollPane.getViewport().setBackground(new Color(30, 41, 59));

        chartCanvas = new ChartCanvas();
        chartCanvas.setPreferredSize(new Dimension(800, 260));

        add(scrollPane, BorderLayout.NORTH);
        add(chartCanvas, BorderLayout.CENTER);
    }

    public void updateResult(OptimizationResult res) {
        tableModel.setRowCount(0);
        if (res == null) {
            chartCanvas.setData(0, 0, 0);
            return;
        }

        long opt = res.getOptimalCost();
        long l2r = res.getLeftToRightCost();
        long r2l = res.getRightToLeftCost();

        tableModel.addRow(new Object[]{"★ Optimal Plan (Dynamic Programming)", String.format("%,d", opt), "-", "0.00% (Baseline Optimal)"});
        tableModel.addRow(new Object[]{"Left-to-Right Sequence (((A JOIN B) JOIN C)...)", String.format("%,d", l2r), String.format("+%,d", l2r - opt), String.format("%.2f%% more expensive", computeExtra(l2r, opt))});
        tableModel.addRow(new Object[]{"Right-to-Left Sequence (A JOIN (B JOIN (C...)))", String.format("%,d", r2l), String.format("+%,d", r2l - opt), String.format("%.2f%% more expensive", computeExtra(r2l, opt))});

        chartCanvas.setData(opt, l2r, r2l);
    }

    private double computeExtra(long baseline, long opt) {
        if (opt <= 0) return 0.0;
        return ((double) (baseline - opt) / opt) * 100.0;
    }

    private static class ChartCanvas extends JPanel {
        private long optCost = 0;
        private long l2rCost = 0;
        private long r2lCost = 0;

        public ChartCanvas() {
            setBackground(new Color(15, 23, 42));
        }

        public void setData(long opt, long l2r, long r2l) {
            this.optCost = opt;
            this.l2rCost = l2r;
            this.r2lCost = r2l;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (optCost == 0 && l2rCost == 0) return;

            long maxVal = Math.max(optCost, Math.max(l2rCost, r2lCost));
            if (maxVal == 0) maxVal = 1;

            int startY = 30;
            int barHeight = 36;
            int maxBarWidth = getWidth() - 320;
            if (maxBarWidth < 100) maxBarWidth = 100;

            drawBar(g2, "Optimal Plan (DP)", optCost, maxVal, 20, startY, maxBarWidth, barHeight, new Color(16, 185, 129));
            drawBar(g2, "Left-to-Right Plan", l2rCost, maxVal, 20, startY + 60, maxBarWidth, barHeight, new Color(59, 130, 246));
            drawBar(g2, "Right-to-Left Plan", r2lCost, maxVal, 20, startY + 120, maxBarWidth, barHeight, new Color(239, 68, 68));
        }

        private void drawBar(Graphics2D g2, String label, long value, long maxVal, int x, int y, int maxW, int h, Color color) {
            g2.setColor(new Color(226, 232, 240));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(label, x, y - 6);

            int barW = (int) (((double) value / maxVal) * maxW);
            barW = Math.max(8, barW);

            g2.setColor(color);
            g2.fillRoundRect(x, y, barW, h, 8, 8);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            String valStr = String.format("%,d", value);
            g2.drawString(valStr, x + barW + 12, y + h / 2 + 5);
        }
    }
}
