-- =========================================================================
-- Matrix Chain Query Optimizer - MySQL 8 Database Schema
-- Database Name: matrix_chain_optimizer
-- =========================================================================

CREATE DATABASE IF NOT EXISTS matrix_chain_optimizer
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE matrix_chain_optimizer;

-- Drop tables in order of foreign key dependency if re-initializing
DROP TABLE IF EXISTS optimization_runs;
DROP TABLE IF EXISTS saved_queries;
DROP TABLE IF EXISTS database_tables;

-- -------------------------------------------------------------------------
-- 1. Table: database_tables
-- Stores table metadata, row counts (cardinality), attribute counts (dimensions),
-- selectivity estimates, and primary join keys.
-- -------------------------------------------------------------------------
CREATE TABLE database_tables (
    table_id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(64) NOT NULL UNIQUE,
    row_count BIGINT NOT NULL CHECK (row_count > 0),
    attribute_count INT NOT NULL CHECK (attribute_count > 0),
    selectivity DOUBLE NOT NULL DEFAULT 0.05 CHECK (selectivity > 0 AND selectivity <= 1.0),
    primary_join_key VARCHAR(64),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 2. Table: saved_queries
-- Stores standard and custom SQL join queries tested by students / users.
-- -------------------------------------------------------------------------
CREATE TABLE saved_queries (
    query_id INT AUTO_INCREMENT PRIMARY KEY,
    query_name VARCHAR(128) NOT NULL,
    sql_text TEXT NOT NULL,
    table_count INT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 3. Table: optimization_runs
-- Stores past execution runs of the Matrix Chain Query Optimizer
-- Records optimal join order, baseline orders, estimated costs, and DP root decisions.
-- -------------------------------------------------------------------------
CREATE TABLE optimization_runs (
    run_id INT AUTO_INCREMENT PRIMARY KEY,
    query_id INT,
    optimization_mode VARCHAR(32) NOT NULL, -- 'MODE_A_MCM' or 'MODE_B_RELATIONAL'
    table_sequence TEXT NOT NULL,
    dimension_sequence TEXT NOT NULL,
    optimal_join_order TEXT NOT NULL,
    optimal_cost BIGINT NOT NULL,
    left_to_right_cost BIGINT NOT NULL,
    right_to_left_cost BIGINT NOT NULL,
    cost_saving_pct DOUBLE NOT NULL,
    execution_time_ms BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_opt_query FOREIGN KEY (query_id) REFERENCES saved_queries(query_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =========================================================================
-- Sample Seed Data: 5 Core Tables for E-Commerce Join Simulation
-- Includes realistic tuple counts and schema widths
-- =========================================================================
INSERT INTO database_tables (table_name, row_count, attribute_count, selectivity, primary_join_key, description) VALUES
('Customers',  10000, 10, 0.02, 'customer_id', 'E-commerce verified user accounts'),
('Orders',     50000, 8,  0.05, 'order_id',     'Customer order records linked to customer_id'),
('OrderItems', 200000, 6, 0.01, 'order_id',     'Individual line items per order linked to product_id'),
('Products',   5000,  15, 0.10, 'product_id',   'Product catalog with pricing and stock details'),
('Payments',   45000, 7,  0.05, 'order_id',     'Payment transactions linked to order_id');

-- =========================================================================
-- Sample Benchmark Query
-- =========================================================================
INSERT INTO saved_queries (query_name, sql_text, table_count, description) VALUES
('Full Order Fulfillment Join',
'SELECT *
FROM Customers C
JOIN Orders O ON C.customer_id = O.customer_id
JOIN OrderItems I ON O.order_id = I.order_id
JOIN Products P ON I.product_id = P.product_id
JOIN Payments Y ON O.order_id = Y.order_id;',
5,
'Standard 5-table relational join representing full customer fulfillment');
