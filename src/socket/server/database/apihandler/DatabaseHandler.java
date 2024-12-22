package socket.server.database.apihandler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import socket.server.HttpServerManager;
import socket.server.Server;
import socket.server.database.SgbdServer;
import socket.server.http.handler.ContentHandler;
import socket.server.protocols.HttpRequest;
import socket.server.protocols.HttpResponse;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Objects;

public class DatabaseHandler implements ContentHandler {
    @Override
    public void handle(Server server, String webRoot, HttpRequest request, HttpResponse response, HttpServerManager manager) throws Exception {
        String requestUri = request.getUri();
        String body = request.getBody();

        manager.log("Trying to handle API: " + requestUri + " body: " + body);

        if (!Objects.equals(request.getMethod(), "POST")) {
            response.sendError(403, "Method not allowed");
            return;
        }

        // Parse the JSON body to extract the query and API key
        String query;
        String apiKey;
        try {
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            query = jsonObject.get("query").getAsString();
            apiKey = jsonObject.get("api_key").getAsString();
            System.out.println("query:"+query);
            System.out.println("apiKey:"+apiKey);
        } catch (Exception e) {
            manager.log("Failed to parse JSON body: " + body);
            response.sendError(400, "Invalid request format");
            return;
        }

        // Verify API key

        if (!isValidApiKey(apiKey, manager)) {
            manager.log("Invalid API Key attempt: " + apiKey);
            response.sendError(401, "Unauthorized: Invalid API Key");
            return;
        }

        SgbdServer sgbdServer = (SgbdServer) server;
        Connection connection = sgbdServer.connection;

        Statement statement = null;
        ResultSet resultSet = null;
        try {
            // Validate and sanitize query if necessary
            if (!isQueryAllowed(query)) {
                manager.log("Blocked unsafe query: " + query);
                response.sendError(400, "Invalid or unsafe query");
                return;
            }

            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            // Convert result set to JSON
            String jsonContent = convertResultSetToJson(resultSet);
            System.out.println(jsonContent);
            response.sendResponse(200, "OK", "application/json", jsonContent.getBytes());

        } catch (Exception e) {
            manager.log("Exception while executing query: " + e.getMessage());
            response.sendError(500, "Internal Server Error");
        } finally {
            // Ensure resources are closed
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (Exception e) {
                manager.log("Error closing resources: " + e.getMessage());
            }
        }
    }
    private boolean isValidApiKey(String providedKey, HttpServerManager manager) {
        // Get the current API key from the manager and compare
        return providedKey != null && providedKey.equals(manager.getApiKey());
    }

    public static String convertResultSetToJson(ResultSet resultSet) throws Exception {
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonArray();

        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            JsonObject jsonObject = new JsonObject();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object columnValue = resultSet.getObject(i);

                if (columnValue == null) {
                    jsonObject.add(columnName, null);
                } else {
                    jsonObject.addProperty(columnName, columnValue.toString());
                }
            }

            jsonArray.add(jsonObject);
        }

        return gson.toJson(jsonArray);
    }

    private boolean isQueryAllowed(String query) {
        // Example validation: allow only SELECT queries
        return query != null && query.trim().toUpperCase().startsWith("SELECT ");
    }
}
