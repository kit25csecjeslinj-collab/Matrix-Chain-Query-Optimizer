# ACADEMIC PROJECT REPORT OUTLINE
# Title: Matrix Chain Query Optimizer
### Course: CS 400 - Advanced Database Systems & Algorithms

---

## 1. Abstract
In modern Relational Database Management Systems (RDBMS), multi-table `JOIN` queries are ubiquitous yet computationally intensive. Finding the optimal join execution sequence is crucial because intermediate result sizes and I/O workloads differ drastically depending on associativity parenthesization. For $n$ tables, the search space consists of $C_{n-1} = \frac{1}{n}\binom{2n-2}{n-1}$ parenthesizations (the Catalan number), which exhibits exponential growth $O(4^n/n^{3/2})$. 

This project designs and implements the **Matrix Chain Query Optimizer**, a software tool demonstrating how Dynamic Programming (DP) on intervals calculates the optimal join sequence in polynomial time $O(n^3)$ with $O(n^2)$ space complexity. The system features an interactive Java Swing graphical user interface, a lightweight SQL parser, dynamic programming tables (`dp[i][j]` cost and `root[i][j]` split point), interactive heatmaps, hierarchical binary join trees, cost comparison metrics against standard Left-to-Right and Right-to-Left baselines, and optional MySQL 8 JDBC persistence.

---

## 2. Objectives
1. **Algorithmic Simulation**: Implement the Matrix Chain Multiplication recurrence via Dynamic Programming with interval length iterations ($L = 2 \dots n$).
2. **Relational Cost Modeling**: Formulate an educational database cost model accounting for table cardinalities, column projection dimensions, and predicate selectivity factors.
3. **Interactive Visualization**: Render custom 2D heatmaps showing intermediate subproblem costs and optimal split points $k$, accompanied by hierarchical binary join trees.
4. **SQL Parsing & Validation**: Parse SQL `SELECT ... FROM ... JOIN ... ON` statements, validate table existence against the catalog, and detect non-positive cardinalities or malformed syntax.
5. **Empirical Benchmarking**: Quantify cost differences and percentage improvements between optimal DP plans and default linear plans.
6. **Persistence**: Support MySQL 8 storage via JDBC while guaranteeing graceful degradation to an in-memory repository.

---

## 3. Existing System vs. Proposed System

| Dimension | Existing / Naive Systems | Proposed Matrix Chain Query Optimizer |
| :--- | :--- | :--- |
| **Join Order Selection** | Static Left-to-Right evaluation or exhaustive brute-force search. | Dynamic programming on intervals with optimal sub-structure. |
| **Search Complexity** | $O(n!)$ or $O(4^n/n^{3/2})$ exponential explosion. | $O(n^3)$ polynomial time, $O(n^2)$ space complexity. |
| **Educational Transparency** | Black-box query plan output with internal C/C++ engine obscurity. | Interactive DP heatmap, step-by-step trace of every split $k$, and visual join tree. |
| **Cost Comparison** | Single plan output without clear percentage improvement metrics. | Side-by-side comparison of Optimal, Left-to-Right, and Right-to-Left strategies. |
| **Persistence Flexibility** | Requires mandatory database server startup. | Dual-mode: In-Memory demo engine or live MySQL 8 JDBC connectivity. |

---

## 4. Theoretical Background & Mathematical Formulation

### 4.1 Optimal Substructure
Let $A_{i..j} = A_i \Join A_{i+1} \dots \Join A_j$. Any parenthesization splits the chain into two subproblems $(A_{i..k})$ and $(A_{k+1..j})$ for some $k \in [i, j-1]$. The optimal parenthesization of $A_{i..j}$ must contain the optimal parenthesizations of both subproblems.

### 4.2 Recurrence Relation
$$\text{dp}[i][i] = 0 \quad (\forall 1 \le i \le n)$$
$$\text{dp}[i][j] = \min_{i \le k < j} \left\{ \text{dp}[i][k] + \text{dp}[k+1][j] + p_{i-1} \cdot p_k \cdot p_j \right\} \quad (\forall 1 \le i < j \le n)$$
$$\text{root}[i][j] = \arg\min_{i \le k < j} \left\{ \text{dp}[i][k] + \text{dp}[k+1][j] + p_{i-1} \cdot p_k \cdot p_j \right\}$$

### 4.3 Complexity Analysis
- **Subproblems**: $\binom{n}{2} + n = \frac{n(n+1)}{2} = \Theta(n^2)$ entries in the upper triangle.
- **Split Evaluations**: An interval of length $L$ evaluates $L - 1$ split positions. Total operations: $\sum_{L=2}^n (n - L + 1)(L - 1) = \frac{n^3 - n}{6} = \Theta(n^3)$.
- **Space**: Two $n \times n$ matrices: $\text{dp}[n+1][n+1]$ (64-bit `long`) and $\text{root}[n+1][n+1]$ (32-bit `int`) $\implies \Theta(n^2)$.

---

## 5. Software Architecture & Design
The project adheres to a clean Model-View-Controller (MVC) architectural pattern:
- **Model (`com.matrixoptimizer.model`)**: Data encapsulation for tables, dimensions, trees, and results.
- **Algorithm Engine (`com.matrixoptimizer.algorithm`)**: Pure algorithmic logic implementing DP recurrence, relational cost estimation, and tree generation.
- **Parser (`com.matrixoptimizer.parser`)**: SQL query scanner and validator.
- **Data Access (`com.matrixoptimizer.database`)**: Repository pattern with JDBC prepared statements and concurrent in-memory store.
- **UI (`com.matrixoptimizer.ui`)**: Swing GUI panels featuring custom Java 2D graphics rendering.

---

## 6. Implementation Highlights
1. **64-bit Arithmetic & Overflow Detection**: Prevents integer overflow when computing intermediate multiplication bounds by employing `Math.multiplyExact`.
2. **Recursive Parenthesization Reconstruction**: Walks `root[i][j]` recursively to generate properly balanced parenthesized expressions:
   ```java
   String left = reconstruct(root, i, k, names);
   String right = reconstruct(root, k + 1, j, names);
   return "(" + left + " JOIN " + right + ")";
   ```
3. **Interactive DP Heatmap**: Normalizes cost values into dynamic color ranges and allows interactive click inspection of any subproblem cell $(i, j)$ and split $k$.
4. **Relational Join Cost Model**: Provides Mode B alongside Mode A to bridge matrix mathematics with realistic database join behaviors (intermediate cardinalities and selectivity).

---

## 7. Verification & Test Results
All 10 college benchmark test cases passed with zero errors:
1. **Single Matrix**: Verified base case $\text{dp}[1][1] = 0$.
2. **Two Matrices**: Verified deterministic cost $p_0 \cdot p_1 \cdot p_2$.
3. **Standard 3-Matrix Benchmark**:
   - Dimensions: $10 \times 30$, $30 \times 5$, $5 \times 60 \implies p = [10, 30, 5, 60]$.
   - Result: Optimal split at $k=2$, producing $((A_1 \Join A_2) \Join A_3)$ with cost **4,500**.
   - Verified that Right-to-Left cost is 27,000, yielding **83.33% cost reduction**.
4. **5+ Matrices**: Validated on realistic 5-table e-commerce join (`Customers`, `Orders`, `OrderItems`, `Products`, `Payments`).
5. **Negative/Zero Dimensions**: Successfully intercepted with `IllegalArgumentException`.
6. **SQL Parsing**: Verified tokenization and alias resolution for `FROM` and `JOIN ... ON` clauses.

---

## 8. Limitations & Assumptions
1. **Join Key Equivalence**: The educational matrix model assumes that adjacent tables in the query share a compatible joining attribute (chain topology). Arbitrary hyper-graph joins are approximated as sequential chains.
2. **Cost Metric**: Matrix chain cost models scalar arithmetic operations, while real database optimizers also measure disk page I/O, cache misses, and sorting overhead.

---

## 9. Future Enhancements
1. **Bushy Tree vs. Left-Deep Tree Constraints**: Add a user toggle to enforce left-deep join trees (as used in pipelined execution).
2. **System-R Style Dynamic Programming**: Extend the interval DP to subsets of relations (System-R power set DP) for non-linear query graphs.
3. **Live EXPLAIN Hook**: Support executing `EXPLAIN FORMAT=JSON` directly against MySQL 8 to compare the simulator's predicted order with MySQL's native optimizer.
