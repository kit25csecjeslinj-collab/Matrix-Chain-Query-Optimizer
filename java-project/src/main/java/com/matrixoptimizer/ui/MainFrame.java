package com.matrixoptimizer.ui;

import com.matrixoptimizer.database.OptimizationRepository;
import com.matrixoptimizer.database.QueryRepository;
import com.matrixoptimizer.database.TableRepository;
import com.matrixoptimizer.model.OptimizationResult;
import javax.swing.*;
import java.awt.*;

/**
 * Main application window for the Matrix Chain Query Optimizer desktop suite.
 * Features a modern sidebar navigation bar and modular card views.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel contentCards;

    private final DashboardPanel dashboardPanel;
    private final TableConfigurationPanel tableConfigPanel;
    private final QueryEditorPanel queryEditorPanel;
    private final OptimizationResultPanel resultPanel;
    private final SettingsPanel settingsPanel;

    private final TableRepository tableRepo;
    private final QueryRepository queryRepo;
    private final OptimizationRepository optRepo;

    public MainFrame() {
        super("Matrix Chain Query Optimizer — Academic Database Query Planner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 820);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);

        // Repositories
        tableRepo = new TableRepository();
        queryRepo = new QueryRepository();
        optRepo = new OptimizationRepository();

        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);

        // Initialize panels
        resultPanel = new OptimizationResultPanel();
        dashboardPanel = new DashboardPanel(tableRepo, optRepo, () -> switchCard("QUERY_PLANNER"));
        tableConfigPanel = new TableConfigurationPanel(tableRepo, () -> {
            dashboardPanel.refresh();
            queryEditorPanel.refreshSavedQueries();
        });

        queryEditorPanel = new QueryEditorPanel(tableRepo, queryRepo, (OptimizationResult res) -> {
            // Save run
            optRepo.recordRun(
                    res.getMode(),
                    "Sequence of " + res.getTables().size() + " tables",
                    res.getOptimalExpression(),
                    res.getOptimalCost(),
                    res.getLeftToRightCost(),
                    res.getCostSavingPct()
            );
            dashboardPanel.refresh();
            resultPanel.displayResult(res);
            switchCard("RESULTS");
        });

        settingsPanel = new SettingsPanel();

        // Add to card stack
        contentCards.add(dashboardPanel, "DASHBOARD");
        contentCards.add(tableConfigPanel, "TABLES");
        contentCards.add(queryEditorPanel, "QUERY_PLANNER");
        contentCards.add(resultPanel, "RESULTS");
        contentCards.add(settingsPanel, "SETTINGS");

        // Layout container
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(15, 23, 42));

        root.add(createSidebar(), BorderLayout.WEST);
        root.add(contentCards, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(2, 6, 23)); // Slate-950
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(30, 41, 59)));

        // Branding
        JPanel brand = new JPanel(new BorderLayout(5, 5));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(20, 18, 20, 18));

        JLabel title = new JLabel("Matrix Chain");
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Query Optimizer v1.0");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(129, 140, 248)); // Indigo-400

        brand.add(title, BorderLayout.NORTH);
        brand.add(sub, BorderLayout.SOUTH);
        sidebar.add(brand);

        sidebar.add(Box.createVerticalStrut(10));

        // Navigation Buttons
        sidebar.add(createNavButton("Dashboard Overview", "DASHBOARD"));
        sidebar.add(createNavButton("Table & Schema Catalog", "TABLES"));
        sidebar.add(createNavButton("SQL Query Planner", "QUERY_PLANNER"));
        sidebar.add(createNavButton("Optimization Results & DP", "RESULTS"));
        sidebar.add(createNavButton("Database & Settings", "SETTINGS"));

        sidebar.add(Box.createVerticalGlue());

        // Educational notice
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(15, 18, 20, 18));
        JLabel course = new JLabel("CS 400 - Database Systems");
        course.setFont(new Font("SansSerif", Font.BOLD, 10));
        course.setForeground(new Color(100, 116, 139));
        JLabel algo = new JLabel("Dynamic Programming O(n³)");
        algo.setFont(new Font("SansSerif", Font.PLAIN, 10));
        algo.setForeground(new Color(71, 85, 105));
        footer.add(course, BorderLayout.NORTH);
        footer.add(algo, BorderLayout.SOUTH);
        sidebar.add(footer);

        return sidebar;
    }

    private JButton createNavButton(String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(240, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(new Color(2, 6, 23));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(30, 41, 59));
                btn.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(2, 6, 23));
                btn.setForeground(new Color(203, 213, 225));
            }
        });

        btn.addActionListener(e -> switchCard(cardName));
        return btn;
    }

    private void switchCard(String name) {
        cardLayout.show(contentCards, name);
    }
}
