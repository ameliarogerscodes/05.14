package org.example;

import org.apache.commons.csv.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class Main {
    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();

    // JDBC connection info
    private static final String JDBC_URL      = "jdbc:mysql://localhost:3307/mysql-520";
    private static final String JDBC_USER     = "root";
    private static final String JDBC_PASSWORD = "";           // empty, per your setup

    // path & batch size
    private static final String CSV_PATH      = "/Users/ameliarogers/Downloads/nsw_property_data.csv";
    private static final int    BATCH_SIZE    = 1000;

    public static void main(String[] args) {
        System.out.println("Starting CSV → MySQL load…");

        // 1) Open JDBC connection
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);

            // 2) Prepare your INSERT statement (adjust columns to match your DDL)
            String sql =
                    String.format("INSERT INTO properties (property_id, address, post_code, council_id, property_type_id, area, area_type, zoning, strata_lot_number, property_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");


            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                // 3) Parse the CSV
                Path path = Paths.get(CSV_PATH);
                try (CSVParser parser = CSVParser.parse(path, StandardCharsets.UTF_8, CSV_FORMAT)) {
                    int count = 0;
                    for (CSVRecord record : parser) {
                        // map your CSV headers here:
                        stmt.setString(1, record.get("property_id"));
                        stmt.setString(2, record.get("address"));
                        stmt.setString(3, record.get("post_code"));

                        // If you don’t have council_id/property_type_id in CSV, set to NULL or 0
                        stmt.setObject(4, record.isSet("council_id")
                                ? Integer.valueOf(record.get("council_id"))
                                : null);
                        stmt.setObject(5, record.isSet("property_type_id")
                                ? Integer.valueOf(record.get("property_type_id"))
                                : null);

                        String areaStr = record.get("area");
                        if (areaStr == null || areaStr.isBlank()) {
                            stmt.setNull(6, Types.DECIMAL);
                        } else {
                            stmt.setBigDecimal(6, new java.math.BigDecimal(areaStr));
                        }
                        stmt.setString(7, record.get("area_type"));
                        stmt.setString(8, record.get("zoning"));
                        stmt.setString(9, record.get("strata_lot_number"));
                        stmt.setString(10, record.get("property_name"));

                        stmt.addBatch();
                        count++;

                        // flush every BATCH_SIZE rows
                        if (count % BATCH_SIZE == 0) {
                            stmt.executeBatch();
                            conn.commit();
                            System.out.println("Inserted " + count + " rows...");
                        }
                    }
                    // final flush
                    stmt.executeBatch();
                    conn.commit();
                    System.out.println("Finished! Total rows inserted: " + count);
                }
            }

        } catch (SQLException e) {
            System.err.println("JDBC error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Failed to read CSV: " + e.getMessage());
        }
    }
}
