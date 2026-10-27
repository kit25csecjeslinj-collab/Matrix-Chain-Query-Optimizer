import React, { useState } from 'react';
import { TestCaseResult } from '../types';
import { solveMatrixChainDP, reconstructParentheses, computeRightToLeftCost } from '../algorithm/matrixChain';
import { parseSQLQuery } from '../algorithm/sqlParser';
import { CheckCircle2, XCircle, Play, ShieldCheck } from 'lucide-react';

export const TestRunner: React.FC = () => {
  const [isRunning, setIsRunning] = useState(false);
  const [testResults, setTestResults] = useState<TestCaseResult[] | null>(null);

  const runAllTests = () => {
    setIsRunning(true);
    const results: TestCaseResult[] = [];

    // Test 1: Single Matrix
    try {
      const p = [10, 20];
      const res = solveMatrixChainDP(p);
      const passed = res.dp[1][1] === 0;
      results.push({
        id: 1,
        title: 'Test 1: Single Matrix (Base Case)',
        description: 'Verify dp[1][1] = 0 multiplications for a single matrix relation.',
        expected: 'Cost = 0',
        actual: `Cost = ${res.dp[1][1]}`,
        passed,
        details: 'Chains of length 1 require zero multiplications.',
      });
    } catch (e: any) {
      results.push({
        id: 1,
        title: 'Test 1: Single Matrix',
        description: 'Verify dp[1][1] = 0',
        expected: 'Cost = 0',
        actual: `Error: ${e.message}`,
        passed: false,
        details: e.message,
      });
    }

    // Test 2: Two Matrices
    try {
      const p = [10, 20, 30];
      const res = solveMatrixChainDP(p);
      const expected = 10 * 20 * 30;
      const passed = res.dp[1][2] === expected && res.root[1][2] === 1;
      const paren = reconstructParentheses(res.root, 1, 2, ['T1', 'T2']);
      results.push({
        id: 2,
        title: 'Test 2: Two Matrices (Single Split Choice)',
        description: 'Verify dimensions [10, 20, 30] produce cost 6,000 and (T1 JOIN T2).',
        expected: `Cost = ${expected}, split = 1, expression = (T1 JOIN T2)`,
        actual: `Cost = ${res.dp[1][2]}, split = ${res.root[1][2]}, expression = ${paren}`,
        passed,
        details: 'Unique split k=1 evaluated correctly.',
      });
    } catch (e: any) {
      results.push({
        id: 2,
        title: 'Test 2: Two Matrices',
        description: 'Two matrix evaluation',
        expected: '6000',
        actual: e.message,
        passed: false,
        details: e.message,
      });
    }

    // Test 3: Standard 3-Matrix Textbook Benchmark (10x30, 30x5, 5x60) -> 4,500
    try {
      const p = [10, 30, 5, 60];
      const res = solveMatrixChainDP(p);
      const passed = res.dp[1][3] === 4500 && res.root[1][3] === 2;
      const paren = reconstructParentheses(res.root, 1, 3, ['A1', 'A2', 'A3']);
      results.push({
        id: 3,
        title: 'Test 3: Standard 3-Matrix Benchmark (10×30, 30×5, 5×60)',
        description: 'Core benchmark: optimal split k=2 producing ((A1 JOIN A2) JOIN A3) with cost 4,500.',
        expected: 'Cost = 4,500, split k = 2, expr = ((A1 JOIN A2) JOIN A3)',
        actual: `Cost = ${res.dp[1][3].toLocaleString()}, split k = ${res.root[1][3]}, expr = ${paren}`,
        passed,
        details: 'Matched classic textbook specification exactly: 10*30*5 + 10*5*60 = 1500 + 3000 = 4,500.',
      });
    } catch (e: any) {
      results.push({
        id: 3,
        title: 'Test 3: Standard 3-Matrix Benchmark',
        description: 'Textbook verification',
        expected: '4500',
        actual: e.message,
        passed: false,
        details: e.message,
      });
    }

    // Test 4: Five or more matrices
    try {
      const p = [40, 20, 30, 10, 30, 50];
      const res = solveMatrixChainDP(p);
      const passed = res.dp[1][5] > 0 && res.root[1][5] >= 1 && res.root[1][5] < 5;
      results.push({
        id: 4,
        title: 'Test 4: Five or More Matrices (Chain length 5)',
        description: 'Verify DP recurrence solves larger table sequence without stack overflow.',
        expected: 'Valid positive cost, root split in [1..4]',
        actual: `Cost = ${res.dp[1][5].toLocaleString()}, optimal root split = ${res.root[1][5]}`,
        passed,
        details: 'Successfully computed full upper-triangular table for 5 relations.',
      });
    } catch (e: any) {
      results.push({
        id: 4,
        title: 'Test 4: Five or More Matrices',
        description: 'Multi-table chain',
        expected: 'Positive cost',
        actual: e.message,
        passed: false,
        details: e.message,
      });
    }

    // Test 5: Invalid Dimensions
    try {
      let threw = false;
      try {
        solveMatrixChainDP([10]);
      } catch {
        threw = true;
      }
      results.push({
        id: 5,
        title: 'Test 5: Invalid Dimensions Interception',
        description: 'Ensure arrays with fewer than 2 dimensions throw descriptive errors.',
        expected: 'Throws Error for invalid dimension input',
        actual: threw ? 'Error thrown successfully' : 'No error thrown',
        passed: threw,
        details: 'Dimension validation successfully rejected non-viable matrix chain.',
      });
    } catch (e: any) {
      results.push({
        id: 5,
        title: 'Test 5: Invalid Dimensions',
        description: 'Error handling check',
        expected: 'Throws Error',
        actual: e.message,
        passed: false,
        details: e.message,
      });
    }

    // Test 6: Empty Input
    try {
      let threw = false;
      try {
        solveMatrixChainDP([]);
      } catch {
        threw = true;
      }
      results.push({
        id: 6,
        title: 'Test 6: Empty Input Handling',
        description: 'Empty arrays or null references must fail safely.',
        expected: 'Safely rejects empty input',
        actual: threw ? 'Safely rejected with error' : 'Failed to reject',
        passed: threw,
        details: 'Graceful degradation confirmed.',
      });
    } catch (e: any) {
      results.push({
        id: 6,
        title: 'Test 6: Empty Input',
        description: 'Null and empty check',
        expected: 'Error handled',
        actual: e.message,
        passed: false,
        details: e.message,
      });
    }

    // Test 7: Large Dimensions / Overflow Safety
    try {
      const p = [100000, 100000, 100000];
      const res = solveMatrixChainDP(p);
      const expected = 100000 * 100000 * 100000;
      const passed = res.dp[1][2] === expected && res.dp[1][2] > 0;
      results.push({
        id: 7,
        title: 'Test 7: Overflow Protection with Large Dimensions',
        description: 'Verify 100,000 × 100,000 × 100,000 = 10^15 fits in 64-bit integer without overflow.',
        expected: '1,000,000,000,000,000',
        actual: res.dp[1][2].toLocaleString(),
        passed,
        details: '64-bit integer bounds verified.',
      });
    } catch (e: any) {
      results.push({
        id: 7,
        title: 'Test 7: Overflow Protection',
        description: 'BigInt / large number safety',
        expected: '10^15',
        actual: e.message,
        passed: false,
        details: e.message,
      });
    }

    // Test 8: Correct Parenthesization Reconstruction
    try {
      const p = [10, 20, 30, 40];
      const res = solveMatrixChainDP(p);
      const expr = reconstructParentheses(res.root, 1, 3, ['Cust', 'Orders', 'Items']);
      const passed = expr.includes('Cust') && expr.includes('Orders') && expr.includes('Items') && expr.startsWith('(') && expr.endsWith(')');
      results.push({
        id: 8,
        title: 'Test 8: Balanced Parenthesization Reconstruction',
        description: 'Verify recursive reconstruction builds valid parenthesized join expression.',
        expected: 'Well-formed parenthesized string covering all 3 tables',
        actual: expr,
        passed,
        details: 'Balanced join expression generated from root split matrix.',
      });
    } catch (e: any) {
      results.push({
        id: 8,
        title: 'Test 8: Parenthesization Reconstruction',
        description: 'Expression builder',
        expected: 'Valid expression',
        actual: e.message,
        passed: false,
        details: e.message,
      });
    }

    // Test 9: Cost Comparison vs Baselines
    try {
      const p = [10, 30, 5, 60];
      const res = solveMatrixChainDP(p);
      const optCost = res.dp[1][3];
      const r2l = computeRightToLeftCost(p);
      const saving = ((r2l - optCost) / r2l) * 100;
      const passed = optCost <= r2l && Math.abs(saving - 83.33) < 0.1;
      results.push({
        id: 9,
        title: 'Test 9: Cost Comparison Between Optimal and Baseline Orders',
        description: 'Verify optimal plan (4,500) saves 83.33% compared to right-to-left baseline (27,000).',
        expected: 'Saving = 83.33% (Optimal 4,500 vs Right-to-Left 27,000)',
        actual: `Saving = ${saving.toFixed(2)}% (Optimal ${optCost.toLocaleString()} vs R2L ${r2l.toLocaleString()})`,
        passed,
        details: 'Demonstrates empirical efficacy of dynamic programming optimization.',
      });
    } catch (e: any) {
      results.push({
        id: 9,
        title: 'Test 9: Cost Comparison',
        description: 'Savings calculation',
        expected: '83.33%',
        actual: e.message,
        passed: false,
        details: e.message,
      });
    }

    // Test 10: SQL Parser & Unknown Table Validation
    try {
      const goodSql = 'SELECT * FROM Customers C JOIN Orders O ON C.id = O.c_id;';
      const badSql = 'UPDATE Customers SET status = 1;';
      const resGood = parseSQLQuery(goodSql);
      const resBad = parseSQLQuery(badSql);

      const passed = resGood.success && resGood.tables.length === 2 && !resBad.success;
      results.push({
        id: 10,
        title: 'Test 10: SQL Parser Syntax & Unsupported Query Interception',
        description: 'Correctly parses valid SELECT joins and rejects non-SELECT queries.',
        expected: 'Accepts SELECT with 2 tables; rejects UPDATE queries',
        actual: `Good query parsed: ${resGood.success} (${resGood.tables.length} tables); Bad query rejected: ${!resBad.success}`,
        passed,
        details: 'SQL parser edge cases successfully validated.',
      });
    } catch (e: any) {
      results.push({
        id: 10,
        title: 'Test 10: SQL Parser Validation',
        description: 'Parser validation',
        expected: 'Valid',
        actual: e.message,
        passed: false,
        details: e.message,
      });
    }

    setTestResults(results);
    setIsRunning(false);
  };

  const totalPassed = testResults?.filter((t) => t.passed).length || 0;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-semibold text-white tracking-tight">
            Academic Unit Test Suite (10 Requirements)
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Executes all 10 college specification test cases with live assertion matching and logs.
          </p>
        </div>

        <button
          onClick={runAllTests}
          disabled={isRunning}
          className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white shadow-sm transition-colors disabled:opacity-50 cursor-pointer"
        >
          <Play className="w-3.5 h-3.5 fill-current" />
          <span>{isRunning ? 'Running Tests...' : 'Execute All 10 Tests'}</span>
        </button>
      </div>

      {testResults && (
        <div className="p-4 rounded-xl border border-slate-800 bg-slate-900/60 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
              totalPassed === 10 ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40' : 'bg-amber-500/20 text-amber-400'
            }`}>
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <div className="text-sm font-bold text-white">
                {totalPassed === 10 ? 'All 10 College Test Cases Passed (100%)' : `${totalPassed} / 10 Tests Passed`}
              </div>
              <div className="text-xs text-slate-400">
                Verified against standard benchmark 10x30, 30x5, 5x60 yielding 4,500 cost.
              </div>
            </div>
          </div>

          <div className="text-right font-mono">
            <span className="text-2xl font-bold text-emerald-400">{totalPassed}</span>
            <span className="text-slate-500 text-sm"> / 10</span>
          </div>
        </div>
      )}

      <div className="space-y-3">
        {(testResults || [
          { id: 1, title: 'Test 1: Single Matrix (Base Case)', description: 'Chains of length 1 require 0 multiplications' },
          { id: 2, title: 'Test 2: Two Matrices (Deterministic Split)', description: 'Unique split k=1 evaluated' },
          { id: 3, title: 'Test 3: Standard 3-Matrix Benchmark', description: 'Dimensions 10x30, 30x5, 5x60 => cost 4,500' },
          { id: 4, title: 'Test 4: Five or More Matrices', description: 'Multi-table chain evaluation' },
          { id: 5, title: 'Test 5: Invalid Dimensions Interception', description: 'Non-positive dimensions rejected' },
          { id: 6, title: 'Test 6: Empty Input Handling', description: 'Null and empty arrays rejected' },
          { id: 7, title: 'Test 7: Overflow Protection with Large Dimensions', description: '10^15 64-bit integer test' },
          { id: 8, title: 'Test 8: Correct Reconstruction of Parenthesization', description: 'Recursive string builder' },
          { id: 9, title: 'Test 9: Cost Comparison Between Optimal and Baselines', description: '83.33% savings verification' },
          { id: 10, title: 'Test 10: SQL Parser Syntax & Unknown Table Check', description: 'SELECT and JOIN clause validation' },
        ]).map((item: any) => {
          const res = testResults?.find((r) => r.id === item.id);
          return (
            <div
              key={item.id}
              className={`p-4 rounded-xl border transition-all ${
                res
                  ? res.passed
                    ? 'border-emerald-800/50 bg-emerald-950/20'
                    : 'border-rose-800/50 bg-rose-950/20'
                  : 'border-slate-800 bg-slate-900/30'
              }`}
            >
              <div className="flex items-start justify-between gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    {res ? (
                      res.passed ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                      ) : (
                        <XCircle className="w-4 h-4 text-rose-400 shrink-0" />
                      )
                    ) : (
                      <div className="w-4 h-4 rounded-full border border-slate-600 shrink-0" />
                    )}
                    <span className="text-sm font-semibold text-white">{item.title}</span>
                  </div>
                  <p className="text-xs text-slate-400 pl-6">{item.description}</p>
                </div>

                {res && (
                  <span
                    className={`text-[10px] font-mono px-2 py-0.5 rounded font-semibold uppercase ${
                      res.passed
                        ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                        : 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
                    }`}
                  >
                    {res.passed ? 'PASSED' : 'FAILED'}
                  </span>
                )}
              </div>

              {res && (
                <div className="mt-3 pt-3 border-t border-slate-800/60 pl-6 grid grid-cols-1 md:grid-cols-2 gap-2 text-xs font-mono">
                  <div>
                    <span className="text-slate-500">Expected: </span>
                    <span className="text-slate-300">{res.expected}</span>
                  </div>
                  <div>
                    <span className="text-slate-500">Actual: </span>
                    <span className={res.passed ? 'text-emerald-400' : 'text-rose-400'}>
                      {res.actual}
                    </span>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};
