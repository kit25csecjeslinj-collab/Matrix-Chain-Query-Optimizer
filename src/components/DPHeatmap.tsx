import React, { useState } from 'react';
import { OptimizationResult } from '../types';
import { Layers } from 'lucide-react';

interface DPHeatmapProps {
  result: OptimizationResult | null;
}

export const DPHeatmap: React.FC<DPHeatmapProps> = ({ result }) => {
  const [selectedCell, setSelectedCell] = useState<{ i: number; j: number } | null>(null);

  if (!result || !result.dpTable || result.dpTable.length <= 1) {
    return (
      <div className="p-12 text-center rounded-xl border border-slate-800 bg-slate-900/30">
        <Layers className="w-10 h-10 text-slate-600 mx-auto mb-3" />
        <h3 className="text-base font-semibold text-white">No DP Heatmap Data</h3>
        <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
          Run query optimization from the SQL Planner or Dashboard to generate the dynamic programming cost matrix.
        </p>
      </div>
    );
  }

  const { dpTable, rootTable, tables, dimensions, mode } = result;
  const n = tables.length;

  let minCost = Number.MAX_SAFE_INTEGER;
  let maxCost = 0;
  for (let i = 1; i <= n; i++) {
    for (let j = i + 1; j <= n; j++) {
      const val = dpTable[i]?.[j];
      if (val !== undefined && val < Number.MAX_SAFE_INTEGER) {
        if (val < minCost) minCost = val;
        if (val > maxCost) maxCost = val;
      }
    }
  }
  if (minCost === Number.MAX_SAFE_INTEGER) minCost = 0;
  if (maxCost === 0) maxCost = 1;

  const activeI = selectedCell ? selectedCell.i : 1;
  const activeJ = selectedCell ? selectedCell.j : n;
  const activeCost = dpTable[activeI]?.[activeJ] ?? 0;
  const activeSplit = rootTable[activeI]?.[activeJ] ?? 0;

  const formatVal = (v: number) => {
    if (v >= 1e9) return (v / 1e9).toFixed(1) + 'B';
    if (v >= 1e6) return (v / 1e6).toFixed(1) + 'M';
    if (v >= 1e3) return (v / 1e3).toFixed(1) + 'K';
    return v.toString();
  };

  const getCellColor = (i: number, j: number) => {
    if (j < i) return 'bg-slate-950/40 text-slate-700 border-slate-800/40';
    if (i === j) return 'bg-emerald-950/30 text-emerald-400 border-emerald-800/40 font-bold';

    const cost = dpTable[i]?.[j] ?? 0;
    const ratio = Math.max(0, Math.min(1, (cost - minCost) / (maxCost - minCost || 1)));

    if (ratio < 0.25) return 'bg-teal-950/50 text-teal-300 border-teal-800/50 hover:border-teal-400';
    if (ratio < 0.5) return 'bg-indigo-950/60 text-indigo-300 border-indigo-800/50 hover:border-indigo-400';
    if (ratio < 0.75) return 'bg-amber-950/60 text-amber-300 border-amber-800/50 hover:border-amber-400';
    return 'bg-rose-950/60 text-rose-300 border-rose-800/50 hover:border-rose-400';
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-semibold text-white tracking-tight">
            Dynamic Programming Cost Heatmap (dp[i][j])
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Click any upper-triangular cell to inspect subproblem boundaries, recurrence values, and chosen split point k.
          </p>
        </div>

        <div className="flex items-center gap-3 text-[11px] text-slate-400">
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded bg-emerald-950/60 border border-emerald-800"></span>
            <span>Base (0)</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded bg-teal-950/60 border border-teal-800"></span>
            <span>Low Cost</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded bg-amber-950/60 border border-amber-800"></span>
            <span>Medium</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded bg-rose-950/60 border border-rose-800"></span>
            <span>High Cost</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded border-2 border-amber-400"></span>
            <span className="text-amber-300 font-medium">Optimal [1..{n}]</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 p-4 rounded-xl border border-slate-800 bg-slate-900/40 overflow-x-auto">
          <table className="border-collapse mx-auto">
            <thead>
              <tr>
                <th className="p-2 text-xs font-mono text-slate-500 text-right pr-4">i \ j</th>
                {tables.map((t, colIdx) => (
                  <th key={colIdx} className="p-2 text-center text-xs font-mono text-slate-300">
                    <div className="font-bold">j={colIdx + 1}</div>
                    <div className="text-[10px] text-slate-400 font-sans max-w-[70px] truncate">
                      {t.alias || t.name}
                    </div>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {tables.map((rowTable, rowIdx) => {
                const i = rowIdx + 1;
                return (
                  <tr key={rowIdx}>
                    <td className="p-2 text-right pr-4 text-xs font-mono text-slate-300 whitespace-nowrap">
                      <span className="font-bold">i={i}</span>
                      <span className="ml-1.5 text-[10px] text-slate-400 font-sans">
                        {rowTable.alias || rowTable.name}
                      </span>
                    </td>

                    {tables.map((_, colIdx) => {
                      const j = colIdx + 1;
                      const isDiagonal = i === j;
                      const isLower = j < i;
                      const isOptimal = i === 1 && j === n;
                      const isSelected = i === activeI && j === activeJ;
                      const cost = dpTable[i]?.[j] ?? 0;
                      const split = rootTable[i]?.[j] ?? 0;

                      return (
                        <td key={colIdx} className="p-1">
                          <button
                            disabled={isLower}
                            onClick={() => setSelectedCell({ i, j })}
                            className={`w-16 h-16 rounded-lg border text-center transition-all flex flex-col items-center justify-center relative ${getCellColor(
                              i,
                              j
                            )} ${
                              isSelected
                                ? 'ring-2 ring-indigo-400 scale-105 z-10 shadow-lg'
                                : ''
                            } ${isOptimal ? 'border-amber-400 border-2 font-bold' : ''} ${
                              isLower ? 'cursor-not-allowed opacity-30' : 'cursor-pointer'
                            }`}
                          >
                            {isLower ? (
                              <span className="text-slate-700 text-xs">—</span>
                            ) : isDiagonal ? (
                              <>
                                <span className="font-mono text-xs font-bold text-emerald-400">0</span>
                                <span className="text-[9px] text-emerald-500 font-sans">base</span>
                              </>
                            ) : (
                              <>
                                <span className="font-mono text-xs font-bold">
                                  {formatVal(cost)}
                                </span>
                                {split > 0 && (
                                  <span className="text-[9px] text-amber-300 font-mono mt-0.5">
                                    k={split}
                                  </span>
                                )}
                              </>
                            )}
                          </button>
                        </td>
                      );
                    })}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        <div className="space-y-4">
          <div className="p-5 rounded-xl border border-slate-800 bg-slate-900/60 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <span className="text-xs font-semibold uppercase tracking-wider text-indigo-400">
                Cell [{activeI}, {activeJ}] Recurrence Inspector
              </span>
              <span className="text-xs font-mono text-slate-400">
                Interval: {tables[activeI - 1]?.name} ➔ {tables[activeJ - 1]?.name}
              </span>
            </div>

            {activeI === activeJ ? (
              <div className="text-xs text-slate-300 space-y-2">
                <div className="p-3 rounded-lg bg-emerald-950/30 border border-emerald-800/40 text-emerald-300 font-mono">
                  dp[{activeI}][{activeJ}] = 0 (Base Case)
                </div>
                <p className="text-slate-400 text-xs leading-relaxed">
                  A single table requires zero join multiplications. Cost of evaluating relation {tables[activeI - 1]?.name} alone is 0.
                </p>
              </div>
            ) : (
              <div className="space-y-3 text-xs">
                <div>
                  <span className="text-slate-400 block mb-1">Minimum Evaluated Cost:</span>
                  <div className="text-xl font-bold font-mono text-emerald-400">
                    {activeCost.toLocaleString()} operations
                  </div>
                </div>

                <div className="p-3 rounded-lg bg-slate-950 border border-slate-800 font-mono text-[11px] text-slate-300 space-y-1">
                  <div className="text-indigo-400 font-semibold mb-1">Optimal Split: k = {activeSplit}</div>
                  <div>
                    Left Subproblem: <span className="text-sky-300">dp[{activeI}][{activeSplit}]</span> = {(dpTable[activeI]?.[activeSplit] ?? 0).toLocaleString()}
                  </div>
                  <div>
                    Right Subproblem: <span className="text-purple-300">dp[{activeSplit + 1}][{activeJ}]</span> = {(dpTable[activeSplit + 1]?.[activeJ] ?? 0).toLocaleString()}
                  </div>
                  {mode === 'MODE_A_MCM' && dimensions.length > 0 && (
                    <div className="text-amber-300 pt-1 border-t border-slate-800">
                      Join Scalar Work: p[{activeI - 1}] × p[{activeSplit}] × p[{activeJ}]
                      <div className="text-slate-400">
                        = {dimensions[activeI - 1]} × {dimensions[activeSplit]} × {dimensions[activeJ]} = {(dimensions[activeI - 1] * dimensions[activeSplit] * dimensions[activeJ]).toLocaleString()}
                      </div>
                    </div>
                  )}
                </div>

                <div className="p-3 rounded-lg bg-slate-900 border border-slate-800 text-slate-400 leading-relaxed text-[11px]">
                  <strong className="text-slate-200">Optimal Subtree Order:</strong>
                  <div className="text-amber-300 font-mono mt-1">
                    ({tables.slice(activeI - 1, activeSplit).map((t) => t.name).join(' ⋈ ')}) ⋈ ({tables.slice(activeSplit, activeJ).map((t) => t.name).join(' ⋈ ')})
                  </div>
                </div>
              </div>
            )}
          </div>

          <div className="p-4 rounded-xl border border-slate-800 bg-slate-900/40 text-xs space-y-2">
            <span className="font-semibold text-slate-300 block uppercase tracking-wider text-[11px]">
              DP Recurrence Complexity
            </span>
            <p className="text-slate-400 leading-relaxed text-[11px]">
              The total number of interval subproblems evaluated is <span className="font-mono text-indigo-300">n(n-1)/2 = {(n * (n - 1)) / 2}</span>. At each subproblem, up to <span className="font-mono text-indigo-300">n</span> split positions are checked, giving exact <span className="font-mono text-emerald-300">O(n³)</span> runtime.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
