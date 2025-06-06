package org.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// mongo imports
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

// main class for all functionality
public class Main {
// load in csv format
    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader()
            .setSkipHeaderRecord(true)
            .setAllowDuplicateHeaderNames(false)
            .build();

    // mongo db connection link
    static final private String MONGO_URI    = "mongodb+srv://tommasis:123@real-estate-project.wwjzxma.mongodb.net/";
    static final private String DB_NAME      = "realestate"; // database name
    static final private String COLL_NAME    = "properties"; // column name
    static final private String PATH_TO_FILE =
            "/Users/sebastiantommasi/Downloads/nsw_property_data.csv";

    // Tune this based on memory and network speed
    static final private int BATCH_SIZE = 1000;
    // initial start
    public static void main(final String[] args) {
        System.out.println("Hello and welcome!");

        // 1) Create Mongo client once
        final ConnectionString connString = new ConnectionString(MONGO_URI);
        final MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            final MongoDatabase database   = mongoClient.getDatabase(DB_NAME);
            final MongoCollection<Document> collection = database.getCollection(COLL_NAME);

            // 2) Prepare CSV parser
            final Path csvFilePath = Paths.get(PATH_TO_FILE);
            try (CSVParser parser = CSVParser.parse(csvFilePath, StandardCharsets.UTF_8, CSV_FORMAT)) {
                System.out.println("File opened; headers: " + parser.getHeaderNames());

                // Batch buffer
                final List<Document> batch = new ArrayList<>(BATCH_SIZE);
                int totalCount = 0;

                for (final CSVRecord record : parser) {
                    final Map<String, String> recordMap = record.toMap();
                    final Document doc = new Document(recordMap);
                    batch.add(doc);
                    totalCount++;

                    // Flush every BATCH_SIZE docs
                    if (batch.size() >= BATCH_SIZE) {
                        collection.insertMany(batch);
                        batch.clear();
                        System.out.println("Inserted " + totalCount + " records so far...");
                    }
                }

                // Flush any remaining docs
                if (!batch.isEmpty()) {
                    collection.insertMany(batch);
                    System.out.println("Inserted final " + batch.size() + " records.");
                }

                System.out.println("Total records inserted: " + totalCount);
            } catch (IOException e) {
                System.err.println("Failed to read CSV: " + e.getMessage());
            }
        }

        System.out.println("Done!");
    }
}
