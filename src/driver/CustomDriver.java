package driver;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class CustomDriver implements Driver {
    public CustomDriver() {
    }


    public Connection connect(String url) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }

        // Extract any needed information from the URL (e.g., database name)
        String dbName = parseDatabaseName(url);

        // Use the information to create a new CustomConnection
        return new CustomConnection(dbName);
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }

        // Extract any needed information from the URL (e.g., database name)
        String dbName = parseDatabaseName(url);

        // Use the information to create a new CustomConnection
        return new CustomConnection(dbName);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        // Define a specific URL prefix for your driver
        return url.startsWith("jdbc:customdb:");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0]; // Optional: Add properties if needed
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null; // Optional: Configure logging if needed
    }

    private String parseDatabaseName(String url) {
        // Example: jdbc:customdb:mydatabase -> extract "mydatabase"
        return url.substring("jdbc:customdb:".length());
    }
}
