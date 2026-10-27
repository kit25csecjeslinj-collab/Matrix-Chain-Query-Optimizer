import React from 'react';
import { JoinTreeNode, OptimizationResult } from '../types';
import { GitFork } from 'lucide-react';

interface JoinTreeVisualizerProps {
  result: OptimizationResult | null;
}

interface TreeLayoutNode {
  node: JoinTreeNode;
  x: number;
  y: number;
  width: number;
  height: number;
  children: TreeLayoutNode[];
}

export const JoinTreeVisualizer: React.FC<JoinTreeVisualizerProps> = ({ result }) => {
  if (!result || !result.joinTreeRoot) {
    return (
      <div className="p-12 text-center rounded-xl border border-slate-800 bg-slate-900/30">
        <GitFork className="w-10 h-10 text-slate-600 mx-auto mb-3" />
        <h3 className="text-base font-semibold text-white">No Join Tree Available</h3>
        <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
          Execute query optimization from the SQL Planner to build and render the binary join evaluation tree.
        </p>
      </div>
    );
  }

  const nodeW = 140;
  const nodeH = 50;
  const levelH = 80;

  function buildLayout(node: JoinTreeNode, depth: number, offsetIndex: { current: number }): TreeLayoutNode {
    const children: TreeLayoutNode[] = [];
    if (node.left) {
      children.push(buildLayout(node.left, depth + 1, offsetIndex));
    }
    if (node.right) {
      children.push(buildLayout(node.right, depth + 1, offsetIndex));
    }

    let x = 0;
    if (children.length === 0) {
      x = offsetIndex.current * (nodeW + 30) + nodeW / 2 + 20;
      offsetIndex.current += 1;
    } else if (children.length === 1) {
      x = children[0].x;
    } else {
      x = (children[0].x + children[1].x) / 2;
    }

    const y = depth * levelH + 40;

    return {
      node,
      x,
      y,
      width: nodeW,
      height: nodeH,
      children,
    };
  }

  const offsetIndex = { current: 0 };
  const layoutRoot = buildLayout(result.joinTreeRoot, 0, offsetIndex);
  const totalWidth = Math.max(700, offsetIndex.current * (nodeW + 30) + 60);

  function renderLinks(node: TreeLayoutNode): React.ReactNode[] {
    const elements: React.ReactNode[] = [];
    for (const child of node.children) {
      elements.push(
        <path
          key={`link-${node.node.id}-${child.node.id}`}
          d={`M ${node.x} ${node.y + node.height / 2} C ${node.x} ${node.y + levelH * 0.5}, ${child.x} ${child.y - levelH * 0.5}, ${child.x} ${child.y - child.height / 2}`}
          fill="none"
          stroke="#475569"
          strokeWidth="2"
        />
      );
      elements.push(...renderLinks(child));
    }
    return elements;
  }

  function renderNodes(node: TreeLayoutNode): React.ReactNode[] {
    const elements: React.ReactNode[] = [];
    const isLeaf = node.node.isLeaf;

    elements.push(
      <g
        key={`node-${node.node.id}`}
        transform={`translate(${node.x - node.width / 2}, ${node.y - node.height / 2})`}
        className="cursor-pointer"
      >
        <rect
          width={node.width}
          height={node.height}
          rx="8"
          className={`${
            isLeaf
              ? 'fill-slate-900 stroke-sky-400/80 stroke-2 hover:fill-slate-800'
              : 'fill-indigo-950 stroke-indigo-400 stroke-2 hover:fill-indigo-900'
          } transition-all`}
        />
        <text
          x={node.width / 2}
          y={18}
          textAnchor="middle"
          className={`text-xs font-bold font-sans ${isLeaf ? 'fill-sky-300' : 'fill-amber-300'}`}
        >
          {isLeaf ? node.node.label : `⋈ Join`}
        </text>
        <text
          x={node.width / 2}
          y={34}
          textAnchor="middle"
          className="text-[10px] fill-slate-300 font-mono"
        >
          {isLeaf
            ? `${node.node.table?.rowCount.toLocaleString()} rows`
            : `Cost: ${node.node.estimatedCost.toLocaleString()}`}
        </text>
      </g>
    );

    for (const child of node.children) {
      elements.push(...renderNodes(child));
    }
    return elements;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-semibold text-white tracking-tight">
            Optimal Binary Join Evaluation Tree
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Hierarchical parenthesization graph where base tables are leaves and intermediate joins are internal nodes.
          </p>
        </div>

        <div className="flex items-center gap-4 text-xs font-mono">
          <div className="flex items-center gap-1.5 text-sky-400">
            <span className="w-3 h-3 rounded bg-slate-900 border border-sky-400"></span>
            <span>Base Table (Leaf)</span>
          </div>
          <div className="flex items-center gap-1.5 text-indigo-400">
            <span className="w-3 h-3 rounded bg-indigo-950 border border-indigo-400"></span>
            <span>Intermediate Join (Internal)</span>
          </div>
        </div>
      </div>

      <div className="p-4 rounded-xl border border-slate-800 bg-slate-950/80 overflow-x-auto shadow-inner">
        <svg
          width={totalWidth}
          height={380}
          className="mx-auto block"
        >
          <g>{renderLinks(layoutRoot)}</g>
          <g>{renderNodes(layoutRoot)}</g>
        </svg>
      </div>

      <div className="p-4 rounded-xl border border-slate-800 bg-slate-900/40 space-y-2">
        <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
          Generated Join Order Expression
        </span>
        <div className="p-3 rounded-lg bg-slate-950 border border-slate-800 font-mono text-sm text-amber-300 overflow-x-auto">
          {result.optimalExpression}
        </div>
        <p className="text-xs text-slate-400">
          Reconstructed recursively by traversing split indices k = root[i][j] from root to leaves.
        </p>
      </div>
    </div>
  );
};
