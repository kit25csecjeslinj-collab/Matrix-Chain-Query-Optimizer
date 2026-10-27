export interface ParsedTableRef {
  tableName: string;
  alias: string;
}

export interface SQLParseResult {
  success: boolean;
  errorMessage?: string;
  tables: ParsedTableRef[];
  joinConditions: string[];
}

export function parseSQLQuery(sql: string): SQLParseResult {
  if (!sql || !sql.trim()) {
    return {
      success: false,
      errorMessage: 'SQL query text cannot be empty.',
      tables: [],
      joinConditions: [],
    };
  }

  // Normalize query
  const clean = sql
    .replace(/--.*$/gm, '')
    .replace(/\/\*[\s\S]*?\*\//g, '')
    .replace(/\s+/g, ' ')
    .trim();

  if (!clean.toUpperCase().startsWith('SELECT')) {
    return {
      success: false,
      errorMessage: 'Unsupported SQL syntax: query must start with a SELECT clause.',
      tables: [],
      joinConditions: [],
    };
  }

  const fromMatch = clean.match(/\bFROM\s+([A-Za-z0-9_]+)(?:\s+(?:AS\s+)?([A-Za-z0-9_]+))?/i);
  if (!fromMatch) {
    return {
      success: false,
      errorMessage: 'Syntax error: missing or invalid FROM clause.',
      tables: [],
      joinConditions: [],
    };
  }

  const tables: ParsedTableRef[] = [];
  const joinConditions: string[] = [];

  const isKeyword = (w: string) => {
    const u = w.toUpperCase();
    return ['JOIN', 'INNER', 'LEFT', 'RIGHT', 'ON', 'WHERE', 'GROUP', 'ORDER'].includes(u);
  };

  const primaryTable = fromMatch[1];
  let primaryAlias = fromMatch[2] || '';
  if (primaryAlias && isKeyword(primaryAlias)) primaryAlias = '';
  tables.push({ tableName: primaryTable, alias: primaryAlias });

  // Match JOINs
  const joinRegex = /\b(?:INNER\s+)?JOIN\s+([A-Za-z0-9_]+)(?:\s+(?:AS\s+)?([A-Za-z0-9_]+))?\s+ON\s+([^;\bJOIN\b]+)/gi;
  let match: RegExpExecArray | null;

  while ((match = joinRegex.exec(clean)) !== null) {
    const tbl = match[1];
    let als = match[2] || '';
    if (als && isKeyword(als)) als = '';
    const cond = match[3].trim();

    tables.push({ tableName: tbl, alias: als });
    joinConditions.push(cond);
  }

  if (tables.length < 2) {
    return {
      success: false,
      errorMessage: `Matrix Chain Query Optimization requires at least 2 joinable tables. Found: ${tables.length}.`,
      tables,
      joinConditions,
    };
  }

  return {
    success: true,
    tables,
    joinConditions,
  };
}
