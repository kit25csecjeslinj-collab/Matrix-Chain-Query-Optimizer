package com.matrixoptimizer.ui;

import com.matrixoptimizer.algorithm.JoinOrderOptimizer;
import com.matrixoptimizer.database.QueryRepository;
import com.matrixoptimizer.database.TableRepository;
import com.matrixoptimizer.model.DatabaseTable;
import com.matrixoptimizer.model.OptimizationResult;
import com.matrixoptimizer.parser.SQLQueryParser;
import com.matrixoptimizer.util.InputValidator;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * SQL Query Input and Parsing screen.
 * Allows entering multi-table JOIN SQL queries, selecting modes, validating syntax,
 * and triggering dynamic programming optimization.
 */
public class QueryEditorPanel extends JPanel {

    private final TableRepository tableRepository;
    private final QueryRepository queryRepository;
    private final Consumer<OptimizationResult> onOptimizationComplete;

    private final JTextArea sqlArea;
    private final JComboBox<QueryRepository.SavedQuery> queryDropdown;
    private final JComboBox<JoinOrderOptimizer.OptimizationMode> modeDropdown;
    private final JLabel statusLabel;

    public QueryEditorPanel(TableRepository tableRepo, QueryRepository queryRepo, Consumer<OptimizationResult> onComplete) {
        this.tableRepository = tableRepo;
        this.queryRepository = queryRepo;
        this.onOptimizationComplete = onComplete;

        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("SQL Query Optimizer & Execution Planner");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Enter a SQL SELECT statement with multi-table JOINs to compute the minimum-cost join tree.");
        subtitle.setForeground(new Color(148, 163, 184));
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        // Controls bar (Template selector + Mode)
        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        controlBar.setOpaque(false);

        controlBar.add(new JLabel("Saved Queries:"));
        queryDropdown = new JComboBox<>();
        refreshSavedQueries();
        controlBar.add(queryDropdown);

        controlBar.add(Box.createHorizontalStrut(15));
        controlBar.add(new JLabel("Optimization Mode:"));
        modeDropdown = new JComboBox<>(JoinOrderOptimizer.OptimizationMode.values());
        controlBar.add(modeDropdown);

        // SQL Input Text Area
        sqlArea = new JTextArea(12, 60);
        sqlArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        sqlArea.setBackground(new Color(30, 41, 59));
        sqlArea.setForeground(new Color(241, 245, 249));
        sqlArea.setCaretColor(Color.WHITE);
        sqlArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Load first template by default
        if (queryDropdown.getItemCount() > 0) {
            sqlArea.setText(queryDropdown.getItemAt(0).getSqlText());
        }

        queryDropdown.addActionListener(e -> {
            QueryRepository.SavedQuery sel = (QueryRepository.SavedQuery) queryDropdown.getSelectedItem();
            if (sel != null) sqlArea.setText(sel.getSqlText());
        });

        JScrollPane editorScroll = new JScrollPane(sqlArea);
        editorScroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        // Bottom Actions Toolbar
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonRow.setOpaque(false);

        JButton btnValidate = createStyledButton("Validate Query", new Color(59, 130, 246));
        JButton btnOptimize = createStyledButton("Optimize Join Order (DP)", new Color(16, 185, 129));
        JButton btnReset = createStyledButton("Clear", new Color(100, 116, 139));

        statusLabel = new JLabel("Ready. Enter SQL and click Optimize Join Order.");
        statusLabel.setForeground(new Color(203, 213, 225));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        btnValidate.addActionListener(e -> validateQuery());
        btnOptimize.addActionListener(e -> runOptimization());
        btnReset.addActionListener(e -> sqlArea.setText(""));

        buttonRow.add(btnValidate);
        buttonRow.add(btnOptimize);
        buttonRow.add(btnReset);

        bottomPanel.add(buttonRow, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(controlBar, BorderLayout.NORTH);
        centerPanel.add(editorScroll, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshSavedQueries() {
        queryDropdown.removeAllItems();
        for (QueryRepository.SavedQuery q : queryRepository.findAll()) {
            queryDropdown.addItem(q);
        }
    }

    private boolean validateQuery() {
        String sql = sqlArea.getText();
        SQLQueryParser.ParseResult pr = SQLQueryParser.parse(sql);
        if (!pr.isSuccess()) {
            statusLabel.setForeground(new Color(239, 68, 68));
            statusLabel.setText("Query Error: " + pr.getErrorMessage());
            JOptionPane.showMessageDialog(this, pr.getErrorMessage(), "SQL Parse Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        List<DatabaseTable> configured = tableRepository.findAll();
        List<String> queryTableNames = new ArrayList<>();
        for (SQLQueryParser.ParsedTableRef ref : pr.getTables()) {
            queryTableNames.add(ref.getTableName());
        }

        InputValidator.ValidationResult vr = InputValidator.validateQueryTables(queryTableNames, configured);
        if (!vr.isValid()) {
            statusLabel.setForeground(new Color(239, 68, 68));
            statusLabel.setText("Catalog Error: " + vr.getMessage());
            JOptionPane.showMessageDialog(this, vr.getMessage(), "Missing Catalog Metadata", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        statusLabel.setForeground(new Color(52, 211, 153));
        statusLabel.setText(String.format("Valid SQL: Found %d joinable tables (%s) with verified metadata.",
                pr.getTables().size(), String.join(", ", queryTableNames)));
        return true;
    }

    private void runOptimization() {
        if (!validateQuery()) return;

        String sql = sqlArea.getText();
        SQLQueryParser.ParseResult pr = SQLQueryParser.parse(sql);
        List<DatabaseTable> catalog = tableRepository.findAll();
        List<DatabaseTable> matchedTables = new ArrayList<>();

        for (SQLQueryParser.ParsedTableRef ref : pr.getTables()) {
            DatabaseTable found = catalog.stream()
                    .filter(t -> t.getName().equalsIgnoreCase(ref.getTableName()))
                    .findFirst().orElse(null);
            if (found != null) {
                // Attach alias if present
                DatabaseTable copy = new DatabaseTable(found.getId(), found.getName(), ref.getAlias(),
                        found.getRowCount(), found.getAttributeCount(), found.getSelectivity(),
                        found.getPrimaryJoinKey(), found.getDescription());
                matchedTables.add(copy);
            }
        }

        JoinOrderOptimizer.OptimizationMode mode = (JoinOrderOptimizer.OptimizationMode) modeDropdown.getSelectedItem();
        OptimizationResult res = JoinOrderOptimizer.optimize(matchedTables, null, mode);

        statusLabel.setForeground(new Color(52, 211, 153));
        statusLabel.setText(String.format("Optimal Join Order: %s | Estimated Cost: %,d", res.getOptimalExpression(), res.getOptimalCost()));

        if (onOptimizationComplete != null) {
            onOptimizationComplete.accept(res);
        }
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return btn;
    }

    public String getCurrentSql() {
        return sqlArea.getText();
    }
}
