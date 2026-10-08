package app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegistrationApiTest {

    private int port;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        port = Main.startServer(0);
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        Main.stopServer();
    }

    @Test
    void testGetMethodNotAllowed() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/register"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode(), "Expected 405 Method Not Allowed for GET request");
    }

    @Test
    void testPostWithEmptyBodyReturnsBadRequest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/register"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for empty payload");
    }

    @Test
    void testPasswordHashing() {
        String plainPassword = "SuperSecret123!";

        // We will create a utility method inside Main for hashing
        String hash = Main.hashPassword(plainPassword);

        // 1. The hash must not be the plain text
        org.junit.jupiter.api.Assertions.assertNotEquals(plainPassword, hash);

        // 2. The hash must be verified successfully by jBCrypt
        org.junit.jupiter.api.Assertions.assertTrue(org.mindrot.jbcrypt.BCrypt.checkpw(plainPassword, hash));
    }

    @Test
    void testRegisterDuplicateEmailReturnsConflict() throws Exception {
        // Prepare a JSON payload
        String jsonPayload = "{\"name\":\"Test User\",\"phone\":\"1234567890\",\"email\":\"duplicate@example.com\",\"password\":\"pass123\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/register"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .header("Content-Type", "application/json")
                .build();

        // 1st request should create the user or fail if it exists from a previous test
        // run
        client.send(request, HttpResponse.BodyHandlers.ofString());

        // 2nd request with exactly the same email MUST return 409 Conflict
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Since we are inserting, we expect our Java code to catch the MySQL unique
        // constraint violation and return 409
        assertEquals(409, response.statusCode(), "Expected 409 Conflict for duplicate email registration");
    }
}