package driver;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class CustomResultSetMetaData implements ResultSetMetaData {

    private final String[] columnNames; // Array of column names
    private final int columnCount;

    // Constructor to initialize metadata from the result set
    public CustomResultSetMetaData(String[] columnNames) {
        this.columnNames = columnNames;
        this.columnCount = columnNames.length;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return columnCount;
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return false;
    }

    @Override
    public int isNullable(int column) throws SQLException {
        return 0;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return false;
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        return 0;
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        if (column < 1 || column > columnCount) {
            throw new SQLException("Column index out of bounds.");
        }
        return columnNames[column - 1];
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        if (column < 1 || column > columnCount) {
            throw new SQLException("Column index out of bounds.");
        }
        return columnNames[column - 1];  // 1-based index, so subtract 1
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        return "";
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        return 0;
    }

    @Override
    public int getScale(int column) throws SQLException {
        return 0;
    }

    @Override
    public String getTableName(int column) throws SQLException {
        return "";
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        return "";
    }


    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return false;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return "";
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return  0;
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return "";
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    // Implement other methods from ResultSetMetaData as needed
}
