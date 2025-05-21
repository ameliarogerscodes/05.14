package app;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import org.bson.BsonArray;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;

public class PropertyServer {
    static final Logger LOG = LoggerFactory.getLogger(PropertyServer.class);

    public static void main(String[] args) {
        LOG.info("Starting Real Estate server…");

        var dao = DAO.getInstance();
        MongoDatabase db = dao.getDatabase();
        // make sure this matches the collection you loaded
        app.PropertyDAO propertyDAO = new app.PropertyDAO(db.getCollection("properties"));
        var app = Javalin.create()
                .get("/", ctx -> ctx.result("Real Estate server is running"))

                .start(7071);

        // configure endpoint handlers to process HTTP requests
        JavalinConfig config = new JavalinConfig();
        config.router.apiBuilder(() -> {
            // Sales records are immutable hence no PUT and DELETE

            // return a sale by sale ID
            app.get("/sales/{ID}", ctx -> {
                String ID = ctx.pathParam("ID");
                Document updatedDoc = propertyDAO.findByIdAndIncrement(ID);
                ctx.json(updatedDoc);
            });
            app.get("/sales", ctx -> {
                List<Document> list = propertyDAO.findAll();
                ctx.json(list);
            });

//            app.get("/get", ctx -> {
//                List<Document> list = props
//                    .find(new Document("property_id", 1))
//                    .into(new ArrayList<>());
//            ctx.json(list);
//        });

            // create a new sales record
            app.post("/post", ctx -> {
                Document doc = new Document("property_id", 1);
            propertyDAO.insertOne(doc);
            ctx.status(201).json(doc);
            });
            // Get all sales for a specified postcode
            app.get("/sales/postcode/{postcode}", ctx -> {
                String postcode = ctx.pathParam("postcode");
                List<Document> list = propertyDAO.findByPostcode(postcode);
                ctx.json(list);
            });

            app.get("/salesby", ctx -> {
                List<Document> list = propertyDAO.findSellerValues();
                ctx.json(list);
            });
            app.get("/sales/download_date/{download_date}", ctx -> {
                String date = ctx.pathParam("download_date");
                List<Document> list = propertyDAO.findByDownloadDate(date);
                ctx.json(list);
            });

        });



        LOG.info("Server listening on http://localhost:7070");
    }
}
