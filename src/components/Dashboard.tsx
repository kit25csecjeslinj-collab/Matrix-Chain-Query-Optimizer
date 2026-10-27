import React from 'react';
import { DatabaseTable, OptimizationResult } from '../types';
import { 
  Play, 
  Sparkles, 
  Database, 
  ArrowRight, 
  Zap, 
  CheckCircle, 
  Code2, 
  Cpu
} from 'lucide-react';

interface DashboardProps {
  tables: DatabaseTable[];
  result: OptimizationResult | null;
  onRunDemo: (mode: 'MODE_A_MCM' | 'MODE_B_RELATIONAL') => void;
  onNavigate: (tab: any) => void;
  onLoadStandard3Matrix: () => void;
}

export const Dashboard: React.FC<DashboardProps> = ({
  tables,
  result,
  onRunDemo,
  onNavigate,
  onLoadStandard3Matrix,
}) => {
  return (
    <div className="space-y-6">
      {/* Hero Banner */}
      <div className="relative overflow-hidden rounded-xl border border-slate-800 bg-gradient-to-br from-slate-900 via-slate-900/90 to-indigo-950/40 p-6 md:p-8">
        <div className="max-w-3xl space-y-4">
          <div className="inline-flex items-center gap-2 text-xs font-mono text-indigo-400 bg-indigo-950/50 px-2.5 py-1 rounded-md border border-indigo-800/50">
            <Cpu className="w-3.5 h-3.5" />
            <span>Dynamic Programming on Intervals &bull; Matrix Chain Recurrence</span>
          </div>
          <h1 className="text-2xl md:text-3xl font-bold text-white tracking-tight">
            Matrix Chain Query Optimizer
          </h1>
          <p className="text-sm md:text-base text-slate-300 leading-relaxed">
            Simulate how modern relational database query optimizers calculate the minimum-cost join tree for multi-table queries. Avoid exponential Catalan explosions with dynamic programming on sub-chains in <span className="font-mono text-indigo-300">O(n³)</span> time and <span className="font-mono text-indigo-300">O(n²)</span> space.
          </p>

          <div className="flex flex-wrap items-center gap-3 pt-2">
            <button
              onClick={() => onRunDemo('MODE_A_MCM')}
              className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white shadow-sm transition-colors cursor-pointer"
            >
              <Play className="w-3.5 h-3.5 fill-current" />
              <span>Optimize 5-Table Join (Mode A)</span>
            </button>
            <button
              onClick={() => onRunDemo('MODE_B_RELATIONAL')}
              className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors cursor-pointer"
            >
              <Zap className="w-3.5 h-3.5 text-amber-400" />
              <span>Relational Join Estimator (Mode B)</span>
            </button>
            <button
              onClick={onLoadStandard3Matrix}
              className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold rounded-lg bg-emerald-950/60 hover:bg-emerald-900/60 text-emerald-300 border border-emerald-800/60 transition-colors cursor-pointer"
              title="Loads textbook 10x30, 30x5, 5x60 benchmark verifying 4,500 cost"
            >
              <Sparkles className="w-3.5 h-3.5" />
              <span>Load 4,500 Textbook Benchmark</span>
            </button>
          </div>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-4 rounded-xl border border-slate-800 bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">Catalog Tables</span>
            <Database className="w-4 h-4 text-sky-400" />
          </div>
          <div className="text-2xl font-bold text-white font-mono">{tables.length}</div>
          <div className="text-xs text-slate-400 mt-1">Configured relation schemas</div>
        </div>

        <div className="p-4 rounded-xl border border-slate-800 bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">Optimal Cost</span>
            <Zap className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-emerald-400 font-mono">
            {result ? result.optimalCost.toLocaleString() : '—'}
          </div>
          <div className="text-xs text-slate-400 mt-1">
            {result ? result.modeDisplayName.split('(')[0] : 'Run optimization'}
          </div>
        </div>

        <div className="p-4 rounded-xl border border-slate-800 bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">Cost Reduction</span>
            <Sparkles className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold text-amber-400 font-mono">
            {result ? `${result.costSavingPct.toFixed(1)}%` : '—'}
          </div>
          <div className="text-xs text-slate-400 mt-1">Savings vs Left-to-Right plan</div>
        </div>

        <div className="p-4 rounded-xl border border-slate-800 bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">Search Space Saved</span>
            <CheckCircle className="w-4 h-4 text-indigo-400" />
          </div>
          <div className="text-2xl font-bold text-indigo-300 font-mono">
            {tables.length >= 5 ? '14 Trees' : `${tables.length} Tables`}
          </div>
          <div className="text-xs text-slate-400 mt-1">Catalan C(n-1) evaluated in O(n³)</div>
        </div>
      </div>

      {/* Current Result Quick View */}
      {result && (
        <div className="p-5 rounded-xl border border-slate-800 bg-slate-900/40 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-semibold text-white uppercase tracking-wider">
              Latest Optimization Plan ({result.modeDisplayName})
            </h2>
            <button
              onClick={() => onNavigate('heatmap')}
              className="text-xs text-indigo-400 hover:text-indigo-300 flex items-center gap-1 font-medium cursor-pointer"
            >
              <span>Inspect DP Heatmap</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="p-3.5 rounded-lg bg-slate-950 border border-slate-800 font-mono text-sm text-amber-300 overflow-x-auto">
            {result.optimalExpression}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-xs">
            <div className="p-3 rounded-lg bg-slate-900 border border-slate-800/80">
              <span className="text-slate-400">Optimal DP Cost:</span>
              <p className="text-emerald-400 font-mono font-semibold text-sm mt-0.5">
                {result.optimalCost.toLocaleString()} ops
              </p>
            </div>
            <div className="p-3 rounded-lg bg-slate-900 border border-slate-800/80">
              <span className="text-slate-400">Baseline Left-to-Right:</span>
              <p className="text-slate-200 font-mono font-semibold text-sm mt-0.5">
                {result.leftToRightCost.toLocaleString()} ops
              </p>
            </div>
            <div className="p-3 rounded-lg bg-slate-900 border border-slate-800/80">
              <span className="text-slate-400">Execution Runtime:</span>
              <p className="text-sky-400 font-mono font-semibold text-sm mt-0.5">
                {result.executionTimeMs} ms
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Feature Navigation Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div 
          onClick={() => onNavigate('planner')}
          className="p-5 rounded-xl border border-slate-800/80 bg-slate-900/30 hover:bg-slate-900/60 hover:border-slate-700 transition-all cursor-pointer group"
        >
          <div className="w-8 h-8 rounded-lg bg-sky-500/10 text-sky-400 flex items-center justify-center mb-3">
            <Play className="w-4 h-4" />
          </div>
          <h3 className="text-sm font-semibold text-white group-hover:text-sky-300 transition-colors">
            SQL Query Parser & Validator
          </h3>
          <p className="text-xs text-slate-400 mt-1.5 leading-relaxed">
            Extracts tables, aliases, and join keys from SQL SELECT queries and maps them to catalog cardinalities.
          </p>
        </div>

        <div 
          onClick={() => onNavigate('heatmap')}
          className="p-5 rounded-xl border border-slate-800/80 bg-slate-900/30 hover:bg-slate-900/60 hover:border-slate-700 transition-all cursor-pointer group"
        >
          <div className="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-400 flex items-center justify-center mb-3">
            <Zap className="w-4 h-4" />
          </div>
          <h3 className="text-sm font-semibold text-white group-hover:text-amber-300 transition-colors">
            Interactive DP Heatmap
          </h3>
          <p className="text-xs text-slate-400 mt-1.5 leading-relaxed">
            Click any cell $(i, j)$ in the cost matrix to inspect the recurrence relation, split point $k$, and subproblem costs.
          </p>
        </div>

        <div 
          onClick={() => onNavigate('java-code')}
          className="p-5 rounded-xl border border-slate-800/80 bg-slate-900/30 hover:bg-slate-900/60 hover:border-slate-700 transition-all cursor-pointer group"
        >
          <div className="w-8 h-8 rounded-lg bg-purple-500/10 text-purple-400 flex items-center justify-center mb-3">
            <Code2 className="w-4 h-4" />
          </div>
          <h3 className="text-sm font-semibold text-white group-hover:text-purple-300 transition-colors">
            Eclipse IDE Java Codebase
          </h3>
          <p className="text-xs text-slate-400 mt-1.5 leading-relaxed">
            Explore and download every Java source file, Maven pom.xml, Swing UI, MySQL schema, and 10 unit tests.
          </p>
        </div>
      </div>
    </div>
  );
};
