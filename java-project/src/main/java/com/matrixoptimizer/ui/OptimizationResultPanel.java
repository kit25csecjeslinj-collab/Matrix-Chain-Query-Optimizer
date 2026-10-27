package com.matrixoptimizer.ui;

import com.matrixoptimizer.model.DatabaseTable;
import com.matrixoptimizer.model.OptimizationResult;
import com.matrixoptimizer.util.CSVExporter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Results container tabulating the DP Heatmap, Join Tree visualizer,
 * cost comparisons, algorithm trace logs, and export actions.
 */
public class OptimizationResultPanel extends JPanel {

    private final DPHeatmapPanel heatmapPanel;
    private final JoinTreePanel treePanel;
    private final CostComparisonPanel costComparisonPanel;
    private final JTextArea traceArea;
    private final JTable rootTableGrid;
    private final DefaultTableModel rootTableModel;

    private final JLabel lblOptimalExpr;
    private final JLabel lblOptimalCost;
    private final JLabel lblSavings;

    private OptimizationResult currentResult;

    public OptimizationResultPanel() {
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Summary Bar
        JPanel summaryCard = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryCard.setBackground(new Color(30, 41, 59));
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        lblOptimalExpr = new JLabel("Optimal Plan: Run query optimization to view plan.");
        lblOptimalExpr.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblOptimalExpr.setForeground(new Color(253, 224, 71));

        lblOptimalCost = new JLabel("Optimal Cost: —");
        lblOptimalCost.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblOptimalCost.setForeground(new Color(52, 211, 153));

        lblSavings = new JLabel("Cost Improvement: —");
        lblSavings.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblSavings.setForeground(new Color(96, 165, 250));

        summaryCard.add(lblOptimalExpr);
        summaryCard.add(lblOptimalCost);
        summaryCard.add(lblSavings);

        // Tabbed Pane for Multi-View
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 12));

        heatmapPanel = new DPHeatmapPanel();
        treePanel = new JoinTreePanel();
        costComparisonPanel = new CostComparisonPanel();

        // Algorithm Trace Tab
        traceArea = new JTextArea();
        traceArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        traceArea.setBackground(new Color(15, 23, 42));
        traceArea.setForeground(new Color(226, 232, 240));
        traceArea.setEditable(false);
        JScrollPane traceScroll = new JScrollPane(traceArea);

        // Root Table Tab
        rootTableModel = new DefaultTableModel();
        rootTableGrid = new JTable(rootTableModel);
        rootTableGrid.setRowHeight(30);
        rootTableGrid.setBackground(new Color(30, 41, 59));
        rootTableGrid.setForeground(Color.WHITE);
        JScrollPane rootScroll = new JScrollPane(rootTableGrid);
        rootScroll.getViewport().setBackground(new Color(30, 41, 59));

        tabs.addTab("Interactive DP Heatmap", heatmapPanel);
        tabs.addTab("Join Tree Visualizer", treePanel);
        tabs.addTab("Cost Comparison", costComparisonPanel);
        tabs.addTab("Algorithm Trace (O(n³))", traceScroll);
        tabs.addTab("Root Split Table root[i][j]", rootScroll);

        // Bottom Export Controls
        JPanel footerBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        footerBar.setOpaque(false);

        JButton btnExportCsv = new JButton("Export CSV Report");
        btnExportCsv.setBackground(new Color(59, 130, 246));
        btnExportCsv.setForeground(Color.WHITE);
        btnExportCsv.setFocusPainted(false);
        btnExportCsv.addActionListener(e -> exportCSV());

        JButton btnExportTxt = new JButton("Export Text Audit");
        btnExportTxt.setBackground(new Color(16, 185, 129));
        btnExportTxt.setForeground(Color.WHITE);
        btnExportTxt.setFocusPainted(false);
        btnExportTxt.addActionListener(e -> exportTxt());

        footerBar.add(btnExportCsv);
        footerBar.add(btnExportTxt);

        add(summaryCard, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(footerBar, BorderLayout.SOUTH);
    }

    public void displayResult(OptimizationResult res) {
        this.currentResult = res;
        lblOptimalExpr.setText("Plan: " + res.getOptimalExpression());
        lblOptimalCost.setText(String.format("Optimal Cost: %,d", res.getOptimalCost()));
        lblSavings.setText(String.format("Savings vs L-to-R: %.2f%%", res.getCostSavingPct()));

        List<String> names = new ArrayList<>();
        for (DatabaseTable t : res.getTables()) {
            names.add(t.getName());
        }

        heatmapPanel.updateData(res.getDpTable(), res.getRootTable(), names);
        treePanel.setTree(res.getJoinTreeRoot());
        costComparisonPanel.updateResult(res);

        // Update trace logs
        StringBuilder traceSb = new StringBuilder();
        traceSb.append("=== DYNAMIC PROGRAMMING INTERVAL TRACE (O(n³)) ===\n\n");
        traceSb.append("Recurrence Relation: dp[i][j] = min_{i<=k<j} { dp[i][k] + dp[k+1][j] + cost(i, k, j) }\n\n");
        for (String step : res.getStepLogs()) {
            traceSb.append(step).append("\n");
        }
        traceArea.setText(traceSb.toString());
        traceArea.setCaretPosition(0);

        // Update Root Table
        int n = res.getTables().size();
        String[] cols = new String[n + 1];
        cols[0] = "i / j";
        for (int j = 1; j <= n; j++) cols[j] = names.get(j - 1);
        rootTableModel.setDataVector(new Object[0][0], cols);

        int[][] roots = res.getRootTable();
        for (int i = 1; i <= n; i++) {
            Object[] row = new Object[n + 1];
            row[0] = names.get(i - 1);
            for (int j = 1; j <= n; j++) {
                if (j <= i) {
                    row[j] = "-";
                } else {
                    row[j] = "k=" + roots[i][j] + " (" + names.get(roots[i][j] - 1) + ")";
                }
            }
            rootTableModel.addRow(row);
        }
    }

    private void exportCSV() {
        if (currentResult == null) {
            JOptionPane.showMessageDialog(this, "No optimization result available to export.", "Export Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("matrix_optimization_report.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CSVExporter.exportResultToCSV(currentResult, fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Report successfully exported to " + fc.getSelectedFile().getName(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportTxt() {
        if (currentResult == null) {
            JOptionPane.showMessageDialog(this, "No optimization result available to export.", "Export Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("matrix_optimization_audit.txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String report = CSVExporter.generateTextReport(currentResult, "Multi-table SQL Join Optimization");
                java.nio.file.Files.writeString(fc.getSelectedFile().toPath(), report);
                JOptionPane.showMessageDialog(this, "Audit successfully exported to " + fc.getSelectedFile().getName(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
