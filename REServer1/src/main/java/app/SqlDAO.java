package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlDAO {
    private static SqlDAO instance;
    private Connection conn;

    private SqlDAO() {
        try {
            // explicitly load the driver class
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3307/mysql-520"
                            + "?useSSL=false&allowPublicKeyRetrieval=true",
                    "root",
                    ""
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver not found on classpath", e);
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
