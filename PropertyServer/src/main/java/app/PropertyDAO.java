package app;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class PropertyDAO {
    private final MongoCollection<Document> collection;

    public PropertyDAO(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public Document findById(String id) {
        return collection.find(new Document("property_id", id)).first();
    }

    public List<Document> findAll() {
        return collection.find().limit(20).into(new ArrayList<>());
    }

    public List<Document> findByPostcode(String postcode) {
        return collection.find(new Document("post_code", postcode)).limit(20).into(new ArrayList<>());
    }
    public Document findIDCount(String id) {
        List<Document> result = collection.aggregate(List.of(
                Aggregates.match(new Document("property_id", id)),
                Aggregates.project(Projections.fields(
                        Projections.include("property_id"),
                        Projections.excludeId()
                ))
        )).into(new ArrayList<>());

        return result.isEmpty() ? null : result.get(0);
    }

    public List<Document> findSellerValues() {
        List<Bson> pipeline = List.of(
                Aggregates.sample(100),
                Aggregates.group("$council_name", Accumulators.sum("total_sales", 1)),
                Aggregates.project(Projections.fields(
                        Projections.computed("council", "$council_name"),
                        Projections.computed("total_sales", "$total_sales")
                ))
        );

        List<Document> results = new ArrayList<>();
        collection.aggregate(pipeline).into(results); // ✅ proper call
        return results;
    }
    public List<Document> findByDownloadDate(String date) {
        return collection
                .find(new Document("download_date", date))
                .limit(100)
                .into(new ArrayList<>());
    }

    public void insertOne(Document document) {
        collection.insertOne(document);
    }

}
