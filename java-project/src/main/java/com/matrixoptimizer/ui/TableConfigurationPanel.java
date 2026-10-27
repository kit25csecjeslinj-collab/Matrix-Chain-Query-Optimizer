package com.matrixoptimizer.ui;

import com.matrixoptimizer.database.TableRepository;
import com.matrixoptimizer.model.DatabaseTable;
import com.matrixoptimizer.util.InputValidator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Table and Matrix Configuration UI screen.
 * Supports adding, editing, deleting, and loading demo datasets.
 */
public class TableConfigurationPanel extends JPanel {

    private final TableRepository repository;
    private final DefaultTableModel tableModel;
    private final JTable tableGrid;
    private final Runnable onDataChangedCallback;

    public TableConfigurationPanel(TableRepository repository, Runnable onDataChangedCallback) {
        this.repository = repository;
        this.onDataChangedCallback = onDataChangedCallback;

        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Database Table & Cardinality Catalog");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        JLabel subLabel = new JLabel("Configure relations, tuple counts (rows), schema widths (attributes), and join selectivities.");
        subLabel.setForeground(new Color(148, 163, 184));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);

        // Data Table
        String[] columns = {"ID", "Table Name", "Tuples / Rows (Card)", "Attributes (Cols)", "Selectivity", "Join Key", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tableGrid = new JTable(tableModel);
        tableGrid.setRowHeight(32);
        tableGrid.setBackground(new Color(30, 41, 59));
        tableGrid.setForeground(Color.WHITE);
        tableGrid.getTableHeader().setBackground(new Color(51, 65, 85));
        tableGrid.getTableHeader().setForeground(new Color(226, 232, 240));

        JScrollPane scrollPane = new JScrollPane(tableGrid);
        scrollPane.getViewport().setBackground(new Color(30, 41, 59));

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setOpaque(false);

        JButton btnAdd = createStyledButton("Add Table", new Color(16, 185, 129));
        JButton btnEdit = createStyledButton("Edit Table", new Color(59, 130, 246));
        JButton btnRemove = createStyledButton("Remove Table", new Color(239, 68, 68));
        JButton btnLoadDemo = createStyledButton("Load Demo Dataset", new Color(99, 102, 241));
        JButton btnReset = createStyledButton("Reset Default", new Color(100, 116, 139));

        btnAdd.addActionListener(e -> showAddEditDialog(null));
        btnEdit.addActionListener(e -> {
            int row = tableGrid.getSelectedRow();
            if (row >= 0) {
                String name = (String) tableModel.getValueAt(row, 1);
                DatabaseTable target = repository.findAll().stream()
                        .filter(t -> t.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                if (target != null) showAddEditDialog(target);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnRemove.addActionListener(e -> {
            int row = tableGrid.getSelectedRow();
            if (row >= 0) {
                String name = (String) tableModel.getValueAt(row, 1);
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove table '" + name + "'?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    repository.deleteByName(name);
                    refreshTable();
                    notifyChange();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a table to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnLoadDemo.addActionListener(e -> {
            repository.resetToDefault();
            refreshTable();
            notifyChange();
            JOptionPane.showMessageDialog(this, "Demo dataset (5 tables: Customers, Orders, OrderItems, Products, Payments) loaded.", "Demo Loaded", JOptionPane.INFORMATION_MESSAGE);
        });

        btnReset.addActionListener(e -> {
            repository.resetToDefault();
            refreshTable();
            notifyChange();
        });

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnRemove);
        toolbar.add(btnLoadDemo);
        toolbar.add(btnReset);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(toolbar, BorderLayout.SOUTH);

        refreshTable();
    }

    private void notifyChange() {
        if (onDataChangedCallback != null) onDataChangedCallback.run();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<DatabaseTable> tables = repository.findAll();
        for (DatabaseTable t : tables) {
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getName(),
                    String.format("%,d", t.getRowCount()),
                    t.getAttributeCount(),
                    String.format("%.3f", t.getSelectivity()),
                    t.getPrimaryJoinKey(),
                    t.getDescription()
            });
        }
    }

    private void showAddEditDialog(DatabaseTable existing) {
        boolean isEdit = existing != null;
        JTextField txtName = new JTextField(isEdit ? existing.getName() : "");
        JTextField txtRows = new JTextField(isEdit ? String.valueOf(existing.getRowCount()) : "10000");
        JTextField txtCols = new JTextField(isEdit ? String.valueOf(existing.getAttributeCount()) : "10");
        JTextField txtSel = new JTextField(isEdit ? String.valueOf(existing.getSelectivity()) : "0.05");
        JTextField txtKey = new JTextField(isEdit ? existing.getPrimaryJoinKey() : "id");
        JTextField txtDesc = new JTextField(isEdit ? existing.getDescription() : "");

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Table Name:"));
        form.add(txtName);
        form.add(new JLabel("Tuple / Row Count:"));
        form.add(txtRows);
        form.add(new JLabel("Attribute / Col Count:"));
        form.add(txtCols);
        form.add(new JLabel("Selectivity Factor (0.0 - 1.0]:"));
        form.add(txtSel);
        form.add(new JLabel("Primary Join Key:"));
        form.add(txtKey);
        form.add(new JLabel("Description:"));
        form.add(txtDesc);

        int result = JOptionPane.showConfirmDialog(this, form, isEdit ? "Edit Database Table" : "Add Database Table", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            InputValidator.ValidationResult v = InputValidator.validateTable(txtName.getText(), txtRows.getText(), txtCols.getText(), txtSel.getText());
            if (!v.isValid()) {
                JOptionPane.showMessageDialog(this, v.getMessage(), "Input Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            DatabaseTable table = new DatabaseTable(
                    isEdit ? existing.getId() : 0,
                    txtName.getText().trim(),
                    "",
                    Long.parseLong(txtRows.getText().trim()),
                    Integer.parseInt(txtCols.getText().trim()),
                    Double.parseDouble(txtSel.getText().trim()),
                    txtKey.getText().trim(),
                    txtDesc.getText().trim()
            );

            repository.save(table);
            refreshTable();
            notifyChange();
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
}
