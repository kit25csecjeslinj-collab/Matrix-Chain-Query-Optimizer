package com.matrixoptimizer.ui;

import com.matrixoptimizer.database.OptimizationRepository;
import com.matrixoptimizer.database.TableRepository;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Executive project dashboard for overview metrics, past runs, and quick actions.
 */
public class DashboardPanel extends JPanel {

    private final TableRepository tableRepo;
    private final OptimizationRepository optRepo;

    private final JLabel lblTableCount;
    private final JLabel lblRunCount;
    private final JLabel lblLowestCost;
    private final JLabel lblAvgSaving;

    private final DefaultTableModel historyModel;

    public DashboardPanel(TableRepository tableRepo, OptimizationRepository optRepo, Runnable onLaunchOptimizer) {
        this.tableRepo = tableRepo;
        this.optRepo = optRepo;

        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Matrix Chain Query Optimizer — Overview");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        JLabel desc = new JLabel("Simulating dynamic programming join-order selection for relational databases.");
        desc.setForeground(new Color(148, 163, 184));
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(desc, BorderLayout.SOUTH);

        // Metric Cards Grid
        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 15, 15));
        metricsGrid.setOpaque(false);

        lblTableCount = new JLabel("5");
        lblRunCount = new JLabel("0");
        lblLowestCost = new JLabel("—");
        lblAvgSaving = new JLabel("—");

        metricsGrid.add(createMetricCard("Configured Tables", lblTableCount, "Active database relations", new Color(59, 130, 246)));
        metricsGrid.add(createMetricCard("Optimization Runs", lblRunCount, "Total evaluated queries", new Color(168, 85, 247)));
        metricsGrid.add(createMetricCard("Lowest Estimated Cost", lblLowestCost, "Minimum scalar operations", new Color(16, 185, 129)));
        metricsGrid.add(createMetricCard("Max Cost Reduction", lblAvgSaving, "Savings vs Left-to-Right", new Color(245, 158, 11)));

        // Recent History Table
        JPanel historySection = new JPanel(new BorderLayout(10, 10));
        historySection.setOpaque(false);
        JLabel historyTitle = new JLabel("Recent Query Optimizations");
        historyTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        historyTitle.setForeground(Color.WHITE);
        historySection.add(historyTitle, BorderLayout.NORTH);

        String[] cols = {"Run ID", "Mode", "Table Sequence", "Optimal Join Order", "Optimal Cost", "L-to-R Cost", "Savings %", "Timestamp"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(30);
        historyTable.setBackground(new Color(30, 41, 59));
        historyTable.setForeground(Color.WHITE);
        historyTable.getTableHeader().setBackground(new Color(51, 65, 85));
        historyTable.getTableHeader().setForeground(new Color(226, 232, 240));

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.getViewport().setBackground(new Color(30, 41, 59));
        historySection.add(scroll, BorderLayout.CENTER);

        // Quick action banner
        JPanel actionBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        actionBanner.setOpaque(false);
        JButton btnGoOptimize = new JButton("Open SQL Query Planner →");
        btnGoOptimize.setBackground(new Color(99, 102, 241));
        btnGoOptimize.setForeground(Color.WHITE);
        btnGoOptimize.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnGoOptimize.setFocusPainted(false);
        btnGoOptimize.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btnGoOptimize.addActionListener(e -> {
            if (onLaunchOptimizer != null) onLaunchOptimizer.run();
        });
        actionBanner.add(btnGoOptimize);

        JPanel centerContainer = new JPanel(new BorderLayout(20, 20));
        centerContainer.setOpaque(false);
        centerContainer.add(metricsGrid, BorderLayout.NORTH);
        centerContainer.add(historySection, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
        add(actionBanner, BorderLayout.SOUTH);

        refresh();
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLbl.setForeground(new Color(148, 163, 184));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subLbl.setForeground(new Color(100, 116, 139));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        return card;
    }

    public void refresh() {
        int tableCount = tableRepo.findAll().size();
        lblTableCount.setText(String.valueOf(tableCount));

        List<OptimizationRepository.RunRecord> runs = optRepo.findAll();
        lblRunCount.setText(String.valueOf(runs.size()));

        if (!runs.isEmpty()) {
            long minCost = Long.MAX_VALUE;
            double maxSaving = 0.0;
            historyModel.setRowCount(0);
            for (OptimizationRepository.RunRecord r : runs) {
                if (r.getOptimalCost() < minCost) minCost = r.getOptimalCost();
                if (r.getCostSavingPct() > maxSaving) maxSaving = r.getCostSavingPct();
                historyModel.addRow(new Object[]{
                        r.getId(),
                        r.getMode(),
                        r.getTableSequence(),
                        r.getOptimalJoinOrder(),
                        String.format("%,d", r.getOptimalCost()),
                        String.format("%,d", r.getLeftToRightCost()),
                        String.format("%.2f%%", r.getCostSavingPct()),
                        r.getTimestamp()
                });
            }
            lblLowestCost.setText(String.format("%,d", minCost));
            lblAvgSaving.setText(String.format("%.1f%%", maxSaving));
        } else {
            lblLowestCost.setText("—");
            lblAvgSaving.setText("—");
        }
    }
}
