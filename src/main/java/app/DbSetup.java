package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbSetup {
    public static void main(String[] args) {
        String url = "jdbc:mysql://" + DatabaseConfig.HOST + ":" + DatabaseConfig.PORT + "/";
        
        try (Connection conn = DriverManager.getConnection(url, DatabaseConfig.USER, DatabaseConfig.PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DatabaseConfig.DB_NAME);
            System.out.println("Database " + DatabaseConfig.DB_NAME + " created or already exists.");

            stmt.executeUpdate("USE " + DatabaseConfig.DB_NAME);

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
