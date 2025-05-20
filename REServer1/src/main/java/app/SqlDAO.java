package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlDAO {
    private static SqlDAO instance;
    private Connection conn;

    private SqlDAO() {
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/realestate", "realestate", "secret"
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to SQL DB", e);
        }
    }

    public static synchronized SqlDAO getInstance() {
        if (instance == null) {
            instance = new SqlDAO();
        }
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }
}
