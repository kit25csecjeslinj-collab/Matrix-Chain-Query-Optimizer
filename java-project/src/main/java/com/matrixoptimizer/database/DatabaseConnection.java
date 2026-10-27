package com.matrixoptimizer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages database connectivity to MySQL 8.
 * Provides configurable host, port, credentials, and transparent fallback
 * to an in-memory repository when a live MySQL instance is not accessible.
 */
public class DatabaseConnection {

    private static String host = "localhost";
    private static int port = 3306;
    private static String database = "matrix_chain_optimizer";
    private static String username = "root";
    private static String password = ""; // Configurable in UI settings

    private static boolean inMemoryFallback = true;
    private static String lastErrorMessage = null;

    public static synchronized void configure(String newHost, int newPort, String newDb, String newUser, String newPass) {
        host = newHost;
        port = newPort;
        database = newDb;
        username = newUser;
        password = newPass;
    }

    public static String getJdbcUrl() {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port, database);
    }

    /**
     * Attempts to acquire a live MySQL connection.
     * Returns null if connection fails (allowing repositories to use in-memory state).
     */
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(getJdbcUrl(), username, password);
            lastErrorMessage = null;
            return conn;
        } catch (ClassNotFoundException e) {
            lastErrorMessage = "MySQL JDBC Driver not found on classpath: " + e.getMessage();
            return null;
        } catch (SQLException e) {
            lastErrorMessage = "Cannot connect to MySQL at " + host + ":" + port + " (" + e.getMessage() + ")";
            return null;
        }
    }

    public static boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public static boolean isInMemoryFallback() {
        return inMemoryFallback;
    }

    public static void setInMemoryFallback(boolean enable) {
        inMemoryFallback = enable;
    }

    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getDatabase() { return database; }
    public static String getUsername() { return username; }
}
