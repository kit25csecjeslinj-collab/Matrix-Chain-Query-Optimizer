import React, { useState } from 'react';
import JSZip from 'jszip';
import { DatabaseTable, OptimizationMode, OptimizationResult } from './types';
import { INITIAL_TABLES, TEXTBOOK_3_TABLES, SAVED_QUERIES } from './data/sampleData';
import { JAVA_PROJECT_FILES } from './data/javaProjectFiles';
import { optimizeMatrixChain } from './algorithm/matrixChain';
import { optimizeRelationalJoin } from './algorithm/relationalEstimator';
import { Navbar, ActiveTab } from './components/Navbar';
import { Dashboard } from './components/Dashboard';
import { TableCatalog } from './components/TableCatalog';
import { QueryPlanner } from './components/QueryPlanner';
import { DPHeatmap } from './components/DPHeatmap';
import { JoinTreeVisualizer } from './components/JoinTreeVisualizer';
import { CostComparison } from './components/CostComparison';
import { TestRunner } from './components/TestRunner';
import { JavaCodeExplorer } from './components/JavaCodeExplorer';
import { ReportViewer } from './components/ReportViewer';

export default function App() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('dashboard');
  const [tables, setTables] = useState<DatabaseTable[]>(INITIAL_TABLES);
  const [mode, setMode] = useState<OptimizationMode>('MODE_A_MCM');
  const [result, setResult] = useState<OptimizationResult | null>(() => {
    return optimizeMatrixChain(INITIAL_TABLES);
  });
  const [isDownloading, setIsDownloading] = useState(false);

  // Table Management Handlers
  const handleAddTable = (newTable: Omit<DatabaseTable, 'id'>) => {
    const nextId = tables.length > 0 ? Math.max(...tables.map((t) => t.id)) + 1 : 1;
    const added: DatabaseTable = { id: nextId, ...newTable };
    const updated = [...tables, added];
    setTables(updated);
    runOptimizationOnTables(updated, mode);
  };

  const handleUpdateTable = (updatedTable: DatabaseTable) => {
    const updated = tables.map((t) => (t.id === updatedTable.id ? updatedTable : t));
    setTables(updated);
    runOptimizationOnTables(updated, mode);
  };

  const handleDeleteTable = (id: number) => {
    if (tables.length <= 2) {
      alert('Optimization requires at least 2 database tables.');
      return;
    }
    const updated = tables.filter((t) => t.id !== id);
    setTables(updated);
    runOptimizationOnTables(updated, mode);
  };

  const handleResetDefault = () => {
    setTables(INITIAL_TABLES);
    runOptimizationOnTables(INITIAL_TABLES, mode);
  };

  const handleLoadTextbook = () => {
    setTables(TEXTBOOK_3_TABLES);
    setMode('MODE_A_MCM');
    // For standard textbook benchmark: A1(10x30), A2(30x5), A3(5x60) -> p = [10, 30, 5, 60]
    const customDims = [10, 30, 5, 60];
    const res = optimizeMatrixChain(TEXTBOOK_3_TABLES, customDims);
    setResult(res);
    setActiveTab('comparison');
  };

  const runOptimizationOnTables = (tableList: DatabaseTable[], optMode: OptimizationMode) => {
    if (tableList.length < 2) return;
    if (optMode === 'MODE_A_MCM') {
      const res = optimizeMatrixChain(tableList);
      setResult(res);
    } else {
      const res = optimizeRelationalJoin(tableList);
      setResult(res);
    }
  };

  const handleOptimize = (matchedTables: DatabaseTable[], optMode: OptimizationMode) => {
    setMode(optMode);
    runOptimizationOnTables(matchedTables, optMode);
    setActiveTab('heatmap');
  };

  const handleRunDemo = (optMode: OptimizationMode) => {
    setMode(optMode);
    runOptimizationOnTables(tables, optMode);
    setActiveTab('heatmap');
  };

  // One-click Eclipse Maven Project ZIP Downloader using JSZip
  const handleDownloadZip = async () => {
    try {
      setIsDownloading(true);
      const zip = new JSZip();
      const folder = zip.folder('matrix-chain-query-optimizer');

      if (folder) {
        // Add all project files
        for (const file of JAVA_PROJECT_FILES) {
          folder.file(file.path, file.code);
        }

        const blob = await zip.generateAsync({ type: 'blob' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'matrix-chain-query-optimizer-java-project.zip';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      }
    } catch (err) {
      console.error('Failed to create project ZIP:', err);
      alert('Error bundling project ZIP. You can still copy individual files in the Java Codebase tab.');
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onDownloadZip={handleDownloadZip}
        isDownloading={isDownloading}
        hasResult={result !== null}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {activeTab === 'dashboard' && (
          <Dashboard
            tables={tables}
            result={result}
            onRunDemo={handleRunDemo}
            onNavigate={setActiveTab}
            onLoadStandard3Matrix={handleLoadTextbook}
          />
        )}

        {activeTab === 'tables' && (
          <TableCatalog
            tables={tables}
            onAddTable={handleAddTable}
            onUpdateTable={handleUpdateTable}
            onDeleteTable={handleDeleteTable}
            onResetDefault={handleResetDefault}
            onLoadTextbook={handleLoadTextbook}
          />
        )}

        {activeTab === 'planner' && (
          <QueryPlanner
            tables={tables}
            savedQueries={SAVED_QUERIES}
            onOptimize={handleOptimize}
            currentMode={mode}
            setMode={setMode}
          />
        )}

        {activeTab === 'heatmap' && <DPHeatmap result={result} />}

        {activeTab === 'tree' && <JoinTreeVisualizer result={result} />}

        {activeTab === 'comparison' && <CostComparison result={result} />}

        {activeTab === 'tests' && <TestRunner />}

        {activeTab === 'java-code' && (
          <JavaCodeExplorer
            onDownloadZip={handleDownloadZip}
            isDownloading={isDownloading}
          />
        )}

        {activeTab === 'report' && <ReportViewer />}
      </main>

      {/* Clean Academic Footer */}
      <footer className="border-t border-slate-800/80 bg-slate-950 py-4 text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-2">
          <div>
            <span>Matrix Chain Query Optimizer</span> &bull; <span>Dynamic Programming $O(n^3)$</span> &bull; <span>CS 400 Database Systems</span>
          </div>
          <div className="flex items-center gap-4">
            <button
              onClick={() => setActiveTab('java-code')}
              className="text-slate-400 hover:text-slate-200 transition-colors"
            >
              Java 17 Codebase
            </button>
            <button
              onClick={() => setActiveTab('report')}
              className="text-slate-400 hover:text-slate-200 transition-colors"
            >
              Academic Report
            </button>
            <button
              onClick={handleDownloadZip}
              className="text-indigo-400 hover:text-indigo-300 transition-colors"
            >
              Download Project ZIP
            </button>
          </div>
        </div>
      </footer>
    </div>
  );
}
