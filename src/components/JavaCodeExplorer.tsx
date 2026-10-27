import React, { useState } from 'react';
import { JAVA_PROJECT_FILES, ProjectFileEntry } from '../data/javaProjectFiles';
import { 
  FileCode, 
  Copy, 
  Check, 
  Download, 
  FolderTree
} from 'lucide-react';

interface JavaCodeExplorerProps {
  onDownloadZip: () => void;
  isDownloading: boolean;
}

export const JavaCodeExplorer: React.FC<JavaCodeExplorerProps> = ({
  onDownloadZip,
  isDownloading,
}) => {
  const [selectedFile, setSelectedFile] = useState<ProjectFileEntry>(JAVA_PROJECT_FILES[0]);
  const [copied, setCopied] = useState(false);
  const [filterCategory, setFilterCategory] = useState<string>('all');

  const handleCopy = () => {
    navigator.clipboard.writeText(selectedFile.code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const categories = [
    { id: 'all', label: 'All Files' },
    { id: 'algorithm', label: 'Algorithm' },
    { id: 'ui', label: 'Swing UI' },
    { id: 'parser', label: 'SQL Parser' },
    { id: 'database', label: 'MySQL / JDBC' },
    { id: 'test', label: 'Unit Tests' },
    { id: 'config', label: 'Config & Build' },
    { id: 'doc', label: 'Docs' },
  ];

  const filteredFiles = filterCategory === 'all' 
    ? JAVA_PROJECT_FILES 
    : JAVA_PROJECT_FILES.filter((f) => f.category === filterCategory);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-semibold text-white tracking-tight">
            Java 17 & Eclipse IDE Codebase Explorer
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Complete Java desktop application sources with Swing GUI, Dynamic Programming algorithms, JDBC, and JUnit 5.
          </p>
        </div>

        <button
          onClick={onDownloadZip}
          disabled={isDownloading}
          className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white shadow-sm transition-colors disabled:opacity-50 cursor-pointer"
        >
          <Download className="w-4 h-4" />
          <span>{isDownloading ? 'Zipping Project...' : 'Download Complete Eclipse Project (.ZIP)'}</span>
        </button>
      </div>

      <div className="flex items-center gap-1 overflow-x-auto pb-1 text-xs">
        {categories.map((cat) => (
          <button
            key={cat.id}
            onClick={() => setFilterCategory(cat.id)}
            className={`px-3 py-1.5 rounded-md font-medium whitespace-nowrap transition-colors cursor-pointer ${
              filterCategory === cat.id
                ? 'bg-indigo-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200 bg-slate-900 border border-slate-800'
            }`}
          >
            {cat.label}
          </button>
        ))}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-4">
        <div className="md:col-span-4 rounded-xl border border-slate-800 bg-slate-900/60 p-3 space-y-1.5 max-h-[580px] overflow-y-auto">
          <div className="px-2 py-1 text-[11px] font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
            <FolderTree className="w-3.5 h-3.5 text-indigo-400" />
            <span>Project Files ({filteredFiles.length})</span>
          </div>

          {filteredFiles.map((f) => {
            const isSelected = selectedFile.path === f.path;
            return (
              <button
                key={f.path}
                onClick={() => setSelectedFile(f)}
                className={`w-full text-left p-2.5 rounded-lg text-xs transition-colors flex items-start gap-2.5 cursor-pointer ${
                  isSelected
                    ? 'bg-indigo-950/70 border border-indigo-700/60 text-white font-medium'
                    : 'text-slate-300 hover:bg-slate-800/60 border border-transparent'
                }`}
              >
                <FileCode className={`w-4 h-4 shrink-0 mt-0.5 ${isSelected ? 'text-indigo-400' : 'text-slate-500'}`} />
                <div className="truncate">
                  <div className="truncate font-mono">{f.name}</div>
                  <div className="text-[10px] text-slate-500 truncate font-sans">{f.description}</div>
                </div>
              </button>
            );
          })}
        </div>

        <div className="md:col-span-8 rounded-xl border border-slate-800 bg-slate-950 overflow-hidden shadow-inner flex flex-col">
          <div className="flex items-center justify-between px-4 py-2.5 bg-slate-900/90 border-b border-slate-800 text-xs">
            <div className="flex items-center gap-2">
              <span className="font-mono text-emerald-400 font-semibold">{selectedFile.path}</span>
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 uppercase font-mono">
                {selectedFile.language}
              </span>
            </div>

            <button
              onClick={handleCopy}
              className="inline-flex items-center gap-1.5 px-2.5 py-1 text-xs rounded bg-slate-800 hover:bg-slate-700 text-slate-200 transition-colors cursor-pointer"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copied ? 'Copied!' : 'Copy Code'}</span>
            </button>
          </div>

          <div className="px-4 py-2 bg-slate-900/40 border-b border-slate-800/60 text-xs text-slate-400">
            {selectedFile.description}
          </div>

          <div className="p-4 overflow-x-auto font-mono text-xs text-slate-200 max-h-[480px] overflow-y-auto leading-relaxed">
            <pre className="whitespace-pre">
              <code>{selectedFile.code}</code>
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
};
