import React, { useState } from 'react';
import { DatabaseTable, OptimizationMode, SavedQuery } from '../types';
import { parseSQLQuery, SQLParseResult } from '../algorithm/sqlParser';
import { Play, CheckCircle2, AlertTriangle, Layers } from 'lucide-react';

interface QueryPlannerProps {
  tables: DatabaseTable[];
  savedQueries: SavedQuery[];
  onOptimize: (matchedTables: DatabaseTable[], mode: OptimizationMode) => void;
  currentMode: OptimizationMode;
  setMode: (mode: OptimizationMode) => void;
}

export const QueryPlanner: React.FC<QueryPlannerProps> = ({
  tables,
  savedQueries,
  onOptimize,
  currentMode,
  setMode,
}) => {
  const [sql, setSql] = useState<string>(savedQueries[0]?.sql || '');
  const [parseResult, setParseResult] = useState<SQLParseResult | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const handleValidate = () => {
    const res = parseSQLQuery(sql);
    setParseResult(res);

    if (!res.success) {
      setValidationError(res.errorMessage || 'Invalid SQL syntax.');
      return false;
    }

    const missing: string[] = [];
    const matched: DatabaseTable[] = [];

    for (const ref of res.tables) {
      const found = tables.find((t) => t.name.toLowerCase() === ref.tableName.toLowerCase());
      if (!found) {
        missing.push(ref.tableName);
      } else {
        matched.push({
          ...found,
          alias: ref.alias || found.alias,
        });
      }
    }

    if (missing.length > 0) {
      setValidationError(
        `Table(s) [${missing.join(', ')}] referenced in SQL query are not configured in the catalog. Please configure them in Table Catalog first.`
      );
      return false;
    }

    if (matched.length < 2) {
      setValidationError('Optimization requires at least 2 joinable tables.');
      return false;
    }

    setValidationError(null);
    return true;
  };

  const handleRunOptimization = () => {
    const valid = handleValidate();
    if (!valid) return;

    const res = parseSQLQuery(sql);
    const matched: DatabaseTable[] = [];
    for (const ref of res.tables) {
      const found = tables.find((t) => t.name.toLowerCase() === ref.tableName.toLowerCase());
      if (found) {
        matched.push({
          ...found,
          alias: ref.alias || found.alias,
        });
      }
    }

    onOptimize(matched, currentMode);
  };

  const handleSelectSaved = (query: SavedQuery) => {
    setSql(query.sql);
    setParseResult(null);
    setValidationError(null);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-semibold text-white tracking-tight">
            SQL Query Input & Parser
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Enter a multi-table JOIN query to evaluate all associativity combinations via interval dynamic programming.
          </p>
        </div>

        <div className="flex items-center gap-1 p-1 bg-slate-900 border border-slate-800 rounded-lg text-xs">
          <button
            onClick={() => setMode('MODE_A_MCM')}
            className={`px-3 py-1.5 font-medium rounded-md transition-colors cursor-pointer ${
              currentMode === 'MODE_A_MCM'
                ? 'bg-indigo-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Mode A: Matrix Chain (MCM)
          </button>
          <button
            onClick={() => setMode('MODE_B_RELATIONAL')}
            className={`px-3 py-1.5 font-medium rounded-md transition-colors cursor-pointer ${
              currentMode === 'MODE_B_RELATIONAL'
                ? 'bg-indigo-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Mode B: Relational Estimator
          </button>
        </div>
      </div>

      <div className="p-3.5 rounded-xl border border-slate-800 bg-slate-900/40 text-xs text-slate-300 flex items-start gap-3">
        <div className="p-1.5 rounded-lg bg-indigo-500/10 text-indigo-400 mt-0.5">
          <Layers className="w-4 h-4" />
        </div>
        <div className="space-y-1">
          <span className="font-semibold text-white">
            {currentMode === 'MODE_A_MCM' ? 'Mode A: Matrix Chain Multiplication' : 'Mode B: Relational Join Estimator'}
          </span>
          <p className="text-slate-400 leading-relaxed">
            {currentMode === 'MODE_A_MCM'
              ? 'Applies textbook dynamic programming recurrence dp[i][j] = min_k { dp[i][k] + dp[k+1][j] + p[i-1]*p[k]*p[j] }. Tables represent matrix dimension sequences.'
              : 'Models real RDBMS query optimizers (System-R style). Estimates intermediate output cardinality = |Left| * |Right| * selectivity, and computes scan/hash join costs.'}
          </p>
        </div>
      </div>

      <div className="flex items-center gap-2 overflow-x-auto pb-1 text-xs">
        <span className="text-slate-400 font-medium whitespace-nowrap">Load Benchmark:</span>
        {savedQueries.map((q) => (
          <button
            key={q.id}
            onClick={() => handleSelectSaved(q)}
            className="px-2.5 py-1 rounded bg-slate-900 border border-slate-800 text-slate-300 hover:text-white hover:border-slate-700 whitespace-nowrap transition-colors cursor-pointer"
          >
            {q.name}
          </button>
        ))}
      </div>

      <div className="rounded-xl border border-slate-800 bg-slate-950 overflow-hidden shadow-inner">
        <div className="flex items-center justify-between px-4 py-2 bg-slate-900/80 border-b border-slate-800 text-xs">
          <span className="font-mono text-slate-400">SQL SELECT Query Editor</span>
          <span className="text-slate-500">Supports: SELECT, FROM, [INNER] JOIN ... ON</span>
        </div>
        <textarea
          rows={7}
          value={sql}
          onChange={(e) => {
            setSql(e.target.value);
            setValidationError(null);
          }}
          placeholder="SELECT * FROM Customers C JOIN Orders O ON C.customer_id = O.customer_id..."
          className="w-full bg-slate-950 p-4 font-mono text-sm text-emerald-300 placeholder:text-slate-600 focus:outline-none resize-y"
          spellCheck={false}
        />
      </div>

      {validationError && (
        <div className="p-3.5 rounded-xl border border-rose-800/60 bg-rose-950/40 text-rose-300 text-xs flex items-start gap-2.5">
          <AlertTriangle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
          <div>
            <span className="font-semibold block">Validation Failed:</span>
            <span>{validationError}</span>
          </div>
        </div>
      )}

      {parseResult && parseResult.success && !validationError && (
        <div className="p-3.5 rounded-xl border border-emerald-800/60 bg-emerald-950/40 text-emerald-300 text-xs flex items-start gap-2.5">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
          <div>
            <span className="font-semibold block">Valid SQL Join Sequence:</span>
            <span>
              Extracted {parseResult.tables.length} tables: {parseResult.tables.map((t) => t.tableName + (t.alias ? ` (${t.alias})` : '')).join(' ➔ ')}.
            </span>
          </div>
        </div>
      )}

      <div className="flex flex-wrap items-center gap-3">
        <button
          onClick={handleRunOptimization}
          className="inline-flex items-center gap-2 px-5 py-2.5 text-xs font-semibold rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white shadow-sm transition-colors cursor-pointer"
        >
          <Play className="w-4 h-4 fill-current" />
          <span>Optimize Join Order (Run DP)</span>
        </button>
        <button
          onClick={handleValidate}
          className="inline-flex items-center gap-2 px-4 py-2.5 text-xs font-medium rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors cursor-pointer"
        >
          <CheckCircle2 className="w-4 h-4 text-sky-400" />
          <span>Validate Query</span>
        </button>
      </div>
    </div>
  );
};
