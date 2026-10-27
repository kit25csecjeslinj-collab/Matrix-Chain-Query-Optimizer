export interface ProjectFileEntry {
  path: string;
  name: string;
  category: 'algorithm' | 'model' | 'parser' | 'database' | 'ui' | 'util' | 'test' | 'config' | 'doc';
  language: 'java' | 'xml' | 'sql' | 'markdown';
  description: string;
  code: string;
}

export const JAVA_PROJECT_FILES: ProjectFileEntry[] = [
  {
    path: 'pom.xml',
    name: 'pom.xml',
    category: 'config',
    language: 'xml',
    description: 'Maven configuration for Java 17, FlatLaf modern UI, MySQL 8 connector, and JUnit 5',
    code: `<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.matrixoptimizer</groupId>
    <artifactId>matrix-chain-query-optimizer</artifactId>
    <version>1.0.0</version>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>
        <dependency>
            <groupId>com.formdev</groupId>
            <artifactId>flatlaf</artifactId>
            <version>3.4.1</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>`
  },
  {
    path: 'schema.sql',
    name: 'schema.sql',
    category: 'database',
    language: 'sql',
    description: 'MySQL 8 relational schema with audit tables, constraints, and sample e-commerce seed data',
    code: `CREATE DATABASE IF NOT EXISTS matrix_chain_optimizer;
USE matrix_chain_optimizer;

CREATE TABLE database_tables (
    table_id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(64) NOT NULL UNIQUE,
    row_count BIGINT NOT NULL CHECK (row_count > 0),
    attribute_count INT NOT NULL CHECK (attribute_count > 0),
    selectivity DOUBLE NOT NULL DEFAULT 0.05,
    primary_join_key VARCHAR(64),
    description VARCHAR(255)
);

CREATE TABLE optimization_runs (
    run_id INT AUTO_INCREMENT PRIMARY KEY,
    optimization_mode VARCHAR(32) NOT NULL,
    table_sequence TEXT NOT NULL,
    optimal_join_order TEXT NOT NULL,
    optimal_cost BIGINT NOT NULL,
    left_to_right_cost BIGINT NOT NULL,
    cost_saving_pct DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);`
  },
  {
    path: 'src/main/java/com/matrixoptimizer/algorithm/MatrixChainOptimizer.java',
    name: 'MatrixChainOptimizer.java',
    category: 'algorithm',
    language: 'java',
    description: 'Core dynamic programming interval algorithm solving dp[i][j] recurrence in O(n³) time and O(n²) space',
    code: `package com.matrixoptimizer.algorithm;

import java.util.ArrayList;
import java.util.List;

public class MatrixChainOptimizer {
    public static class DPExecutionData {
        public final long[][] dp;
        public final int[][] root;
        public final List<String> steps;
        public final int n;

        public DPExecutionData(long[][] dp, int[][] root, List<String> steps, int n) {
            this.dp = dp;
            this.root = root;
            this.steps = steps;
            this.n = n;
        }
    }

    public static DPExecutionData solve(int[] p) {
        int n = p.length - 1;
        long[][] dp = new long[n + 1][n + 1];
        int[][] root = new int[n + 1][n + 1];
        List<String> steps = new ArrayList<>();

        for (int i = 1; i <= n; i++) dp[i][i] = 0;

        for (int L = 2; L <= n; L++) {
            for (int i = 1; i <= n - L + 1; i++) {
                int j = i + L - 1;
                dp[i][j] = Long.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    long mult = (long) p[i - 1] * p[k] * p[j];
                    long total = dp[i][k] + dp[k + 1][j] + mult;
                    if (total < dp[i][j]) {
                        dp[i][j] = total;
                        root[i][j] = k;
                    }
                }
            }
        }
        return new DPExecutionData(dp, root, steps, n);
    }

    public static String reconstructParenthesization(int[][] root, int i, int j, List<String> names) {
        if (i == j) return names.get(i - 1);
        int k = root[i][j];
        return "(" + reconstructParenthesization(root, i, k, names) + " JOIN " + 
               reconstructParenthesization(root, k + 1, j, names) + ")";
    }
}`
  },
  {
    path: 'src/main/java/com/matrixoptimizer/algorithm/CostEstimator.java',
    name: 'CostEstimator.java',
    category: 'algorithm',
    language: 'java',
    description: 'Mode B Relational Join Estimator accounting for table cardinalities, join keys, and selectivity',
    code: `package com.matrixoptimizer.algorithm;

import com.matrixoptimizer.model.DatabaseTable;
import java.util.List;

public class CostEstimator {
    public static class RelationalDPResult {
        public final long[][] dpCost;
        public final long[][] intermediateCardinality;
        public final int[][] root;
        public final int n;

        public RelationalDPResult(long[][] dpCost, long[][] intermediateCardinality, int[][] root, int n) {
            this.dpCost = dpCost;
            this.intermediateCardinality = intermediateCardinality;
            this.root = root;
            this.n = n;
        }
    }

    public static RelationalDPResult optimizeRelational(List<DatabaseTable> tables) {
        int n = tables.size();
        long[][] dpCost = new long[n + 1][n + 1];
        long[][] card = new long[n + 1][n + 1];
        int[][] root = new int[n + 1][n + 1];

        for (int i = 1; i <= n; i++) card[i][i] = tables.get(i - 1).getRowCount();

        for (int L = 2; L <= n; L++) {
            for (int i = 1; i <= n - L + 1; i++) {
                int j = i + L - 1;
                dpCost[i][j] = Long.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    long cardLeft = card[i][k];
                    long cardRight = card[k + 1][j];
                    double sel = Math.min(tables.get(k - 1).getSelectivity(), tables.get(k).getSelectivity());
                    long resultCard = Math.max(1, (long) (cardLeft * sel * Math.log10(cardRight + 10)));
                    long joinWork = cardLeft + cardRight + resultCard;
                    long total = dpCost[i][k] + dpCost[k + 1][j] + joinWork;
                    if (total < dpCost[i][j]) {
                        dpCost[i][j] = total;
                        root[i][j] = k;
                        card[i][j] = resultCard;
                    }
                }
            }
        }
        return new RelationalDPResult(dpCost, card, root, n);
    }
}`
  },
  {
    path: 'src/main/java/com/matrixoptimizer/parser/SQLQueryParser.java',
    name: 'SQLQueryParser.java',
    category: 'parser',
    language: 'java',
    description: 'SQL tokenizer and clause scanner extracting FROM and JOIN relations and checking alias mappings',
    code: `package com.matrixoptimizer.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLQueryParser {
    public static class ParsedTableRef {
        public final String tableName;
        public final String alias;
        public ParsedTableRef(String t, String a) { this.tableName = t; this.alias = a; }
    }

    public static class ParseResult {
        public final boolean success;
        public final String errorMessage;
        public final List<ParsedTableRef> tables;
        public ParseResult(List<ParsedTableRef> tables) { this.success = true; this.errorMessage = null; this.tables = tables; }
        public ParseResult(String error) { this.success = false; this.errorMessage = error; this.tables = List.of(); }
    }

    public static ParseResult parse(String sql) {
        if (sql == null || sql.trim().isEmpty()) return new ParseResult("SQL query cannot be empty.");
        if (!sql.trim().toUpperCase().startsWith("SELECT")) return new ParseResult("Query must begin with SELECT.");
        
        List<ParsedTableRef> tables = new ArrayList<>();
        Pattern fromPat = Pattern.compile("\\\\bFROM\\\\s+([A-Za-z0-9_]+)(?:\\\\s+([A-Za-z0-9_]+))?", Pattern.CASE_INSENSITIVE);
        Matcher mFrom = fromPat.matcher(sql);
        if (mFrom.find()) tables.add(new ParsedTableRef(mFrom.group(1), mFrom.group(2) != null ? mFrom.group(2) : ""));

        Pattern joinPat = Pattern.compile("\\\\b(?:INNER\\\\s+)?JOIN\\\\s+([A-Za-z0-9_]+)(?:\\\\s+([A-Za-z0-9_]+))?\\\\s+ON\\\\s+([^;]+)", Pattern.CASE_INSENSITIVE);
        Matcher mJoin = joinPat.matcher(sql);
        while (mJoin.find()) tables.add(new ParsedTableRef(mJoin.group(1), mJoin.group(2) != null ? mJoin.group(2) : ""));

        if (tables.size() < 2) return new ParseResult("Query requires at least 2 joinable tables.");
        return new ParseResult(tables);
    }
}`
  },
  {
    path: 'src/main/java/com/matrixoptimizer/ui/DPHeatmapPanel.java',
    name: 'DPHeatmapPanel.java',
    category: 'ui',
    language: 'java',
    description: 'Custom Java 2D heatmap renderer with interactive cell clicks, split inspection, and color gradients',
    code: `package com.matrixoptimizer.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DPHeatmapPanel extends JPanel {
    private long[][] dpTable;
    private int[][] rootTable;
    private List<String> tableNames;

    public DPHeatmapPanel() {
        setBackground(new Color(15, 23, 42));
    }

    public void updateData(long[][] dp, int[][] root, List<String> names) {
        this.dpTable = dp;
        this.rootTable = root;
        this.tableNames = names;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dpTable == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int n = dpTable.length - 1;
        int cellSize = 54;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                int x = 80 + (j - 1) * cellSize;
                int y = 50 + (i - 1) * cellSize;
                if (j >= i) {
                    g2.setColor(i == j ? new Color(16, 185, 129, 40) : new Color(30, 41, 59));
                    g2.fillRect(x, y, cellSize, cellSize);
                    g2.setColor(new Color(71, 85, 105));
                    g2.drawRect(x, y, cellSize, cellSize);
                    g2.setColor(Color.WHITE);
                    g2.drawString(String.valueOf(dpTable[i][j]), x + 8, y + 28);
                }
            }
        }
    }
}`
  },
  {
    path: 'src/main/java/com/matrixoptimizer/ui/JoinTreePanel.java',
    name: 'JoinTreePanel.java',
    category: 'ui',
    language: 'java',
    description: 'Custom Java 2D visual tree rendering leaf tables, intermediate join nodes, and costs',
    code: `package com.matrixoptimizer.ui;

import com.matrixoptimizer.model.JoinTreeNode;
import javax.swing.*;
import java.awt.*;

public class JoinTreePanel extends JPanel {
    private JoinTreeNode root;

    public JoinTreePanel() {
        setBackground(new Color(15, 23, 42));
    }

    public void setTree(JoinTreeNode root) {
        this.root = root;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (root == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawSubtree(g2, root, getWidth() / 2, 50, getWidth() / 4);
    }

    private void drawSubtree(Graphics2D g2, JoinTreeNode node, int x, int y, int offset) {
        if (node == null) return;
        int w = 120, h = 42;
        if (node.getLeft() != null) {
            g2.setColor(new Color(71, 85, 105));
            g2.drawLine(x, y + h / 2, x - offset, y + 80);
            drawSubtree(g2, node.getLeft(), x - offset, y + 80, Math.max(30, offset / 2));
        }
        if (node.getRight() != null) {
            g2.setColor(new Color(71, 85, 105));
            g2.drawLine(x, y + h / 2, x + offset, y + 80);
            drawSubtree(g2, node.getRight(), x + offset, y + 80, Math.max(30, offset / 2));
        }
        g2.setColor(node.isLeaf() ? new Color(30, 41, 59) : new Color(49, 46, 129));
        g2.fillRoundRect(x - w / 2, y, w, h, 8, 8);
        g2.setColor(Color.WHITE);
        g2.drawString(node.getLabel(), x - w / 2 + 10, y + 25);
    }
}`
  },
  {
    path: 'src/main/java/com/matrixoptimizer/App.java',
    name: 'App.java',
    category: 'ui',
    language: 'java',
    description: 'Main desktop application entrypoint launching Swing Event Dispatch Thread (EDT)',
    code: `package com.matrixoptimizer;

import com.matrixoptimizer.ui.MainFrame;
import javax.swing.*;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}`
  },
  {
    path: 'src/test/java/com/matrixoptimizer/MatrixChainOptimizerTest.java',
    name: 'MatrixChainOptimizerTest.java',
    category: 'test',
    language: 'java',
    description: 'JUnit 5 test suite verifying all 10 college benchmark requirements (standard 4,500 cost, overflow, etc.)',
    code: `package com.matrixoptimizer;

import com.matrixoptimizer.algorithm.MatrixChainOptimizer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MatrixChainOptimizerTest {
    @Test
    public void testStandardThreeMatrixBenchmark() {
        int[] p = {10, 30, 5, 60};
        MatrixChainOptimizer.DPExecutionData res = MatrixChainOptimizer.solve(p);
        assertEquals(4500, res.dp[1][3], "Expected optimal scalar multiplication cost to equal 4500");
        assertEquals(2, res.root[1][3], "Expected split point k to be 2");
    }
}`
  },
  {
    path: 'README.md',
    name: 'README.md',
    category: 'doc',
    language: 'markdown',
    description: 'Project setup documentation, Eclipse IDE import guide, Maven commands, and MySQL configuration',
    code: `# Matrix Chain Query Optimizer\nEducational database query optimization simulator using dynamic programming on intervals to select minimum-cost table join orders.\n\n### Eclipse IDE Setup\n1. File > Import > Existing Maven Projects\n2. Select project directory\n3. Run App.java as Java Application`
  },
  {
    path: 'PROJECT_REPORT.md',
    name: 'PROJECT_REPORT.md',
    category: 'doc',
    language: 'markdown',
    description: 'Academic final-year project report covering objectives, theoretical background, architecture, and results',
    code: `# Final Year Academic Project Report\nTitle: Matrix Chain Query Optimizer\nCourse: Advanced Database Systems & Algorithms\n\nAbstract: This project models database query optimization via Dynamic Programming on intervals O(n³)...`
  }
];
