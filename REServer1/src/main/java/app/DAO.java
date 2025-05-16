package app;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DAO {
    private static DAO instance;
    private final MongoClient client;
    private final MongoDatabase database;

    private DAO() {
        // Adjust URI & database name as needed
        String uri = "mongodb+srv://tommasis:123@real-estate-project.wwjzxma.mongodb.net/";
        ConnectionString cs = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .build();

        this.client = MongoClients.create(settings);
        this.database = client.getDatabase("realestate");
    }


    public static synchronized DAO getInstance() {
        if (instance == null) {
            instance = new DAO();
        }
        return instance;
    }


    public MongoDatabase getDatabase() {
        return database;
    }


    public void close() {
        client.close();
    }
}
