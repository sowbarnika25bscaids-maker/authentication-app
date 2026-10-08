package app;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class RegisterHandler implements HttpHandler {
    private static final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Handle CORS preflight
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            return;
        }

        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            if (json == null) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid JSON\"}");
                return;
            }

            String name = getString(json, "name");
            String phone = getString(json, "phone");
            String email = getString(json, "email");
            String password = getString(json, "password");

            // Server-side validation
            if (name == null || name.isEmpty() ||
                    phone == null || phone.isEmpty() ||
                    email == null || email.isEmpty() || !email.contains("@") ||
                    password == null || password.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Missing or invalid fields\"}");
                return;
            }

            // Hash the password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

            // Store in DB
            try (Connection conn = Database.getConnection()) {
                String sql = "INSERT INTO users (name, phone, email, password) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, phone);
                    stmt.setString(3, email);
                    stmt.setString(4, hashedPassword);
                    stmt.executeUpdate();
                }

                sendResponse(exchange, 201, "{\"message\": \"Registration successful!\"}");
            } catch (SQLIntegrityConstraintViolationException e) {
                sendResponse(exchange, 409, "{\"error\": \"Email is already registered.\"}");
            } catch (SQLException e) {
                e.printStackTrace(); // Log internally
                sendResponse(exchange, 500, "{\"error\": \"Database error occurred.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error.\"}");
        }
    }

    private String getString(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString().trim();
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
