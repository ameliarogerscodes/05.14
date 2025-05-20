package org.example;

import org.apache.commons.csv.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class Main {
    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();

    private static final String JDBC_URL      =
            "jdbc:mysql://127.0.0.1:3307/mysql-520?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String JDBC_USER     = "root";
    private static final String JDBC_PASSWORD = "";

    private static final String CSV_PATH   = "/Users/ameliarogers/Downloads/nsw_property_data.csv";
    private static final int    BATCH_SIZE = 1000;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting CSV → MySQL load…");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);

            // lookup caches
            Map<String,Integer> councilCache      = new HashMap<>();
            Map<String,Integer> propertyTypeCache = new HashMap<>();

            // prepare all statements
            PreparedStatement insertCouncil = conn.prepareStatement(
                    "INSERT IGNORE INTO councils(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS
            );
            PreparedStatement findCouncil = conn.prepareStatement(
                    "SELECT id FROM councils WHERE name = ?"
            );

            PreparedStatement insertPropType = conn.prepareStatement(
                    "INSERT IGNORE INTO property_types(type) VALUES(?)", Statement.RETURN_GENERATED_KEYS
            );
            PreparedStatement findPropType = conn.prepareStatement(
                    "SELECT id FROM property_types WHERE type = ?"
            );

            PreparedStatement insertProperty = conn.prepareStatement(
                    "INSERT INTO properties(" +
                            " property_id,address,post_code,council_id,property_type_id," +
                            " area,area_type,zoning,strata_lot_number,property_name" +
                            ") VALUES(?,?,?,?,?,?,?,?,?,?)"
            );

            PreparedStatement insertSale = conn.prepareStatement(
                    "INSERT INTO sales(" +
                            " property_id,purchase_price,contract_date,settlement_date," +
                            " download_date,nature_of_property,primary_purpose,legal_description" +
                            ") VALUES(?,?,?,?,?,?,?,?)"
            );

            // parse CSV
            Path path = Paths.get(CSV_PATH);
            try (CSVParser parser = CSVParser.parse(path, StandardCharsets.UTF_8, CSV_FORMAT)) {
                int count = 0;
                for (CSVRecord rec : parser) {
                    // —————————————————————————————————————
                    // 1) council lookup/insert
                    // —————————————————————————————————————
                    String councilName = rec.get("council_name");
                    Integer councilId = councilCache.get(councilName);
                    if (councilId == null) {
                        // try insert
                        insertCouncil.setString(1, councilName);
                        insertCouncil.executeUpdate();
                        try (ResultSet gen = insertCouncil.getGeneratedKeys()) {
                            if (gen.next()) {
                                councilId = gen.getInt(1);
                            }
                        }
                        // if already existed, fetch it
                        if (councilId == null) {
                            findCouncil.setString(1, councilName);
                            try (ResultSet rs = findCouncil.executeQuery()) {
                                rs.next();
                                councilId = rs.getInt("id");
                            }
                        }
                        councilCache.put(councilName, councilId);
                    }

                    // —————————————————————————————————————
                    // 2) property_type lookup/insert
                    // —————————————————————————————————————
                    String propTypeName = rec.get("property_type");
                    Integer propTypeId = propertyTypeCache.get(propTypeName);
                    if (propTypeId == null) {
                        insertPropType.setString(1, propTypeName);
                        insertPropType.executeUpdate();
                        try (ResultSet gen = insertPropType.getGeneratedKeys()) {
                            if (gen.next()) {
                                propTypeId = gen.getInt(1);
                            }
                        }
                        if (propTypeId == null) {
                            findPropType.setString(1, propTypeName);
                            try (ResultSet rs = findPropType.executeQuery()) {
                                rs.next();
                                propTypeId = rs.getInt("id");
                            }
                        }
                        propertyTypeCache.put(propTypeName, propTypeId);
                    }

                    // —————————————————————————————————————
                    // 3) insert into properties
                    // —————————————————————————————————————
                    insertProperty.setString(1, rec.get("property_id"));
                    insertProperty.setString(2, rec.get("address"));
                    insertProperty.setString(3, rec.get("post_code"));
                    insertProperty.setObject(4, councilId, Types.INTEGER);
                    insertProperty.setObject(5, propTypeId, Types.INTEGER);

                    // area
                    String areaStr = rec.get("area");
                    if (areaStr == null || areaStr.isBlank()) {
                        insertProperty.setNull(6, Types.DECIMAL);
                    } else {
                        insertProperty.setBigDecimal(6, new java.math.BigDecimal(areaStr));
                    }

                    insertProperty.setString(7, rec.get("area_type"));
                    insertProperty.setString(8, rec.get("zoning"));
                    insertProperty.setString(9, rec.get("strata_lot_number"));
                    insertProperty.setString(10, rec.get("property_name"));
                    insertProperty.addBatch();

                    // —————————————————————————————————————
                    // 4) insert into sales
                    // —————————————————————————————————————
                    insertSale.setString(1, rec.get("property_id"));
                    insertSale.setLong(2, Long.parseLong(rec.get("purchase_price")));
                    insertSale.setDate(3, Date.valueOf(rec.get("contract_date")));
                    insertSale.setDate(4, Date.valueOf(rec.get("settlement_date")));
                    insertSale.setDate(5, Date.valueOf(rec.get("download_date")));
                    insertSale.setString(6, rec.get("nature_of_property"));
                    insertSale.setString(7, rec.get("primary_purpose"));
                    insertSale.setString(8, rec.get("legal_description"));
                    insertSale.addBatch();

                    // flush in batches
                    if (++count % BATCH_SIZE == 0) {
                        insertProperty.executeBatch();
                        insertSale.executeBatch();
                        conn.commit();
                        System.out.println("Inserted " + count + " rows...");
                    }
                }

                // final flush
                insertProperty.executeBatch();
                insertSale.executeBatch();
                conn.commit();
                System.out.println("Finished! Total rows inserted: " + count);
            }
        }
    }
}
