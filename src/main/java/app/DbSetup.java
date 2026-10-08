package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbSetup {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/";
        String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
        String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "S0wb@rnik@#224";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS auth_db");
            System.out.println("Database auth_db created or already exists.");

            stmt.executeUpdate("USE auth_db");

            String createTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(255) NOT NULL, "
                    + "phone VARCHAR(50) NOT NULL, "
                    + "email VARCHAR(255) NOT NULL UNIQUE, "
                    + "password VARCHAR(255) NOT NULL)";
            stmt.executeUpdate(createTable);
            System.out.println("Table users created or already exists.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
