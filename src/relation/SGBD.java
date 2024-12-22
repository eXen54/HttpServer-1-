package relation;

import database.CustomDatabase;
import driver.CustomResultSet;
import driver.CustomResultSetMetaData;
import relation.condition.Condition;
import relation.condition.ConditionValued;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SGBD {

    public CustomDatabase currentCustomDatabase;
    private Map<String, Relation> relations;

    public SGBD(CustomDatabase customDatabase) throws SQLException {
        this.currentCustomDatabase = customDatabase;
    }


    public void listen() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the SGBD Command Interface!");
        System.out.println("Use SQL-like commands or type 'exit' to quit.");

        while (true) {
            System.out.print("SGBD> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting SGBD. Goodbye!");
                break;
            }

            try {
                handleCommand(input);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }
    public void listen(String query) {
            try {
                query = query.trim();
                handleCommand(query);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
    }
    public void executeUpdate(String query) {
            try {
                query = query.trim();
                handleCommand(query);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
    }

    public ResultSet listenSelect(String query) {
        String[] parts = query.split("\\s+");
        String command = parts[0].toUpperCase();

        if (command.equals("SELECT")) {
            if (currentCustomDatabase != null) {
                if (parts.length >= 4 && parts[0].equalsIgnoreCase("SELECT") && parts[2].equalsIgnoreCase("FROM")) {
                    String[] columnNames = parts[1].split(",");  // Split column names by commas
                    for (int i = 0; i < columnNames.length; i++) {
                        columnNames[i] = columnNames[i].trim();  // Trim spaces around column names
                    }

                    String relationName = parts[3];  // The relation name after "FROM"
                    Relation selectedRelation = currentCustomDatabase.getRelationByName(relationName);

                    if (selectedRelation != null) {
                        try {
                            if (columnNames.length == 1 && columnNames[0].equals("*")) {
                                // SELECT * case - return all columns in the relation
                                return createResultSet(selectedRelation);
                            } else {
                                // Handle specific columns case
                                Relation subRelation = Relation.getSubRelation(selectedRelation, columnNames);
                                return createResultSet(subRelation);
                            }
                        } catch (IllegalArgumentException e) {
                            System.out.println("Error: " + e.getMessage());  // Handle invalid column names
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        System.out.println("Error: Relation " + relationName + " not found.");
                    }
                } else {
                    System.out.println("Error: Invalid SELECT syntax.");
                }
            } else {
                System.out.println("Error: No database loaded.");
            }
        }

        return null;  // Return null if no valid SELECT statement is found
    }

    private ResultSet createResultSet(Relation relation) throws SQLException {
        // Extract column names from the Relation
        String[] columnNames = relation.getAttributes().stream()
                .map(Attribute::getAttributeName)
                .toArray(String[]::new);

        // Create metadata for the ResultSet using only column names
        CustomResultSetMetaData metaData = new CustomResultSetMetaData(columnNames);

        // Prepare the data for the CustomResultSet
        List<String[]> data = new ArrayList<>();
        for (Row row : relation.getRows()) {
            String[] rowData = row.getValues().stream()
                    .map(Object::toString)
                    .toArray(String[]::new);
            data.add(rowData);
        }

        // Create and populate the CustomResultSet
        CustomResultSet resultSet = new CustomResultSet(metaData);
        resultSet.setData(data);

        return resultSet;
    }



    private ConditionValued[] parseConditions(String whereClause) {
        // Use a regex matcher to extract logical operators (AND/OR)
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)\\b(AND|OR)\\b").matcher(whereClause);
        List<String> logicalOperators = new ArrayList<>();
        while (matcher.find()) {
            logicalOperators.add(matcher.group().toLowerCase());
        }

        // Split by conditions
        String[] conditionParts = whereClause.split("(?i)\\bAND\\b|\\bOR\\b");
        ConditionValued[] conditions = new ConditionValued[conditionParts.length];

        for (int i = 0; i < conditionParts.length; i++) {
            String condition = conditionParts[i].trim();
            System.out.println("condition" + condition);

            // Parse condition into tokens
            String[] tokens = null;
            Condition conditionType = null;

            if (condition.contains(">=")) {
                tokens = condition.split(">=", 2);
                conditionType = Condition.mapOperatorToCondition(">=");
            } else if (condition.contains("<=")) {
                tokens = condition.split("<=", 2);
                conditionType = Condition.mapOperatorToCondition("<=");
            } else if (condition.contains("=")) {
                tokens = condition.split("=", 2);
                conditionType = Condition.mapOperatorToCondition("=");
            } else if (condition.contains("!=")) {
                tokens = condition.split("!=", 2);
                conditionType = Condition.mapOperatorToCondition("!=");
            } else if (condition.contains(">")) {
                tokens = condition.split(">", 2);
                conditionType = Condition.mapOperatorToCondition(">");
            } else if (condition.contains("<")) {
                tokens = condition.split("<", 2);
                conditionType = Condition.mapOperatorToCondition("<");
            } else if (condition.contains("like")) {
                tokens = condition.split("(?i)like", 2); // Case-insensitive LIKE
                conditionType = Condition.LIKE;
            } else {
                throw new IllegalArgumentException("Invalid condition format: " + condition);
            }

            // Validate tokens and create ConditionValued
            if (tokens.length == 2) {
                String column = tokens[0].trim();
                String value = tokens[1].trim().replace("'", ""); // Remove quotes around values

                // Assign logical operator (default "and" for first condition)
                String logicalOperator = (i == 0) ? "and" : logicalOperators.get(i - 1);

                // Create and add ConditionValued
                System.out.println("Condition: " + column + " " + conditionType + " " + value + ", type: " + logicalOperator);
                conditions[i] = new ConditionValued(column, conditionType, value, logicalOperator);
            } else {
                throw new IllegalArgumentException("Invalid condition format: " + condition);
            }
        }

        return conditions;
    }

    private void handleCommand(String input) {
        String[] parts = input.split("\\s+");
        String command = parts[0].toUpperCase();

        switch (command) {
            case "SELECT":
                if (currentCustomDatabase != null) {
                    if (parts.length >= 4 && parts[0].equalsIgnoreCase("SELECT") && parts[2].equalsIgnoreCase("FROM")) {
                        String[] columnNames = parts[1].split(","); // Split column names by commas
                        for (int i = 0; i < columnNames.length; i++) {
                            columnNames[i] = columnNames[i].trim(); // Trim spaces around column names
                        }

                        String relationName = parts[3]; // The relation name after "FROM"
                        Relation selectedRelation = currentCustomDatabase.getRelationByName(relationName);

                        if (selectedRelation != null) {
                            try {
                                // Check if a WHERE clause exists
                                ConditionValued[] conditions = null;

                                if (input.toUpperCase().contains("WHERE")) {
                                    String whereClause = input.substring(input.toUpperCase().indexOf("WHERE") + 5).trim();
                                    conditions = parseConditions(whereClause);
                                    // Parse WHERE conditions
                                }


                                Relation resultRelation;
                                if (columnNames.length == 1 && columnNames[0].equals("*")) {
                                    // SELECT * case
                                    resultRelation = selectedRelation;
                                } else {
                                    // Handle specific columns case
                                    resultRelation = Relation.getSubRelation(selectedRelation, columnNames);
                                }

                                // Apply conditions if present
                                if (conditions != null) {
                                    resultRelation = Relation.filtered(resultRelation, conditions);
                                }

                                // Display and save the result
                                resultRelation.afficheRow();
                            } catch (IllegalArgumentException e) {
                                System.out.println("Error: " + e.getMessage()); // Handle invalid column names
                            }
                        } else {
                            System.out.println("Error: Relation " + relationName + " not found.");
                        }
                    } else {
                        System.out.println("Error: Invalid SELECT syntax.");
                    }
                } else {
                    System.out.println("Error: No database loaded.");
                }
                break;

            case "INSERT":
                System.out.println("gang shit");
                for (int i = 0; i < parts.length; i++) {
                    System.out.println(parts[i].toString());
                }
                if (parts.length >= 4 && parts[1].equalsIgnoreCase("INTO")) {
                    String relationName = parts[2];  // Extract relation name
                    Relation selectedRelation = currentCustomDatabase.getRelationByName(relationName);


                    if (selectedRelation != null) {
                        if (parts.length >= 6 && parts[parts.length - 2].equalsIgnoreCase("VALUES")) {
                            // Remove extra whitespace from column and value parts
                            String columnsPart = parts[3].replaceAll("\\s+", "");
                            String valuesPart = parts[5].replaceAll("\\s+", "");

                            System.out.println("values:" + valuesPart);

                            // Extract and validate column names
                            String[] columnNames = columnsPart
                                    .substring(1, columnsPart.length() - 1)  // Remove parentheses
                                    .split(",");
                            for (int i = 0; i < columnNames.length; i++) {
                                columnNames[i] = columnNames[i].trim();
                            }

                            Object[] values = new Object[valuesPart
                                    .substring(1, valuesPart.length() - 1)  // Remove parentheses
                                    .split(",").length];
                            // Extract and validate values
                            String[] valuesString = valuesPart
                                    .substring(1, valuesPart.length() - 1)  // Remove parentheses
                                    .split(",");
                            for (int i = 0; i < values.length; i++) {

                                values[i] = (valuesString[i]).trim();
                                values[i] = stripQuotes(valuesString[i]);  // Remove enclosing quotes if any

                                // Check the type of value and cast accordingly
                                if ((valuesString[i]).equalsIgnoreCase("NULL")) {
                                    values[i] = null;  // Handle NULL explicitly
                                } else if (isInteger(valuesString[i])) {
                                    values[i] = Integer.parseInt(valuesString[i]);  // Cast to Integer
                                } else if (isDouble(valuesString[i])) {
                                    values[i] = Double.parseDouble(valuesString[i]);  // Cast to Double
                                } else {
                                    System.out.println(values[i] + " is a string");
                                }
                            }

                            // Check if columns and values match
                            if (columnNames.length == values.length) {
                                ArrayList<Object> rowValues = new ArrayList<>();
                                for (Object value : values) {
                                    rowValues.add(value);
                                }

                                // Add row to the relation and display updated data
                                selectedRelation.addRow(rowValues.toArray());

                                System.out.println("domain name"+selectedRelation.getAttributes().get(0).getDomain().getDomainName());
                                selectedRelation.afficheRow();
                                currentCustomDatabase.saveDatabase();
                                System.out.println("Row inserted successfully into " + relationName);
                            } else {
                                System.out.println("Error: Number of columns does not match number of values.");
                            }
                        } else {
                            System.out.println("Error: Invalid syntax. Please ensure the VALUES keyword is used and values are correctly specified.");
                        }
                    } else {
                        System.out.println("Error: Relation " + relationName + " not found.");
                    }
                } else {
                    System.out.println("Error: Invalid INSERT syntax.");
                }
                break;

            case "UPDATE":
                // Update command logic (commented out as before)
                break;
            case "COMMIT":
                currentCustomDatabase.saveDatabase();
                // Update command logic (commented out as before)
                break;

            case "DELETE":
                if (parts.length >= 3 && parts[1].equalsIgnoreCase("FROM")) {
                    String relationName = parts[2];  // Extract relation name
                    Relation selectedRelation = currentCustomDatabase.getRelationByName(relationName);

                    if (selectedRelation != null) {
                        if (parts.length == 3) {
                            // Delete all rows if no condition is specified
                            selectedRelation.clearRows();
                            System.out.println("All rows deleted from " + relationName);
                        } else if (parts.length >= 5 && parts[3].equalsIgnoreCase("WHERE")) {
                            // Extract condition
                            String condition = String.join(" ", Arrays.copyOfRange(parts, 4, parts.length));

                            // Parse condition (supports =, !=, <, >, <=, >=)
                            System.out.println(condition);

                            String[] conditionParts = Condition.parseCondition(condition);
                            System.out.println(conditionParts[0]);
                            System.out.println(conditionParts[1]);
                            if (conditionParts != null && conditionParts.length >= 3) {
                                String columnName = conditionParts[0].trim();
                                String operator = conditionParts[1].trim();
                                String value = stripQuotes(conditionParts[2].trim());  // Handle quotes

                                // Map operator to Condition enum
                                Condition conditionEnum = Condition.mapOperatorToCondition(operator);
                                if (conditionEnum == null) {
                                    System.out.println("Error: Unsupported operator '" + operator + "'.");
                                    break;
                                }

                                // Find column index
                                int columnIndex = selectedRelation.getColumnIndex(columnName);
                                if (columnIndex != -1) {
                                    // Use the Relation deleteRows method to handle filtering and deletion
                                    Relation.deleteRows(selectedRelation, columnName, value, conditionEnum);
                                    System.out.println("Rows matching condition deleted from " + relationName);
                                } else {
                                    System.out.println("Error: Column " + columnName + " does not exist in " + relationName);
                                }
                            } else {
                                System.out.println("Error: Invalid WHERE clause. Supported operators: =, !=, <, >, <=, >=.");
                            }
                        } else {
                            System.out.println("Error: Invalid DELETE syntax. Use DELETE FROM table [WHERE condition].");
                        }
                    } else {
                        System.out.println("Error: Relation " + relationName + " not found.");
                    }
                } else {
                    System.out.println("Error: Invalid DELETE syntax.");
                }
                break;

            case "JOIN":
                if (parts.length >= 6 && parts[1].equalsIgnoreCase("ON")) {
                    // Parse command structure: JOIN table1 table2 ON table1.column1 = table2.column2
                    String relation1Name = parts[2];
                    String relation2Name = parts[3];

                    Relation relation1 = currentCustomDatabase.getRelationByName(relation1Name);
                    Relation relation2 = currentCustomDatabase.getRelationByName(relation2Name);

                    if (relation1 == null || relation2 == null) {
                        System.out.println("Error: One or more relations not found.");
                        break;
                    }

                    // Extract and parse the ON condition
                    String onCondition = String.join(" ", Arrays.copyOfRange(parts, 4, parts.length));
                    if (!onCondition.contains("=")) {
                        System.out.println("Error: Invalid ON condition. Use format ON table1.column1 = table2.column2.");
                        break;
                    }

                    String[] conditionParts = onCondition.replace("ON", "").trim().split("=");
                    if (conditionParts.length != 2) {
                        System.out.println("Error: Invalid ON condition. Use format ON table1.column1 = table2.column2.");
                        break;
                    }

                    String[] column1Parts = conditionParts[0].trim().split("\\.");
                    String[] column2Parts = conditionParts[1].trim().split("\\.");

                    if (column1Parts.length != 2 || column2Parts.length != 2) {
                        System.out.println("Error: Invalid column reference in ON condition. Use format table.column.");
                        break;
                    }

                    // Parse column details
                    String table1 = column1Parts[0];
                    String column1 = column1Parts[1];
                    String table2 = column2Parts[0];
                    String column2 = column2Parts[1];

                    // Verify table and column references
                    if (!table1.equalsIgnoreCase(relation1Name) || !table2.equalsIgnoreCase(relation2Name)) {
                        System.out.println("Error: Table names in the ON condition do not match the specified relations.");
                        break;
                    }

                    int columnIndex1 = relation1.getColumnIndex(column1);
                    int columnIndex2 = relation2.getColumnIndex(column2);

                    if (columnIndex1 == -1 || columnIndex2 == -1) {
                        System.out.println("Error: One or more columns do not exist in the specified relations.");
                        break;
                    }

                    // Perform the join
                    Relation resultRelation = Relation.joinCross(relation1, relation2, null, column1, column2);

                    if (resultRelation == null) {
                        System.out.println("Error: Unable to perform the join.");
                    } else {
                        // Display the result (assuming `afficheRow()` prints the rows)
                        resultRelation.afficheRow();
                    }
                } else {
                    System.out.println("Error: Invalid JOIN syntax. Use JOIN table1 table2 ON table1.column1 = table2.column2.");
                }
                break;


            default:
                System.out.println("Unknown command. Use SQL basics like SELECT, UPDATE, DELETE, INSERT, or JOIN.");
        }
    }

    private Condition parseCondition(String operator) {
        switch (operator) {
            case "=":
                return Condition.EQUAL;
            case "!=":
                return Condition.DIFF;
            case "<":
                return Condition.INF;
            case ">":
                return Condition.SUP;
            case "<=":
                return Condition.INF_EQUAL;
            case ">=":
                return Condition.SUP_EQUAL;
            default:
                throw new IllegalArgumentException("Unknown condition operator: " + operator);
        }
    }

    public void setDatabase(CustomDatabase customDatabase) {
        this.currentCustomDatabase = customDatabase;
        System.out.println("Current database set to: " + customDatabase.getNomDatabase());
    }

    private String stripQuotes(String value) {
        if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);  // Remove the first and last character
        }
        return value;
    }
    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Method to check if a string can be parsed as a double
    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
