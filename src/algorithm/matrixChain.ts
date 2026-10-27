import { DatabaseTable, JoinTreeNode, OptimizationResult } from '../types';

export interface DPExecutionData {
  dp: number[][];
  root: number[][];
  steps: string[];
  n: number;
}

export function solveMatrixChainDP(p: number[]): DPExecutionData {
  if (!p || p.length < 2) {
    throw new Error('Dimension array p must contain at least 2 entries.');
  }

  const n = p.length - 1;
  // Initialize 1-based tables of size (n+1) x (n+1)
  const dp: number[][] = Array.from({ length: n + 1 }, () => Array(n + 1).fill(0));
  const root: number[][] = Array.from({ length: n + 1 }, () => Array(n + 1).fill(0));
  const steps: string[] = [];

  // Base cases: dp[i][i] = 0
  for (let i = 1; i <= n; i++) {
    dp[i][i] = 0;
  }

  // L is chain length: from 2 to n
  for (let L = 2; L <= n; L++) {
    for (let i = 1; i <= n - L + 1; i++) {
      const j = i + L - 1;
      dp[i][j] = Number.MAX_SAFE_INTEGER;
      let bestSplit = -1;

      steps.push(`Evaluating interval [${i}..${j}] (Chain length L = ${L}):`);

      for (let k = i; k < j; k++) {
        const multCost = p[i - 1] * p[k] * p[j];
        const subCost = dp[i][k] + dp[k + 1][j];
        const total = subCost + multCost;

        steps.push(
          `   Split at k=${k}: dp[${i}][${k}] (${dp[i][k]}) + dp[${k + 1}][${j}] (${dp[k + 1][j]}) + (${p[i - 1]} × ${p[k]} × ${p[j]} = ${multCost}) => total = ${total}`
        );

        if (total < dp[i][j]) {
          dp[i][j] = total;
          bestSplit = k;
        }
      }
      root[i][j] = bestSplit;
      steps.push(`=> Optimal for [${i}..${j}]: min cost = ${dp[i][j]}, split at k = ${bestSplit}`);
    }
  }

  return { dp, root, steps, n };
}

export function reconstructParentheses(root: number[][], i: number, j: number, names: string[]): string {
  if (i === j) {
    return names[i - 1] || `A${i}`;
  }
  const k = root[i][j];
  if (k < i || k >= j) {
    return `(${names[i - 1]} JOIN ${names[j - 1]})`;
  }
  const left = reconstructParentheses(root, i, k, names);
  const right = reconstructParentheses(root, k + 1, j, names);
  return `(${left} JOIN ${right})`;
}

export function computeLeftToRightCost(p: number[]): number {
  const n = p.length - 1;
  if (n <= 1) return 0;
  let totalCost = 0;
  const currentRows = p[0];
  for (let i = 1; i < n; i++) {
    totalCost += currentRows * p[i] * p[i + 1];
  }
  return totalCost;
}

export function computeRightToLeftCost(p: number[]): number {
  const n = p.length - 1;
  if (n <= 1) return 0;
  let totalCost = 0;
  const currentCols = p[n];
  for (let i = n - 1; i >= 1; i--) {
    totalCost += p[i - 1] * p[i] * currentCols;
  }
  return totalCost;
}

export function buildJoinTree(
  root: number[][],
  dp: number[][],
  card: number[][] | null,
  i: number,
  j: number,
  tables: DatabaseTable[]
): JoinTreeNode {
  if (i === j) {
    const table = tables[i - 1];
    return {
      id: `leaf-${i}`,
      label: table.alias ? `${table.name} (${table.alias})` : table.name,
      isLeaf: true,
      table,
      estimatedCost: 0,
      intermediateCardinality: table.rowCount,
    };
  }

  let k = root[i][j];
  if (k < i || k >= j) k = i;

  const left = buildJoinTree(root, dp, card, i, k, tables);
  const right = buildJoinTree(root, dp, card, k + 1, j, tables);

  const cost = dp[i] && dp[i][j] !== undefined ? dp[i][j] : 0;
  const intermediateCardinality = card && card[i] && card[i][j] !== undefined ? card[i][j] : 0;

  return {
    id: `node-${i}-${j}`,
    label: `JOIN [${tables[i - 1]?.name}..${tables[j - 1]?.name}]`,
    isLeaf: false,
    left,
    right,
    estimatedCost: cost,
    intermediateCardinality,
    splitIndex: k,
  };
}

export function optimizeMatrixChain(tables: DatabaseTable[], customDims?: number[]): OptimizationResult {
  const startTime = performance.now();
  const n = tables.length;
  let p = customDims;

  if (!p || p.length !== n + 1) {
    p = new Array(n + 1);
    p[0] = Math.min(tables[0].rowCount, 100000);
    for (let i = 0; i < n; i++) {
      p[i + 1] = tables[i].attributeCount;
    }
  }

  const { dp, root, steps } = solveMatrixChainDP(p);
  const tableNames = tables.map((t) => (t.alias ? `${t.name} ${t.alias}` : t.name));
  const optimalExpression = reconstructParentheses(root, 1, n, tableNames);
  const optimalCost = dp[1][n];
  const leftToRightCost = computeLeftToRightCost(p);
  const rightToLeftCost = computeRightToLeftCost(p);

  const diff = leftToRightCost - optimalCost;
  const costSavingPct = leftToRightCost > 0 ? Math.max(0, (diff / leftToRightCost) * 100) : 0;

  const joinTreeRoot = buildJoinTree(root, dp, null, 1, n, tables);
  const executionTimeMs = +(performance.now() - startTime).toFixed(2);

  return {
    mode: 'MODE_A_MCM',
    modeDisplayName: 'Mode A: Matrix Chain Multiplication (Standard DP)',
    tables,
    dimensions: p,
    dpTable: dp,
    rootTable: root,
    optimalExpression,
    optimalCost,
    leftToRightCost,
    rightToLeftCost,
    costSavingPct,
    joinTreeRoot,
    stepLogs: steps,
    executionTimeMs,
  };
}
