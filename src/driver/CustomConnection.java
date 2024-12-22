package driver;


import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import database.CustomDatabase;
import helpers.AttributeTypeAdapter;
import helpers.DomainTypeAdapter;
import helpers.RelationTypeAdapter;
import helpers.RowTypeAdapter;
import relation.Attribute;
import relation.Relation;
import relation.Row;
import relation.SGBD;
import relation.domaines.Domain;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class CustomConnection implements Connection {


    // A simple flag to simulate an open or closed connection
    private boolean isOpen = true;
    private final String dbName;
    private final CustomDatabase database;
    private final SGBD sgbd;  // Add SGBD instance here

    public boolean isOpen() {
        return isOpen;
    }

    public String getDbName() {
        return dbName;
    }

    public CustomDatabase getDatabase() {
        return database;
    }

    public SGBD getSgbd() {
        return sgbd;  // Provide getter for SGBD
    }

    // Constructor to initialize the connection with necessary details
    public CustomConnection(String dbName) throws SQLException {
        this.dbName = dbName;
        this.database = loadDatabaseFromFile(dbName);
        this.sgbd = new SGBD(this.database);  // Initialize SGBD instance here
        this.isOpen = true;
    }

    private CustomDatabase loadDatabaseFromFile(String databaseName) {
        String filePath = "src/database/list/" + databaseName + ".json";

        try (FileReader reader = new FileReader(filePath)) {
            // Register the custom adapters for Row, Attribute, and Domain
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Row.class, new RowTypeAdapter())  // Register custom Row adapter
                    .registerTypeAdapter(Attribute.class, new AttributeTypeAdapter())  // Register custom Attribute adapter
                    .registerTypeAdapter(Domain.class, new DomainTypeAdapter())  // Register custom Domain adapter
                    .registerTypeAdapter(Relation.class, new RelationTypeAdapter())  // Register custom Relation adapter
                    .create();

            // Deserialize the JSON into a list of Relation objects
            List<Relation> relations = gson.fromJson(reader, new TypeToken<List<Relation>>() {}.getType());

            // Create a new Database and set the relations
            CustomDatabase database = new CustomDatabase(databaseName);
            database.setRelations(new ArrayList<>(relations)); // Set the relations
            return database;
        } catch (IOException e) {
            System.out.println("Error reading the database file: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
        }

        return null;
    }

    // Example method where you can call listen() from SGBD
    public ResultSet executeQuery(String sql) throws SQLException {
//        if (!isOpen) {
//            throw new SQLException("Connection is closed.");
//        }

        if (sql.trim().toUpperCase().startsWith("SELECT")) {
            return sgbd.listenSelect(sql);  // Call listen() from SGBD
        } else {
            throw new SQLException("Unsupported SQL query type: " + sql);
        }
    }

    // Other methods like createStatement, prepareStatement, etc., remain unchanged
    @Override
    public Statement createStatement() throws SQLException {

        return new CustomStatement(this);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
//        if (!isOpen) {
//            throw new SQLException("Connection is closed.");
//        }
        return null;  // You can implement your own logic for preparing a statement
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return null;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return "";
    }

    @Override
    public void close() throws SQLException {
        if (isOpen) {
            isOpen = false;
            System.out.println("Connection to " + dbName + " closed.");
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        return !isOpen;
    }

    // Other methods (commit, rollback, etc.) remain unchanged

    // Other overridden methods remain unchanged
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
//        if (!isOpen) {
//            throw new SQLException("Connection is closed.");
//        }
        return this.getMetaData();
    }

    // A simple transaction management implementation (optional)
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
//        if (!isOpen) {
//            throw new SQLException("Connection is closed.");
//        }
        // Handle setting auto-commit here (if applicable)
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return true; // Default to true for simplicity (you can handle this based on your design)
    }

    @Override
    public void commit() throws SQLException {
//        if (!isOpen) {
//            throw new SQLException("Connection is closed.");
//        }
        // Commit the transaction (if applicable)
    }

    @Override
    public void rollback() throws SQLException {
//        if (!isOpen) {
//            throw new SQLException("Connection is closed.");
//        }
        // Rollback the transaction (if applicable)
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
//        if (!isOpen) {
//            throw new SQLException("Connection is closed.");
//        }
        // Set connection to read-only mode (if applicable)
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return false; // Default to false (can be extended to support read-only mode)
    }

    // Other required methods (implement as needed for your use case)
    @Override
    public void setCatalog(String catalog) throws SQLException {
//        if (!isOpen) {
//            throw new SQLException("Connection is closed.");
//        }
        // Set the catalog (if applicable)
    }

    @Override
    public String getCatalog() throws SQLException {
        return dbName; // For simplicity, return the database name
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
//        if (!isOpen) {
//            throw new SQLException("Connection is closed.");
//        }
        // Set the transaction isolation level (if applicable)
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return TRANSACTION_READ_COMMITTED; // Default isolation level (can be changed as per your design)
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return Map.of();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

    }

    @Override
    public void setHoldability(int holdability) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return null;
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return null;
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {

    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {

    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return null;
    }

    @Override
    public Clob createClob() throws SQLException {
        return null;
    }

    @Override
    public Blob createBlob() throws SQLException {
        return null;
    }

    @Override
    public NClob createNClob() throws SQLException {
        return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return null;
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return false;
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {

    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {

    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return "";
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return null;
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return null;
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return null;
    }

    @Override
    public void setSchema(String schema) throws SQLException {

    }

    @Override
    public String getSchema() throws SQLException {
        return "";
    }

    @Override
    public void abort(Executor executor) throws SQLException {

    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void beginRequest() throws SQLException {
        Connection.super.beginRequest();
    }

    @Override
    public void endRequest() throws SQLException {
        Connection.super.endRequest();
    }

    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws SQLException {
        return Connection.super.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
    }

    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
        return Connection.super.setShardingKeyIfValid(shardingKey, timeout);
    }

    @Override
    public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey) throws SQLException {
        Connection.super.setShardingKey(shardingKey, superShardingKey);
    }

    @Override
    public void setShardingKey(ShardingKey shardingKey) throws SQLException {
        Connection.super.setShardingKey(shardingKey);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    // Add other required methods, if needed.
}
