package com.matrixoptimizer.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL Parser for extracting tables, aliases, and join conditions
 * from a supported subset of SQL SELECT queries with INNER JOIN / JOIN clauses.
 *
 * Supported syntax:
 * SELECT <cols>
 * FROM Table1 [alias1]
 * [INNER] JOIN Table2 [alias2] ON <condition>
 * [[INNER] JOIN TableN [aliasN] ON <condition>]*
 */
public class SQLQueryParser {

    public static class ParsedTableRef {
        private final String tableName;
        private final String alias;

        public ParsedTableRef(String tableName, String alias) {
            this.tableName = tableName;
            this.alias = alias;
        }

        public String getTableName() { return tableName; }
        public String getAlias() { return alias; }

        @Override
        public String toString() {
            return alias != null && !alias.isEmpty() ? tableName + " AS " + alias : tableName;
        }
    }

    public static class ParseResult {
        private final boolean success;
        private final String errorMessage;
        private final List<ParsedTableRef> tables;
        private final List<String> joinConditions;

        public ParseResult(List<ParsedTableRef> tables, List<String> joinConditions) {
            this.success = true;
            this.errorMessage = null;
            this.tables = tables;
            this.joinConditions = joinConditions;
        }

        public ParseResult(String errorMessage) {
            this.success = false;
            this.errorMessage = errorMessage;
            this.tables = Collections.emptyList();
            this.joinConditions = Collections.emptyList();
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public List<ParsedTableRef> getTables() { return tables; }
        public List<String> getJoinConditions() { return joinConditions; }
    }

    /**
     * Parses a SQL SELECT query to extract referenced tables in join order.
     */
    public static ParseResult parse(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return new ParseResult("SQL query cannot be empty.");
        }

        String cleanedSql = sql.replaceAll("--.*", "") // remove single line comments
                               .replaceAll("/\\*.*?\\*/", "") // remove block comments
                               .replaceAll("\\s+", " ") // normalize whitespace
                               .trim();

        if (!cleanedSql.toUpperCase().startsWith("SELECT")) {
            return new ParseResult("Unsupported query: query must begin with a SELECT statement.");
        }

        // Locate FROM keyword
        Pattern fromPattern = Pattern.compile("\\bFROM\\s+([A-Za-z0-9_]+)(?:\\s+(?:AS\\s+)?([A-Za-z0-9_]+))?", Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(cleanedSql);

        if (!fromMatcher.find()) {
            return new ParseResult("Syntax error: missing or invalid FROM clause.");
        }

        List<ParsedTableRef> tables = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        String primaryTable = fromMatcher.group(1);
        String primaryAlias = fromMatcher.group(2);

        // Disallow SQL keywords as table alias
        if (primaryAlias != null && isKeyword(primaryAlias)) {
            primaryAlias = "";
        }
        tables.add(new ParsedTableRef(primaryTable, primaryAlias != null ? primaryAlias : ""));

        // Match JOIN clauses
        Pattern joinPattern = Pattern.compile("\\b(?:INNER\\s+)?JOIN\\s+([A-Za-z0-9_]+)(?:\\s+(?:AS\\s+)?([A-Za-z0-9_]+))?\\s+ON\\s+([^;\\bJOIN\\b]+)", Pattern.CASE_INSENSITIVE);
        Matcher joinMatcher = joinPattern.matcher(cleanedSql);

        while (joinMatcher.find()) {
            String joinTable = joinMatcher.group(1);
            String joinAlias = joinMatcher.group(2);
            if (joinAlias != null && isKeyword(joinAlias)) {
                joinAlias = "";
            }
            String condition = joinMatcher.group(3).trim();
            tables.add(new ParsedTableRef(joinTable, joinAlias != null ? joinAlias : ""));
            conditions.add(condition);
        }

        if (tables.size() < 2) {
            return new ParseResult("Optimization requires at least 2 joinable tables in FROM and JOIN clauses. Found: " + tables.size());
        }

        return new ParseResult(tables, conditions);
    }

    private static boolean isKeyword(String word) {
        String upper = word.toUpperCase();
        return upper.equals("JOIN") || upper.equals("INNER") || upper.equals("LEFT") || 
               upper.equals("RIGHT") || upper.equals("ON") || upper.equals("WHERE") ||
               upper.equals("GROUP") || upper.equals("ORDER");
    }
}
