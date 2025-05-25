package app;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;

public class IDDAO {
    private final MongoCollection<Document> collection;

    // This should be called with the "ID_Search_Counter" collection
    public IDDAO(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public int incrementAndReturnCount(String id) {
        Document updated = collection.findOneAndUpdate(
                new Document("id", id),
                new Document("$inc", new Document("search_count", 1)),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        );

        return updated.getInteger("search_count", 0);
    }


    public int getCount(String id) {
        Document result = collection.find(new Document("id", id)).first();
        return result != null ? result.getInteger("search_count", 0) : 0;
    }
}
