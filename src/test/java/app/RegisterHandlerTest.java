package app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterHandlerTest {

    @Test
    void testSetup() {
        // This is a placeholder test to ensure our test environment is ready.
        assertTrue(true);
    }

    @Test
    void testMissingRequiredFields() throws Exception {
        // 1. Start a temporary test server
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer
                .create(new java.net.InetSocketAddress(0), 0);
        server.createContext("/api/register", new RegisterHandler());
        server.setExecutor(null);
        server.start();

        int port = server.getAddress().getPort();
        java.net.URI uri = java.net.URI.create("http://localhost:" + port + "/api/register");

        // 2. Prepare incomplete data (missing email and password)
        String requestBody = "{\"name\":\"John\", \"phone\":\"1234567890\"}";

        // 3. Send the POST request
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpResponse<String> response = client.send(request,
            java.net.http.HttpResponse.BodyHandlers.ofString());

        // 4. Turn off the temporary server
        server.stop(0);

        // 5. Assert that the server rejected the incomplete data with status 400
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("error"), "Response should contain an error message");
    }

    @Test
    void testSuccessfulRegistration() throws Exception {
        // 1. Start a temporary test server
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer
                .create(new java.net.InetSocketAddress(0), 0);
        server.createContext("/api/register", new RegisterHandler());
        server.setExecutor(null);
        server.start();

        int port = server.getAddress().getPort();
        java.net.URI uri = java.net.URI.create("http://localhost:" + port + "/api/register");

        // 2. Prepare valid data with a unique email
        String uniqueEmail = "testuser" + System.currentTimeMillis() + "@example.com";
        String requestBody = "{\"name\":\"Test User\", \"phone\":\"1234567890\", \"email\":\""
                + uniqueEmail + "\", \"password\":\"secretpassword\"}";

        // 3. Send the POST request
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpResponse<String> response = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString());

        // 4. Turn off the temporary server
        server.stop(0);

        // 5. Assert that the server accepted the registration with status 201 Created
        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Registration successful"));
    }

    @Test
    void testDuplicateEmail() throws Exception {
        // 1. Start a temporary test server
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer
                .create(new java.net.InetSocketAddress(0), 0);
        server.createContext("/api/register", new RegisterHandler());
        server.setExecutor(null);
        server.start();

        int port = server.getAddress().getPort();
        java.net.URI uri = java.net.URI.create("http://localhost:" + port + "/api/register");

        // 2. Prepare data with an email
        String duplicateEmail = "duplicate" + System.currentTimeMillis() + "@example.com";
        String requestBody = "{\"name\":\"Duplicate User\", \"phone\":\"1112223333\", \"email\":\""
                + duplicateEmail + "\", \"password\":\"pass\"}";

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        // 3. Send the FIRST request (should succeed)
        java.net.http.HttpResponse<String> firstResponse = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString());
        assertEquals(201, firstResponse.statusCode(), "First registration should succeed");

        // 4. Send the EXACT SAME request again (should fail)
        java.net.http.HttpResponse<String> secondResponse = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString());

        // 5. Turn off the server
        server.stop(0);

        // 6. Assert that the server rejected the duplicate with status 409 Conflict
        assertEquals(409, secondResponse.statusCode(), "Second registration should be rejected as a duplicate");
        assertTrue(secondResponse.body().contains("already registered"));
    }

}
