import React from 'react';
import { OptimizationResult } from '../types';
import { BarChart3, TrendingDown, ShieldAlert } from 'lucide-react';

interface CostComparisonProps {
  result: OptimizationResult | null;
}

export const CostComparison: React.FC<CostComparisonProps> = ({ result }) => {
  if (!result) {
    return (
      <div className="p-12 text-center rounded-xl border border-slate-800 bg-slate-900/30">
        <BarChart3 className="w-10 h-10 text-slate-600 mx-auto mb-3" />
        <h3 className="text-base font-semibold text-white">No Comparison Data</h3>
        <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
          Execute an optimization run to generate baseline cost comparisons and improvement metrics.
        </p>
      </div>
    );
  }

  const { optimalCost, leftToRightCost, rightToLeftCost, costSavingPct, optimalExpression } = result;

  const maxVal = Math.max(optimalCost, Math.max(leftToRightCost, rightToLeftCost)) || 1;

  const strategies = [
    {
      name: '★ Optimal Dynamic Programming Order',
      expression: optimalExpression,
      cost: optimalCost,
      color: 'bg-emerald-500',
      badgeColor: 'text-emerald-400 bg-emerald-950/60 border-emerald-800/60',
      diff: 0,
      pctDiff: '0.00% (Optimal Baseline)',
      isBest: true,
    },
    {
      name: 'Left-to-Right Evaluation Order',
      expression: '(((A ⋈ B) ⋈ C) ⋈ D) ...',
      cost: leftToRightCost,
      color: 'bg-sky-500',
      badgeColor: 'text-sky-400 bg-sky-950/60 border-sky-800/60',
      diff: leftToRightCost - optimalCost,
      pctDiff: `+${(((leftToRightCost - optimalCost) / (optimalCost || 1)) * 100).toFixed(1)}% extra cost`,
      isBest: false,
    },
    {
      name: 'Right-to-Left Evaluation Order',
      expression: '(A ⋈ (B ⋈ (C ⋈ D))) ...',
      cost: rightToLeftCost,
      color: 'bg-rose-500',
      badgeColor: 'text-rose-400 bg-rose-950/60 border-rose-800/60',
      diff: rightToLeftCost - optimalCost,
      pctDiff: `+${(((rightToLeftCost - optimalCost) / (optimalCost || 1)) * 100).toFixed(1)}% extra cost`,
      isBest: false,
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-semibold text-white tracking-tight">
            Plan Cost Comparison & Savings Breakdown
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Empirical comparison of the DP optimizer versus default left-to-right and right-to-left linear plans.
          </p>
        </div>

        <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg bg-emerald-950/40 border border-emerald-800/60 text-emerald-400 text-xs font-mono font-semibold">
          <TrendingDown className="w-4 h-4" />
          <span>{costSavingPct.toFixed(1)}% Reduction vs Left-to-Right</span>
        </div>
      </div>

      <div className="p-6 rounded-xl border border-slate-800 bg-slate-900/40 space-y-5">
        <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-300">
          Estimated Execution Workload (Lower is Better)
        </h3>

        <div className="space-y-4">
          {strategies.map((strat, idx) => {
            const widthPct = Math.max(6, (strat.cost / maxVal) * 100);
            return (
              <div key={idx} className="space-y-1.5">
                <div className="flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-white">{strat.name}</span>
                    {strat.isBest && (
                      <span className="text-[10px] px-1.5 py-0.2 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                        Selected
                      </span>
                    )}
                  </div>
                  <span className="font-mono font-bold text-slate-200">
                    {strat.cost.toLocaleString()} ops
                  </span>
                </div>

                <div className="w-full bg-slate-950 rounded-full h-4 overflow-hidden border border-slate-800 p-0.5">
                  <div
                    className={`h-full rounded-full transition-all duration-700 ${strat.color}`}
                    style={{ width: `${widthPct}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      <div className="rounded-xl border border-slate-800 bg-slate-900/40 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900 text-slate-400 font-semibold uppercase tracking-wider border-b border-slate-800">
              <tr>
                <th className="py-3 px-4">Evaluation Strategy</th>
                <th className="py-3 px-4">Join Order Structure</th>
                <th className="py-3 px-4">Estimated Total Cost</th>
                <th className="py-3 px-4">Cost Delta (vs DP)</th>
                <th className="py-3 px-4">Efficiency Gain</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {strategies.map((strat, idx) => (
                <tr key={idx} className="hover:bg-slate-800/30 transition-colors">
                  <td className="py-3 px-4 font-sans font-bold text-white">
                    {strat.name}
                  </td>
                  <td className="py-3 px-4 text-slate-300 max-w-xs truncate font-sans">
                    {strat.expression}
                  </td>
                  <td className="py-3 px-4 font-bold text-emerald-400">
                    {strat.cost.toLocaleString()} ops
                  </td>
                  <td className="py-3 px-4 text-slate-300">
                    {strat.diff === 0 ? '—' : `+${strat.diff.toLocaleString()}`}
                  </td>
                  <td className="py-3 px-4">
                    <span className={`px-2 py-0.5 rounded text-[11px] font-sans border ${strat.badgeColor}`}>
                      {strat.pctDiff}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="p-4 rounded-xl border border-amber-800/40 bg-amber-950/20 text-xs text-amber-200/90 flex items-start gap-3">
        <ShieldAlert className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
        <div className="space-y-1">
          <span className="font-semibold block text-amber-300">Educational Cost Model Assumptions</span>
          <p className="text-amber-200/80 leading-relaxed text-[11px]">
            Cost figures represent estimated scalar operations or I/O scan costs based on table cardinalities and selectivity approximations. Real RDBMS engines also factor in hardware buffer pools, index b-trees, sorting overhead, and pipelining.
          </p>
        </div>
      </div>
    </div>
  );
};
