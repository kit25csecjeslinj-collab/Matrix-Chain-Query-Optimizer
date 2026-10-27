import React from 'react';
import { 
  LayoutDashboard, 
  Database, 
  Terminal, 
  Grid3X3, 
  GitFork, 
  BarChart3, 
  CheckCircle2, 
  FileCode2, 
  BookOpen, 
  Download 
} from 'lucide-react';

export type ActiveTab = 
  | 'dashboard' 
  | 'tables' 
  | 'planner' 
  | 'heatmap' 
  | 'tree' 
  | 'comparison' 
  | 'tests' 
  | 'java-code' 
  | 'report';

interface NavbarProps {
  activeTab: ActiveTab;
  setActiveTab: (tab: ActiveTab) => void;
  onDownloadZip: () => void;
  isDownloading: boolean;
  hasResult: boolean;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  setActiveTab,
  onDownloadZip,
  isDownloading,
  hasResult,
}) => {
  const navItems: { id: ActiveTab; label: string; icon: React.ReactNode; badge?: string }[] = [
    { id: 'dashboard', label: 'Dashboard', icon: <LayoutDashboard className="w-4 h-4" /> },
    { id: 'tables', label: 'Table Catalog', icon: <Database className="w-4 h-4" /> },
    { id: 'planner', label: 'SQL Planner', icon: <Terminal className="w-4 h-4" /> },
    { id: 'heatmap', label: 'DP Heatmap', icon: <Grid3X3 className="w-4 h-4" />, badge: hasResult ? 'Ready' : undefined },
    { id: 'tree', label: 'Join Tree', icon: <GitFork className="w-4 h-4" /> },
    { id: 'comparison', label: 'Cost Analysis', icon: <BarChart3 className="w-4 h-4" /> },
    { id: 'tests', label: 'Test Suite (10)', icon: <CheckCircle2 className="w-4 h-4" /> },
    { id: 'java-code', label: 'Java Codebase', icon: <FileCode2 className="w-4 h-4" /> },
    { id: 'report', label: 'Academic Report', icon: <BookOpen className="w-4 h-4" /> },
  ];

  return (
    <header className="border-b border-slate-800 bg-slate-950/80 backdrop-blur sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-indigo-600/20 border border-indigo-500/40 flex items-center justify-center text-indigo-400 font-mono font-bold text-lg">
              dp
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-semibold text-slate-100 text-base tracking-tight">
                  Matrix Chain Query Optimizer
                </span>
                <span className="text-[10px] uppercase font-mono px-1.5 py-0.5 rounded bg-indigo-500/10 text-indigo-400 border border-indigo-500/30">
                  Java 17 &bull; Eclipse
                </span>
              </div>
              <p className="text-xs text-slate-400">
                Dynamic Programming O(n³) on Database Join Trees
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={onDownloadZip}
              disabled={isDownloading}
              className="inline-flex items-center gap-2 px-3 py-1.5 text-xs font-medium rounded-md bg-emerald-600 hover:bg-emerald-500 text-white shadow-sm transition-colors disabled:opacity-50"
              title="Download entire Java Maven project as ZIP ready to open in Eclipse IDE"
            >
              <Download className="w-3.5 h-3.5" />
              <span>{isDownloading ? 'Packaging...' : 'Download Eclipse Project (.ZIP)'}</span>
            </button>
          </div>
        </div>

        <nav className="flex items-center space-x-1 overflow-x-auto py-2 no-scrollbar text-xs border-t border-slate-800/60">
          {navItems.map((item) => {
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md font-medium whitespace-nowrap transition-colors ${
                  isActive
                    ? 'bg-slate-800 text-indigo-300 shadow-sm border border-slate-700/60'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900/60'
                }`}
              >
                {item.icon}
                <span>{item.label}</span>
                {item.badge && (
                  <span className="ml-1 text-[9px] px-1 py-0.2 rounded bg-indigo-500/20 text-indigo-300">
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>
      </div>
    </header>
  );
};
