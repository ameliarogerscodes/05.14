package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostCodeDAO {
    private final Connection conn;

    public PostCodeDAO(Connection conn) {
        this.conn = conn;
    }

    public int incrementAndReturnCount(String postcode) throws SQLException {
        // Try to update existing row
        String update = "UPDATE postcode_searches SET search_count = search_count + 1 WHERE post_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setString(1, postcode);
            int rows = stmt.executeUpdate();

            // If no row updated, insert it
            if (rows == 0) {
                String insert = "INSERT INTO postcode_searches (post_code, search_count) VALUES (?, 1)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insert)) {
                    insertStmt.setString(1, postcode);
                    insertStmt.executeUpdate();
                }
            }
        }

        return getCount(postcode); // Return updated count
    }

    public int getCount(String postcode) throws SQLException {
        String query = "SELECT search_count FROM postcode_searches WHERE post_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, postcode);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("search_count") : 0;
        }
    }
}
