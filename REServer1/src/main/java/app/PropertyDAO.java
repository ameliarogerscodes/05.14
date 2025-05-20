package app;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyDAO {
    private final Connection conn;

    public PropertyDAO(Connection conn) throws SQLException {
        this.conn = conn;
        ensureCounterTable();
    }

    /** 1) Make sure our id_counters table exists */
    private void ensureCounterTable() throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS id_counters (" +
                        "  property_id VARCHAR(20) PRIMARY KEY," +
                        "  count       INT NOT NULL" +
                        ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /** 2) Fetch first 20 properties */
    public List<Property> findAll() throws SQLException {
        String sql = "SELECT * FROM properties LIMIT 20";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Property> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        }
    }

    /** 3) Fetch one property by ID and bump its counter */
    public Property findById(String propertyId) throws SQLException {
        String sql = "SELECT * FROM properties WHERE property_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, propertyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Property p = mapRow(rs);
                    incrementIDCount(propertyId);
                    return p;
                } else {
                    return null;
                }
            }
        }
    }

    /** Upsert into id_counters: insert with count=1 or increment existing count */
    private void incrementIDCount(String propertyId) throws SQLException {
        String sql =
                "INSERT INTO id_counters(property_id, count) VALUES (?, 1) " +
                        "ON DUPLICATE KEY UPDATE count = count + 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, propertyId);
            stmt.executeUpdate();
        }
    }

    /** 4) Read the current count (or 0 if missing) */
    public int findIDCount(String propertyId) throws SQLException {
        String sql = "SELECT count FROM id_counters WHERE property_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, propertyId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("count") : 0;
            }
        }
    }

    /** 5) Insert into properties */
    public void insert(Property property) throws SQLException {
        String sql =
                "INSERT INTO properties (" +
                        "  property_id, address, post_code, council_id," +
                        "  property_type_id, area, area_type, zoning," +
                        "  strata_lot_number, property_name" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, property.getPropertyId());
            stmt.setString(2, property.getAddress());
            stmt.setString(3, property.getPostCode());
            stmt.setObject(4, property.getCouncilId(), Types.INTEGER);
            stmt.setObject(5, property.getPropertyTypeId(), Types.INTEGER);
            stmt.setBigDecimal(6, property.getArea());
            stmt.setString(7, property.getAreaType());
            stmt.setString(8, property.getZoning());
            stmt.setString(9, property.getStrataLotNumber());
            stmt.setString(10, property.getPropertyName());
            stmt.executeUpdate();
        }
    }

    /** 6) Lookup by postcode */
    public List<Property> findByPostcode(String postcode) throws SQLException {
        String sql = "SELECT * FROM properties WHERE post_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, postcode);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Property> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        }
    }

    /** 7) Lookup by download_date via sales join */
    public List<Property> findByDownloadDate(String date) throws SQLException {
        String sql =
                "SELECT p.* FROM properties p " +
                        "JOIN sales s ON p.property_id = s.property_id " +
                        "WHERE s.download_date = ? " +
                        "LIMIT 100";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, date);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Property> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        }
    }

    /** 8) Seller counts by council */
    public List<SellerCount> findSellerValues() throws SQLException {
        String sql =
                "SELECT c.name AS council, COUNT(*) AS total_sales " +
                        "FROM sales s " +
                        "JOIN properties p ON s.property_id = p.property_id " +
                        "JOIN councils c ON p.council_id = c.id " +
                        "GROUP BY c.name " +
                        "ORDER BY total_sales DESC " +
                        "LIMIT 100";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<SellerCount> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new SellerCount(
                        rs.getString("council"),
                        rs.getInt("total_sales")
                ));
            }
            return result;
        }
    }

    /** Map a ResultSet row into your Property model */
    private Property mapRow(ResultSet rs) throws SQLException {
        return new Property(
                rs.getInt("id"),
                rs.getString("property_id"),
                rs.getString("address"),
                rs.getString("post_code"),
                rs.getObject("council_id", Integer.class),
                rs.getObject("property_type_id", Integer.class),
                rs.getBigDecimal("area"),
                rs.getString("area_type"),
                rs.getString("zoning"),
                rs.getString("strata_lot_number"),
                rs.getString("property_name")
        );
    }
}
