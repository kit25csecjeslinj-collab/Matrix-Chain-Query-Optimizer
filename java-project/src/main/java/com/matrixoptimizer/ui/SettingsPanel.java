package com.matrixoptimizer.ui;

import com.matrixoptimizer.database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;

/**
 * Settings and Database Connectivity screen.
 * Allows configuring MySQL host, port, credentials, and testing JDBC connections.
 */
public class SettingsPanel extends JPanel {

    private final JTextField txtHost;
    private final JTextField txtPort;
    private final JTextField txtDatabase;
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JLabel lblStatus;

    public SettingsPanel() {
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Database Connectivity & System Configuration");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Configure MySQL 8 database connection credentials. If unavailable, the in-memory engine runs seamlessly.");
        subtitle.setForeground(new Color(148, 163, 184));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(new Color(30, 41, 59));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 8, 8);

        txtHost = new JTextField(DatabaseConnection.getHost(), 20);
        txtPort = new JTextField(String.valueOf(DatabaseConnection.getPort()), 20);
        txtDatabase = new JTextField(DatabaseConnection.getDatabase(), 20);
        txtUsername = new JTextField(DatabaseConnection.getUsername(), 20);
        txtPassword = new JPasswordField(20);

        addField(formCard, c, 0, "MySQL Host:", txtHost);
        addField(formCard, c, 1, "Port:", txtPort);
        addField(formCard, c, 2, "Database Name:", txtDatabase);
        addField(formCard, c, 3, "Username:", txtUsername);
        addField(formCard, c, 4, "Password:", txtPassword);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        buttonRow.setOpaque(false);

        JButton btnTest = new JButton("Test MySQL Connection");
        btnTest.setBackground(new Color(59, 130, 246));
        btnTest.setForeground(Color.WHITE);
        btnTest.setFocusPainted(false);

        JButton btnSave = new JButton("Save & Apply");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);

        buttonRow.add(btnTest);
        buttonRow.add(btnSave);

        c.gridx = 1;
        c.gridy = 5;
        formCard.add(buttonRow, c);

        lblStatus = new JLabel("Status: Running in standalone In-Memory Demonstration Mode (No MySQL required).");
        lblStatus.setForeground(new Color(253, 224, 71));
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 2;
        formCard.add(lblStatus, c);

        btnTest.addActionListener(e -> testConnection());
        btnSave.addActionListener(e -> saveConfig());

        add(header, BorderLayout.NORTH);
        add(formCard, BorderLayout.CENTER);
    }

    private void addField(JPanel panel, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(203, 213, 225));
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(lbl, c);

        c.gridx = 1;
        comp.setFont(new Font("SansSerif", Font.PLAIN, 12));
        comp.setBackground(new Color(15, 23, 42));
        comp.setForeground(Color.WHITE);
        comp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        panel.add(comp, c);
    }

    private void saveConfig() {
        try {
            int port = Integer.parseInt(txtPort.getText().trim());
            DatabaseConnection.configure(
                    txtHost.getText().trim(),
                    port,
                    txtDatabase.getText().trim(),
                    txtUsername.getText().trim(),
                    new String(txtPassword.getPassword())
            );
            JOptionPane.showMessageDialog(this, "Database configuration updated.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port must be an integer.", "Invalid Port", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void testConnection() {
        saveConfig();
        boolean connected = DatabaseConnection.isConnected();
        if (connected) {
            lblStatus.setForeground(new Color(52, 211, 153));
            lblStatus.setText("Status: Successfully connected to MySQL 8 (" + DatabaseConnection.getJdbcUrl() + ").");
            JOptionPane.showMessageDialog(this, "Connected successfully to MySQL!", "Connection OK", JOptionPane.INFORMATION_MESSAGE);
        } else {
            lblStatus.setForeground(new Color(248, 113, 113));
            lblStatus.setText("Status: Failed to connect (" + DatabaseConnection.getLastErrorMessage() + "). Using in-memory fallback.");
            JOptionPane.showMessageDialog(this, "Could not connect to MySQL:\n" + DatabaseConnection.getLastErrorMessage() +
                    "\n\nNote: The application will run smoothly in In-Memory Demo mode.", "Notice", JOptionPane.WARNING_MESSAGE);
        }
    }
}
