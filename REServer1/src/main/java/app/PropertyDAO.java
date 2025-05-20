package app;

import app.Property;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyDAO {
    private final Connection conn;

    public PropertyDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Property> findAll() throws SQLException {
        String query = "SELECT * FROM properties LIMIT 20";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            List<Property> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        }
    }

    public Property findById(String propertyId) throws SQLException {
        String query = "SELECT * FROM properties WHERE property_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, propertyId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // First map the row
                Property property = mapRow(rs);

                // Then increment the id_counter
                incrementIDCount(propertyId);

                return property;
            } else {
                return null;
            }
        }
    }
    private void incrementIDCount(String propertyId) throws SQLException {
        String update = "UPDATE properties SET id_counter = COALESCE(id_counter, 0) + 1 WHERE property_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setString(1, propertyId);
            stmt.executeUpdate();
        }
    }


    public void insert(Property property) throws SQLException {
        String query = "INSERT INTO properties (property_id, address, post_code, council_id, " +
                "property_type_id, area, area_type, zoning, strata_lot_number, property_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, property.propertyId());
            stmt.setString(2, property.address());
            stmt.setString(3, property.postCode());
            stmt.setObject(4, property.councilId(), Types.INTEGER);
            stmt.setObject(5, property.propertyTypeId(), Types.INTEGER);
            stmt.setBigDecimal(6, property.area());
            stmt.setString(7, property.areaType());
            stmt.setString(8, property.zoning());
            stmt.setString(9, property.strataLotNumber());
            stmt.setString(10, property.propertyName());
            stmt.executeUpdate();
        }
    }

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
    public int findIDCount(String propertyId) throws SQLException {
        String query = "SELECT id_counter FROM properties WHERE property_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id_counter") : 0;
        }
    }
    public List<Property> findByPostcode(String postcode) throws SQLException {
        String query = "SELECT * FROM properties WHERE post_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, postcode);
            ResultSet rs = stmt.executeQuery();

            List<Property> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        }
    }

    public List<Property> findByDownloadDate(String date) throws SQLException {
        String query = "SELECT p.* FROM properties p " +
                "JOIN sales s ON p.property_id = s.property_id " +
                "WHERE s.download_date = ? " +
                "LIMIT 100";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();

            List<Property> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        }
    }


    public List<SellerCount> findSellerValues() throws SQLException {
        String query = "SELECT c.name AS council, COUNT(*) AS total_sales " +
                "FROM sales s " +
                "JOIN properties p ON s.property_id = p.property_id " +
                "JOIN councils c ON p.council_id = c.id " +
                "GROUP BY c.name " +
                "ORDER BY total_sales DESC " +
                "LIMIT 100";

        try (PreparedStatement stmt = conn.prepareStatement(query);
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



}
