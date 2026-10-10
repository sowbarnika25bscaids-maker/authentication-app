package app;

public class DatabaseConfig {
    public static final String HOST = "localhost";
    public static final String PORT = "3306";
    public static final String DB_NAME = "framework_free_db";
    public static final String USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    public static final String PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "S0wb@rnik@#224";
    public static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME;
}
