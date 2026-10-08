package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB_NAME = "auth_db";

    // Read from environment variables, or use simple beginner-friendly defaults
    private static final String USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD")
            : "S0wb@rnik@#224";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
