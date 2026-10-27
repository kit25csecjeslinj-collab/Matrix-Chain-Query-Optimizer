import React from 'react';
import { Printer, CheckCircle2 } from 'lucide-react';

export const ReportViewer: React.FC = () => {
  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-semibold text-white tracking-tight">
            Academic Project Report & Documentation
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Comprehensive college software engineering report outline covering dynamic programming theory, architecture, and verification.
          </p>
        </div>

        <button
          onClick={handlePrint}
          className="inline-flex items-center gap-2 px-3 py-1.5 text-xs font-medium rounded-md bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors cursor-pointer"
        >
          <Printer className="w-3.5 h-3.5" />
          <span>Print / Save as PDF</span>
        </button>
      </div>

      <div className="p-8 rounded-xl border border-slate-800 bg-slate-900/60 space-y-8 text-slate-300 text-sm leading-relaxed">
        <div className="text-center border-b border-slate-800 pb-6 space-y-2">
          <span className="text-xs font-mono uppercase tracking-widest text-indigo-400">
            College Academic Project Report Outline
          </span>
          <h1 className="text-2xl font-bold text-white tracking-tight">
            Matrix Chain Query Optimizer: Dynamic Programming for Relational Join Order Selection
          </h1>
          <p className="text-xs text-slate-400">
            Course: CS 400 - Database Systems & Algorithm Design &bull; Java 17 & Swing UI &bull; MySQL 8 Persistence
          </p>
        </div>

        <section className="space-y-3">
          <h2 className="text-base font-bold text-white uppercase tracking-wider flex items-center gap-2">
            <span className="text-indigo-400">1.</span> Abstract
          </h2>
          <p>
            In relational database management systems (RDBMS) such as PostgreSQL and System-R, multi-table JOIN operations are among the most resource-intensive operations executed by the database engine. Because join operations are associative, the number of potential evaluation orders for n tables grows exponentially following the Catalan sequence C(n-1) = (2n-2)! / (n! * (n-1)!). For example, joining 10 tables produces 4,862 alternative evaluation trees; exhaustive evaluation quickly becomes prohibitive.
          </p>
          <p>
            This project designs and implements the <strong>Matrix Chain Query Optimizer</strong>, an educational system simulating how query optimizers adapt Dynamic Programming (DP) on intervals to determine the minimum-cost join evaluation plan in polynomial time O(n³) with O(n²) space. The application provides two operating modes: <em>Mode A (Classic Matrix Chain Multiplication)</em> and <em>Mode B (Relational Join Estimator)</em>, an interactive Swing-based GUI with color-scaled heatmaps and binary join tree visualizers, a robust SQL tokenizer, and optional MySQL 8 persistence with seamless in-memory fallback.
          </p>
        </section>

        <section className="space-y-3">
          <h2 className="text-base font-bold text-white uppercase tracking-wider flex items-center gap-2">
            <span className="text-indigo-400">2.</span> Objectives
          </h2>
          <ul className="list-disc list-inside space-y-1.5 pl-2 text-xs text-slate-300">
            <li>Model multi-table query join optimization using the Matrix Chain dynamic programming recurrence.</li>
            <li>Implement the core O(n³) dynamic programming engine in Java 17 using standard 2D arrays (<code>dp[i][j]</code> and <code>root[i][j]</code>).</li>
            <li>Reconstruct optimal parenthesized join trees recursively from the split matrix.</li>
            <li>Develop an interactive 2D graphical heatmap for visualizing subproblem solutions and optimal split points k.</li>
            <li>Provide comparative workload evaluation showing percentage savings against linear baselines (Left-to-Right and Right-to-Left).</li>
            <li>Implement JDBC persistence for MySQL 8 alongside an automatic in-memory demonstration engine.</li>
          </ul>
        </section>

        <section className="space-y-3">
          <h2 className="text-base font-bold text-white uppercase tracking-wider flex items-center gap-2">
            <span className="text-indigo-400">3.</span> Recurrence Relation & Complexity
          </h2>
          <div className="p-4 rounded-lg bg-slate-950 border border-slate-800 font-mono text-xs text-emerald-300 space-y-2">
            <div>dp[i][i] = 0  (Base Case: single table requires 0 join operations)</div>
            <div>
              dp[i][j] = min over k in [i..j-1] of ( dp[i][k] + dp[k+1][j] + cost(i, k, j) )
            </div>
            <div className="text-slate-400 text-[11px] pt-1 border-t border-slate-800">
              For Mode A: cost(i, k, j) = p[i-1] * p[k] * p[j]
              <br />
              For Mode B: cost(i, k, j) = card[i][k] + card[k+1][j] + output_cardinality
            </div>
          </div>
          <p className="text-xs text-slate-400">
            <strong>Time Complexity:</strong> O(n³) — Total intervals evaluated: n(n-1)/2. At each interval of length L, L-1 split positions are inspected.
            <br />
            <strong>Space Complexity:</strong> O(n²) — Two (n+1) × (n+1) 2D arrays: <code>dp[i][j]</code> (64-bit <code>long</code>) and <code>root[i][j]</code> (32-bit <code>int</code>).
          </p>
        </section>

        <section className="space-y-3">
          <h2 className="text-base font-bold text-white uppercase tracking-wider flex items-center gap-2">
            <span className="text-indigo-400">4.</span> Testing & Benchmark Verification
          </h2>
          <p className="text-xs">
            The project was tested against the standard college benchmark:
            <br />
            <strong>Dimensions:</strong> A1(10 × 30), A2(30 × 5), A3(5 × 60) implies p = [10, 30, 5, 60].
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs font-mono">
            <div className="p-3 rounded bg-slate-950 border border-slate-800">
              <span className="text-slate-400 block">Plan 1: (A1 JOIN A2) JOIN A3</span>
              <span className="text-emerald-400 font-bold">= 1,500 + 3,000 = 4,500 ops (OPTIMAL)</span>
            </div>
            <div className="p-3 rounded bg-slate-950 border border-slate-800">
              <span className="text-slate-400 block">Plan 2: A1 JOIN (A2 JOIN A3)</span>
              <span className="text-rose-400 font-bold">= 9,000 + 18,000 = 27,000 ops (+500% cost)</span>
            </div>
          </div>
          <p className="text-xs text-emerald-400 font-medium flex items-center gap-1.5">
            <CheckCircle2 className="w-4 h-4" />
            <span>Optimal cost 4,500 verified with split point k=2 and 83.33% cost reduction over baseline.</span>
          </p>
        </section>

        <section className="space-y-3 border-t border-slate-800 pt-6">
          <h2 className="text-base font-bold text-white uppercase tracking-wider flex items-center gap-2">
            <span className="text-indigo-400">5.</span> Academic Conclusion
          </h2>
          <p className="text-xs text-slate-300 leading-relaxed">
            The Matrix Chain Query Optimizer bridges dynamic programming algorithm theory with practical database query planning. It demonstrates why relational database optimizers rely on dynamic programming on intervals and subgraphs to avoid factorial search spaces while consistently discovering minimum-cost join execution trees.
          </p>
        </section>
      </div>
    </div>
  );
};
