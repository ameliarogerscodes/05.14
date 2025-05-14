package app;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class REServer {
    private static final Logger LOG = LoggerFactory.getLogger(REServer.class);

    public static void main(String[] args) {
        LOG.info("Starting Real Estate server…");

        // 1) Initialize Mongo
        var dao = DAO.getInstance();
        MongoDatabase db = dao.getDatabase();
        // make sure this matches the collection you loaded
        MongoCollection<Document> props = db.getCollection("properties");

        var app = Javalin.create()
                .get("/", ctx -> ctx.result("Real Estate server is running"))
                .start(7070);

        // configure endpoint handlers to process HTTP requests
        JavalinConfig config = new JavalinConfig();
        config.router.apiBuilder(() -> {
            // Sales records are immutable hence no PUT and DELETE

            // return a sale by sale ID
            app.get("/sales/{ID}", ctx -> {
                String ID = ctx.pathParam("ID");
                List<Document> list = props
                        .find(new Document("property_id", ID))
                        .into(new ArrayList<>());
                ctx.json(list);
            });
            app.get("/sales", ctx -> {
                List<Document> list = props
                        .find(new Document())
                        .limit(20)
                        .into(new ArrayList<>());
                ctx.json(list);
            });
            // get all sales records - could be big!
            app.get("/get", ctx -> {
                List<Document> list = props
                    .find(new Document("property_id", 1))
                    .into(new ArrayList<>());
            ctx.json(list);
        });
            // create a new sales record
            app.post("/post", ctx -> {
                Document doc = new Document("property_id", 1);
            props.insertOne(doc);
            ctx.status(201).json(doc);
            });
            // Get all sales for a specified postcode
            app.get("/sales/postcode/{postcode}", ctx -> {
                // salesHandler.findSaleByPostCode(ctx, ctx.pathParam("postcode"));
            });
        });



        LOG.info("Server listening on http://localhost:7070");
    }
}
