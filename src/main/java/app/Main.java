package app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;

public class Main {
    private static HttpServer server;

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/auth_db";
    private static final String DB_USER = "root";
    // NOTE: In a real app, passwords are read from env variables.
    // We are keeping it simple for this environment.
    private static final String DB_PASS = "root";

    public static void main(String[] args) throws IOException {
        startServer(8080);
        System.out.println("Web app server started on port 8080...");
    }

    public static int startServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/register", new RegistrationHandler());
        server.setExecutor(null);
        server.start();
        return server.getAddress().getPort();
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    static class RegistrationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes()).trim();
            if (body.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"Missing payload\"}");
                return;
            }

            // Minimal JSON extraction
            String name = extractJsonValue(body, "name");
            String phone = extractJsonValue(body, "phone");
            String email = extractJsonValue(body, "email");
            String plainPassword = extractJsonValue(body, "password");

            if (name == null || phone == null || email == null || plainPassword == null ||
                    name.isEmpty() || phone.isEmpty() || email.isEmpty() || plainPassword.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"Missing required fields\"}");
                return;
            }

            String hashedPassword = hashPassword(plainPassword);

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "INSERT INTO users (name, phone, email, password) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, phone);
                    stmt.setString(3, email);
                    stmt.setString(4, hashedPassword);
                    stmt.executeUpdate();

                    sendResponse(exchange, 201, "{\"message\":\"Registration successful\"}");
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                sendResponse(exchange, 409, "{\"error\":\"Email already exists\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private String extractJsonValue(String json, String key) {
            String search = "\"" + key + "\":\"";
            int start = json.indexOf(search);
            if (start == -1)
                return null;
            start += search.length();
            int end = json.indexOf("\"", start);
            return end == -1 ? null : json.substring(start, end);
        }
    }
}
