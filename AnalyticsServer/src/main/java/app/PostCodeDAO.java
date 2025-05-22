package app;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;

public class PostCodeDAO {
    private final MongoCollection<Document> collection;

    public PostCodeDAO(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public int incrementAndReturnCount(String postcode) {
        Document updated = collection.findOneAndUpdate(
                new Document("post_code", postcode),
                new Document("$inc", new Document("search_count", 1)),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        );

        return updated.getInteger("search_count", 0);
    }

    public int getCount(String postcode) {
        Document result = collection.find(new Document("post_code", postcode)).first();
        return result != null ? result.getInteger("search_count", 0) : 0;
    }
}
