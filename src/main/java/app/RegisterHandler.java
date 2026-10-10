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
import java.sql.SQLIntegrityConstraintViolationException;

public class RegisterHandler implements HttpHandler {
    private static final Gson gson = new Gson();
    private UserRepository userRepository = new UserRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            if (body.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Empty payload\"}");
                return;
            }

            JsonObject json = gson.fromJson(body, JsonObject.class);
            String name = json.has("name") ? json.get("name").getAsString() : null;
            String phone = json.has("phone") ? json.get("phone").getAsString() : null;
            String email = json.has("email") ? json.get("email").getAsString() : null;
            String plainPassword = json.has("password") ? json.get("password").getAsString() : null;

            if (name == null || name.isEmpty() || phone == null || phone.isEmpty() ||
                email == null || email.isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Missing fields\"}");
                return;
            }

            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

            userRepository.insertUser(name, phone, email, hashedPassword);
            sendResponse(exchange, 201, "{\"message\": \"User registered\"}");

        } catch (SQLIntegrityConstraintViolationException e) {
            sendResponse(exchange, 409, "{\"error\": \"Email already exists\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
