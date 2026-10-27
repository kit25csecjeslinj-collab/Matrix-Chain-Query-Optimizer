export interface DatabaseTable {
  id: number;
  name: string;
  alias?: string;
  rowCount: number;
  attributeCount: number;
  selectivity: number;
  primaryJoinKey: string;
  description: string;
}

export type OptimizationMode = 'MODE_A_MCM' | 'MODE_B_RELATIONAL';

export interface JoinTreeNode {
  id: string;
  label: string;
  isLeaf: boolean;
  table?: DatabaseTable;
  left?: JoinTreeNode;
  right?: JoinTreeNode;
  estimatedCost: number;
  intermediateCardinality: number;
  splitIndex?: number;
}

export interface OptimizationResult {
  mode: OptimizationMode;
  modeDisplayName: string;
  tables: DatabaseTable[];
  dimensions: number[];
  dpTable: number[][]; // 1-based indexing for display [1..n][1..n]
  rootTable: number[][]; // 1-based indexing [1..n][1..n]
  optimalExpression: string;
  optimalCost: number;
  leftToRightCost: number;
  rightToLeftCost: number;
  costSavingPct: number;
  joinTreeRoot: JoinTreeNode;
  stepLogs: string[];
  executionTimeMs: number;
}

export interface SavedQuery {
  id: number;
  name: string;
  sql: string;
  tableCount: number;
  description: string;
}

export interface TestCaseResult {
  id: number;
  title: string;
  description: string;
  expected: string;
  actual: string;
  passed: boolean;
  details: string;
}
