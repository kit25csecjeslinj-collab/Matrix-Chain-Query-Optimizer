import React, { useState } from 'react';
import { DatabaseTable } from '../types';
import { Plus, Trash2, Edit2, RotateCcw, Check, X, Sparkles } from 'lucide-react';

interface TableCatalogProps {
  tables: DatabaseTable[];
  onAddTable: (table: Omit<DatabaseTable, 'id'>) => void;
  onUpdateTable: (table: DatabaseTable) => void;
  onDeleteTable: (id: number) => void;
  onResetDefault: () => void;
  onLoadTextbook: () => void;
}

export const TableCatalog: React.FC<TableCatalogProps> = ({
  tables,
  onAddTable,
  onUpdateTable,
  onDeleteTable,
  onResetDefault,
  onLoadTextbook,
}) => {
  const [editingId, setEditingId] = useState<number | null>(null);
  const [isAdding, setIsAdding] = useState(false);

  const [name, setName] = useState('');
  const [alias, setAlias] = useState('');
  const [rowCount, setRowCount] = useState<number>(10000);
  const [attributeCount, setAttributeCount] = useState<number>(10);
  const [selectivity, setSelectivity] = useState<number>(0.05);
  const [primaryJoinKey, setPrimaryJoinKey] = useState('id');
  const [description, setDescription] = useState('');
  const [error, setError] = useState<string | null>(null);

  const startEdit = (t: DatabaseTable) => {
    setEditingId(t.id);
    setName(t.name);
    setAlias(t.alias || '');
    setRowCount(t.rowCount);
    setAttributeCount(t.attributeCount);
    setSelectivity(t.selectivity);
    setPrimaryJoinKey(t.primaryJoinKey);
    setDescription(t.description);
    setError(null);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setIsAdding(false);
    setError(null);
  };

  const validate = () => {
    if (!name.trim()) return 'Table name is required.';
    if (!/^[a-zA-Z_][a-zA-Z0-9_]*$/.test(name.trim())) {
      return 'Table name must start with a letter and contain only alphanumeric characters.';
    }
    if (rowCount <= 0 || !Number.isInteger(Number(rowCount))) {
      return 'Row count (tuples) must be a positive integer.';
    }
    if (attributeCount <= 0 || !Number.isInteger(Number(attributeCount))) {
      return 'Attribute count (cols) must be a positive integer.';
    }
    if (selectivity <= 0 || selectivity > 1) {
      return 'Selectivity estimate must be in (0.0, 1.0].';
    }
    return null;
  };

  const handleSave = () => {
    const err = validate();
    if (err) {
      setError(err);
      return;
    }

    if (isAdding) {
      onAddTable({
        name: name.trim(),
        alias: alias.trim() || undefined,
        rowCount: Number(rowCount),
        attributeCount: Number(attributeCount),
        selectivity: Number(selectivity),
        primaryJoinKey: primaryJoinKey.trim(),
        description: description.trim(),
      });
      setIsAdding(false);
    } else if (editingId !== null) {
      onUpdateTable({
        id: editingId,
        name: name.trim(),
        alias: alias.trim() || undefined,
        rowCount: Number(rowCount),
        attributeCount: Number(attributeCount),
        selectivity: Number(selectivity),
        primaryJoinKey: primaryJoinKey.trim(),
        description: description.trim(),
      });
      setEditingId(null);
    }
    setError(null);
  };

  const openAddModal = () => {
    setEditingId(null);
    setName('');
    setAlias('');
    setRowCount(25000);
    setAttributeCount(8);
    setSelectivity(0.05);
    setPrimaryJoinKey('id');
    setDescription('');
    setIsAdding(true);
    setError(null);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-semibold text-white tracking-tight">
            Database Table & Matrix Catalog
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Configure database tables, cardinalities, column projection dimensions, and selectivity factors.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={openAddModal}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-md bg-indigo-600 hover:bg-indigo-500 text-white transition-colors cursor-pointer"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add Table</span>
          </button>
          <button
            onClick={onLoadTextbook}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-md bg-emerald-950 text-emerald-300 border border-emerald-800/80 hover:bg-emerald-900 transition-colors cursor-pointer"
            title="Load 10x30, 30x5, 5x60 benchmark"
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>Load 3-Matrix Benchmark</span>
          </button>
          <button
            onClick={onResetDefault}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-md bg-slate-800 text-slate-300 border border-slate-700 hover:bg-slate-700 transition-colors cursor-pointer"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>Reset Demo (5 Tables)</span>
          </button>
        </div>
      </div>

      {(isAdding || editingId !== null) && (
        <div className="p-4 rounded-xl border border-indigo-500/40 bg-slate-900/90 shadow-lg space-y-4">
          <div className="flex items-center justify-between border-b border-slate-800 pb-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-indigo-400">
              {isAdding ? 'Add New Database Table' : 'Edit Database Table'}
            </span>
            <button onClick={cancelEdit} className="text-slate-400 hover:text-slate-200">
              <X className="w-4 h-4" />
            </button>
          </div>

          {error && (
            <div className="text-xs text-rose-400 bg-rose-950/40 border border-rose-800/50 p-2.5 rounded-lg">
              {error}
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3 text-xs">
            <div>
              <label className="block text-slate-300 font-medium mb-1">Table Name *</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g. Invoices"
                className="w-full bg-slate-950 border border-slate-700 rounded-md px-2.5 py-1.5 text-white focus:outline-none focus:border-indigo-500"
              />
            </div>
            <div>
              <label className="block text-slate-300 font-medium mb-1">Alias (Optional)</label>
              <input
                type="text"
                value={alias}
                onChange={(e) => setAlias(e.target.value)}
                placeholder="e.g. V"
                className="w-full bg-slate-950 border border-slate-700 rounded-md px-2.5 py-1.5 text-white focus:outline-none focus:border-indigo-500"
              />
            </div>
            <div>
              <label className="block text-slate-300 font-medium mb-1">Tuples / Row Count *</label>
              <input
                type="number"
                min="1"
                value={rowCount}
                onChange={(e) => setRowCount(Number(e.target.value))}
                className="w-full bg-slate-950 border border-slate-700 rounded-md px-2.5 py-1.5 text-white focus:outline-none focus:border-indigo-500"
              />
            </div>
            <div>
              <label className="block text-slate-300 font-medium mb-1">Attributes / Cols *</label>
              <input
                type="number"
                min="1"
                value={attributeCount}
                onChange={(e) => setAttributeCount(Number(e.target.value))}
                className="w-full bg-slate-950 border border-slate-700 rounded-md px-2.5 py-1.5 text-white focus:outline-none focus:border-indigo-500"
              />
            </div>
            <div>
              <label className="block text-slate-300 font-medium mb-1">Selectivity Factor (0.0-1.0]</label>
              <input
                type="number"
                step="0.01"
                min="0.001"
                max="1.0"
                value={selectivity}
                onChange={(e) => setSelectivity(Number(e.target.value))}
                className="w-full bg-slate-950 border border-slate-700 rounded-md px-2.5 py-1.5 text-white focus:outline-none focus:border-indigo-500"
              />
            </div>
            <div>
              <label className="block text-slate-300 font-medium mb-1">Primary Join Key</label>
              <input
                type="text"
                value={primaryJoinKey}
                onChange={(e) => setPrimaryJoinKey(e.target.value)}
                placeholder="e.g. order_id"
                className="w-full bg-slate-950 border border-slate-700 rounded-md px-2.5 py-1.5 text-white focus:outline-none focus:border-indigo-500"
              />
            </div>
            <div className="sm:col-span-2">
              <label className="block text-slate-300 font-medium mb-1">Description</label>
              <input
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Brief table semantics"
                className="w-full bg-slate-950 border border-slate-700 rounded-md px-2.5 py-1.5 text-white focus:outline-none focus:border-indigo-500"
              />
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-2 border-t border-slate-800">
            <button
              onClick={cancelEdit}
              className="px-3 py-1.5 text-xs rounded-md bg-slate-800 text-slate-300 hover:bg-slate-700 cursor-pointer"
            >
              Cancel
            </button>
            <button
              onClick={handleSave}
              className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-semibold rounded-md bg-emerald-600 hover:bg-emerald-500 text-white cursor-pointer"
            >
              <Check className="w-3.5 h-3.5" />
              <span>Save Table</span>
            </button>
          </div>
        </div>
      )}

      <div className="rounded-xl border border-slate-800 bg-slate-900/40 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900 text-slate-400 font-semibold uppercase tracking-wider border-b border-slate-800">
              <tr>
                <th className="py-3 px-4">#</th>
                <th className="py-3 px-4">Table Name</th>
                <th className="py-3 px-4">Cardinality (Rows)</th>
                <th className="py-3 px-4">Columns (Attributes)</th>
                <th className="py-3 px-4">Selectivity</th>
                <th className="py-3 px-4">Join Key</th>
                <th className="py-3 px-4">Description</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {tables.map((t, idx) => (
                <tr key={t.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="py-3 px-4 text-slate-500">{idx + 1}</td>
                  <td className="py-3 px-4 font-bold text-white font-sans">
                    {t.name} {t.alias && <span className="text-slate-400 font-normal">({t.alias})</span>}
                  </td>
                  <td className="py-3 px-4 text-sky-400">{t.rowCount.toLocaleString()}</td>
                  <td className="py-3 px-4 text-indigo-400">{t.attributeCount} cols</td>
                  <td className="py-3 px-4 text-amber-400">{(t.selectivity * 100).toFixed(1)}%</td>
                  <td className="py-3 px-4 text-slate-300">{t.primaryJoinKey || '—'}</td>
                  <td className="py-3 px-4 text-slate-400 font-sans max-w-xs truncate">{t.description || '—'}</td>
                  <td className="py-3 px-4 text-right space-x-1 font-sans">
                    <button
                      onClick={() => startEdit(t)}
                      className="p-1.5 text-slate-400 hover:text-sky-300 rounded hover:bg-slate-800 cursor-pointer"
                      title="Edit table"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => onDeleteTable(t.id)}
                      className="p-1.5 text-slate-400 hover:text-rose-400 rounded hover:bg-slate-800 cursor-pointer"
                      title="Delete table"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
