import { DatabaseTable, SavedQuery } from '../types';

export const INITIAL_TABLES: DatabaseTable[] = [
  {
    id: 1,
    name: 'Customers',
    alias: 'C',
    rowCount: 10000,
    attributeCount: 10,
    selectivity: 0.02,
    primaryJoinKey: 'customer_id',
    description: 'Registered customer accounts and billing addresses',
  },
  {
    id: 2,
    name: 'Orders',
    alias: 'O',
    rowCount: 50000,
    attributeCount: 8,
    selectivity: 0.05,
    primaryJoinKey: 'order_id',
    description: 'Customer order placement headers and timestamps',
  },
  {
    id: 3,
    name: 'OrderItems',
    alias: 'I',
    rowCount: 200000,
    attributeCount: 6,
    selectivity: 0.01,
    primaryJoinKey: 'order_id',
    description: 'Line item records referencing products within orders',
  },
  {
    id: 4,
    name: 'Products',
    alias: 'P',
    rowCount: 5000,
    attributeCount: 15,
    selectivity: 0.1,
    primaryJoinKey: 'product_id',
    description: 'Product catalog with prices, categories, and stock',
  },
  {
    id: 5,
    name: 'Payments',
    alias: 'Y',
    rowCount: 45000,
    attributeCount: 7,
    selectivity: 0.05,
    primaryJoinKey: 'order_id',
    description: 'Payment transactions and settlement logs',
  },
];

export const TEXTBOOK_3_TABLES: DatabaseTable[] = [
  {
    id: 1,
    name: 'Matrix_A1',
    alias: 'A1',
    rowCount: 10,
    attributeCount: 30,
    selectivity: 0.05,
    primaryJoinKey: 'id',
    description: 'Textbook Matrix A1 (10 × 30)',
  },
  {
    id: 2,
    name: 'Matrix_A2',
    alias: 'A2',
    rowCount: 30,
    attributeCount: 5,
    selectivity: 0.05,
    primaryJoinKey: 'id',
    description: 'Textbook Matrix A2 (30 × 5)',
  },
  {
    id: 3,
    name: 'Matrix_A3',
    alias: 'A3',
    rowCount: 5,
    attributeCount: 60,
    selectivity: 0.05,
    primaryJoinKey: 'id',
    description: 'Textbook Matrix A3 (5 × 60)',
  },
];

export const SAVED_QUERIES: SavedQuery[] = [
  {
    id: 1,
    name: 'E-Commerce 5-Table Join (Full Fulfillment)',
    sql: `SELECT *
FROM Customers C
JOIN Orders O ON C.customer_id = O.customer_id
JOIN OrderItems I ON O.order_id = I.order_id
JOIN Products P ON I.product_id = P.product_id
JOIN Payments Y ON O.order_id = Y.order_id;`,
    tableCount: 5,
    description: 'Real-world 5-table e-commerce relational join tree',
  },
  {
    id: 2,
    name: 'Standard 3-Matrix Textbook Benchmark (10×30, 30×5, 5×60)',
    sql: `SELECT *
FROM Matrix_A1 A1
JOIN Matrix_A2 A2 ON A1.id = A2.id
JOIN Matrix_A3 A3 ON A2.id = A3.id;`,
    tableCount: 3,
    description: 'Standard textbook problem verifying optimal scalar multiplication cost = 4,500',
  },
  {
    id: 3,
    name: 'Orders & OrderItems 2-Table Join',
    sql: `SELECT *
FROM Orders O
JOIN OrderItems I ON O.order_id = I.order_id;`,
    tableCount: 2,
    description: 'Elementary base join verification',
  },
  {
    id: 4,
    name: 'Customer Orders & Products 3-Table Join',
    sql: `SELECT *
FROM Customers C
JOIN Orders O ON C.customer_id = O.customer_id
JOIN Products P ON O.product_id = P.product_id;`,
    tableCount: 3,
    description: '3-table join illustrating intermediate cardinality reduction',
  },
];
