package driver;

import java.sql.*;

public class CustomStatement implements Statement {
    private Connection connection;

    public CustomStatement(Connection connection) {
        this.connection = connection;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        String queryType = sql.trim().toUpperCase().split("\\s+")[0]; // Get the first word of the query

        switch (queryType) {
            case "SELECT":
                if (connection instanceof CustomConnection) {
                    CustomConnection customConnection = (CustomConnection) connection;
                    return customConnection.getSgbd().listenSelect(sql); // Call listenSelect() on CustomConnection
                } else {
                    throw new SQLException("Connection is not of type CustomConnection.");
                }

            case "INSERT":
            case "UPDATE":
            case "DELETE":
                if (connection instanceof CustomConnection) {
                    CustomConnection customConnection = (CustomConnection) connection;
                    String cleanQuery = sql.trim().replaceAll("\\s+", " ");
                    System.out.println("Clean query for INSERT/UPDATE/DELETE: " + cleanQuery);
                    customConnection.getSgbd().listen("insert into ETUDIANT1 (FirstName,Name,Age) values ('tsaratsiry','RAVELO',13)"); // Call executeUpdate() on CustomConnection
                    return null; // No ResultSet is returned for INSERT, UPDATE, DELETE
                } else {
                    throw new SQLException("Connection is not of type CustomConnection.");
                }

            default:
                throw new SQLException("Unsupported SQL query type: " + sql);
        }
    }


    private ResultSet executeSelectQuery(String sql) {


        return null;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        // Handle non-SELECT queries (INSERT, UPDATE, DELETE)
        if (sql.trim().toUpperCase().startsWith("INSERT") ||
                sql.trim().toUpperCase().startsWith("UPDATE") ||
                sql.trim().toUpperCase().startsWith("DELETE")) {
            System.out.println("Executing: " + sql);  // For now, just print the query being executed
            return 1;  // Assume one row was affected
        }
        throw new SQLException("Unsupported SQL query type: " + sql);
    }

    // Implement other methods of Statement interface if necessary.
    // For simplicity, many methods are omitted in this example.

    @Override
    public void close() throws SQLException {
        // Close the statement if needed.
        System.out.println("Statement closed.");
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {

    }

    @Override
    public int getMaxRows() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {

    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {

    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {

    }

    @Override
    public void cancel() throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String name) throws SQLException {

    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return 0;
    }

    @Override
    public void addBatch(String sql) throws SQLException {

    }

    @Override
    public void clearBatch() throws SQLException {

    }

    @Override
    public int[] executeBatch() throws SQLException {
        return new int[0];
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {

    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    // Other methods from the Statement interface can be implemented as needed.
}
