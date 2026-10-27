# Matrix Chain Query Optimizer

An educational database query optimization simulator using dynamic programming on intervals to select minimum-cost table join orders, with interactive DP heatmaps, join tree visualizations, and an Eclipse-ready Java backend.

---

## 1. Project Overview & Academic Goals

In relational database management systems (RDBMS) such as PostgreSQL, MySQL, and IBM DB2 (System-R), evaluating multi-table joins is computationally expensive. Because joins are associative:
$$(R_1 \Join R_2) \Join R_3 \equiv R_1 \Join (R_2 \Join R_3)$$
The number of valid evaluation trees for $n$ tables is given by the Catalan number $C_{n-1} = \frac{1}{n} \binom{2n-2}{n-1}$, which grows exponentially ($O(4^n / n^{3/2})$). 

This project simulates how query optimizers use **Dynamic Programming (DP)** on intervals to select the minimum-cost join tree in polynomial time $O(n^3)$ with $O(n^2)$ space.

### Two Documented Execution Modes:
- **Mode A: Matrix Chain Demonstration (MCM)**:
  Applies the classic dynamic programming recurrence on compatible dimensions $p_0 \times p_1, p_1 \times p_2, \dots$:
  $$\text{dp}[i][j] = \min_{i \le k < j} \left\{ \text{dp}[i][k] + \text{dp}[k+1][j] + p_{i-1} \cdot p_k \cdot p_j \right\}$$
- **Mode B: Relational Join Estimator (RDBMS Cost Model)**:
  Models realistic database table statistics:
  - Base cardinalities $|T|$ (tuple counts).
  - Selectivity estimates $s \in (0.0, 1.0]$.
  - Intermediate output size: $|R_1 \Join R_2| = |R_1| \cdot s \cdot \log_{10}(|R_2| + 10)$.
  - Join execution cost (Hash / Sort-Merge Join scan & materialization).

---

## 2. Project File Structure

```
matrix-chain-query-optimizer/
├── pom.xml                                  # Maven dependencies (Java 17, FlatLaf, MySQL Connector, JUnit 5)
├── schema.sql                               # MySQL 8 database schema & sample seed data
├── README.md                                # Setup, compilation, and Eclipse instructions
├── PROJECT_REPORT.md                        # Academic final-year project report
└── src/
    ├── main/java/com/matrixoptimizer/
    │   ├── App.java                         # Main entrypoint with FlatLaf UI
    │   ├── model/
    │   │   ├── DatabaseTable.java           # Table metadata, rows, attributes, selectivity
    │   │   ├── MatrixDimensions.java        # Chain dimension array p
    │   │   ├── JoinTreeNode.java            # Binary join tree representation
    │   │   └── OptimizationResult.java      # DP results, split tables, cost savings
    │   ├── algorithm/
    │   │   ├── MatrixChainOptimizer.java    # DP recurrence, O(n³) execution, parenthesization
    │   │   ├── CostEstimator.java           # Relational cardinality & selectivity cost model
    │   │   ├── JoinTreeBuilder.java         # Recursive tree construction from DP root
    │   │   └── JoinOrderOptimizer.java      # Facade coordinating Mode A & Mode B
    │   ├── parser/
    │   │   └── SQLQueryParser.java          # Regex/AST parser extracting SELECT, FROM, JOIN, ON
    │   ├── database/
    │   │   ├── DatabaseConnection.java      # JDBC manager with in-memory fallback
    │   │   ├── TableRepository.java         # Table CRUD operations
    │   │   ├── QueryRepository.java         # Saved benchmark queries
    │   │   └── OptimizationRepository.java   # Audit log and run history
    │   ├── ui/
    │   │   ├── MainFrame.java               # Main desktop window & sidebar navigation
    │   │   ├── DashboardPanel.java          # Executive metrics and history table
    │   │   ├── TableConfigurationPanel.java # Table editor and catalog manager
    │   │   ├── QueryEditorPanel.java        # SQL editor, validator, mode selector
    │   │   ├── OptimizationResultPanel.java # Tabbed results, audit logs, export buttons
    │   │   ├── DPHeatmapPanel.java          # Interactive Java 2D heatmap with cell inspection
    │   │   ├── JoinTreePanel.java           # Custom Java 2D binary join tree visualizer
    │   │   ├── CostComparisonPanel.java     # Comparative bar chart and baseline metrics
    │   │   └── SettingsPanel.java           # MySQL JDBC credentials & connection test
    │   └── util/
    │       ├── InputValidator.java          # Validation for dimensions, numbers, tables
    │       └── CSVExporter.java             # Export results to CSV and text audit reports
    └── test/java/com/matrixoptimizer/
        └── MatrixChainOptimizerTest.java    # 10 comprehensive JUnit 5 unit tests
```

---

## 3. How to Run in Eclipse IDE

### Option A: Import as an Existing Maven Project (Recommended)
1. Open **Eclipse IDE for Java Developers** (2022-09 or newer, with JDK 17+ installed).
2. Click **File > Import...**
3. Select **Maven > Existing Maven Projects** and click **Next**.
4. Click **Browse...** and choose the extracted `java-project` directory containing `pom.xml`.
5. Ensure `pom.xml` is checked and click **Finish**. Eclipse will resolve dependencies (FlatLaf, MySQL Connector, JUnit 5).
6. To run the application:
   - In the **Package Explorer**, navigate to `src/main/java/com/matrixoptimizer/App.java`.
   - Right-click `App.java` > **Run As > Java Application**.
7. To run the unit tests:
   - Navigate to `src/test/java/com/matrixoptimizer/MatrixChainOptimizerTest.java`.
   - Right-click > **Run As > JUnit Test**.

### Option B: Import as a Standard Java Project
1. In Eclipse, select **File > New > Java Project**.
2. Uncheck *Use default location* and browse to the project folder.
3. Select Execution environment **JavaSE-17**.
4. In the Project Build Path, add `mysql-connector-j-8.3.0.jar` and `flatlaf-3.4.1.jar` from Maven or lib directory.
5. Right-click `App.java` > **Run As > Java Application**.

---

## 4. How to Run via Maven Command Line

```bash
# Compile and package into an executable JAR
mvn clean package

# Run all 10 unit test cases
mvn test

# Launch the desktop Swing application
java -jar target/matrix-chain-query-optimizer-1.0.0-jar-with-dependencies.jar
```

---

## 5. MySQL 8 Database Setup (Optional)

The application includes an automatic **In-Memory Demonstration Engine** that runs out of the box with zero external configuration. If you wish to connect to a real MySQL 8 server:

1. Open your MySQL client (MySQL Workbench, Terminal, or phpMyAdmin).
2. Execute the included `schema.sql` script:
   ```bash
   mysql -u root -p < schema.sql
   ```
3. In the application UI, go to **Database & Settings**.
4. Enter your host, port, username, and password, then click **Test MySQL Connection**.
5. Once connected, changes and optimization runs persist directly into MySQL tables.
