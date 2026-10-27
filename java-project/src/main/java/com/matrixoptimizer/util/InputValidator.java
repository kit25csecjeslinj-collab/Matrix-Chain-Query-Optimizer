package com.matrixoptimizer.util;

import com.matrixoptimizer.model.DatabaseTable;
import java.util.List;

/**
 * Validates user inputs for table dimensions, SQL clauses, and matrix compatibility.
 */
public class InputValidator {

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }

    public static ValidationResult validateTable(String name, String rowStr, String attrStr, String selStr) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Table name cannot be empty.");
        }
        if (!name.trim().matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return new ValidationResult(false, "Table name must start with a letter or underscore and contain only alphanumeric characters.");
        }

        long rows;
        try {
            rows = Long.parseLong(rowStr.trim());
            if (rows <= 0) return new ValidationResult(false, "Row count must be a positive integer (> 0).");
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Invalid row count: please enter a valid positive number.");
        }

        int attrs;
        try {
            attrs = Integer.parseInt(attrStr.trim());
            if (attrs <= 0) return new ValidationResult(false, "Attribute count must be a positive integer (> 0).");
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Invalid attribute count: please enter a valid positive integer.");
        }

        if (selStr != null && !selStr.trim().isEmpty()) {
            try {
                double s = Double.parseDouble(selStr.trim());
                if (s <= 0.0 || s > 1.0) {
                    return new ValidationResult(false, "Selectivity estimate must be in the open interval (0.0, 1.0].");
                }
            } catch (NumberFormatException e) {
                return new ValidationResult(false, "Invalid selectivity value: please enter a decimal between 0.0 and 1.0.");
            }
        }

        return new ValidationResult(true, "Validation successful.");
    }

    public static ValidationResult validateMatrixCompatibility(int[] p) {
        if (p == null || p.length < 2) {
            return new ValidationResult(false, "Chain must have at least 1 matrix (2 dimensions in array p).");
        }
        for (int i = 0; i < p.length; i++) {
            if (p[i] <= 0) {
                return new ValidationResult(false, "Dimension p[" + i + "] must be positive, found: " + p[i]);
            }
        }
        return new ValidationResult(true, "Compatible dimensions.");
    }

    public static ValidationResult validateQueryTables(List<String> queryTables, List<DatabaseTable> configuredTables) {
        if (queryTables.size() < 2) {
            return new ValidationResult(false, "Optimization requires at least 2 tables in join sequence.");
        }
        for (String qName : queryTables) {
            boolean found = configuredTables.stream()
                    .anyMatch(t -> t.getName().equalsIgnoreCase(qName));
            if (!found) {
                return new ValidationResult(false, "Table '" + qName + "' referenced in SQL query is not configured in database catalog.");
            }
        }
        return new ValidationResult(true, "All query tables verified in catalog.");
    }
}
