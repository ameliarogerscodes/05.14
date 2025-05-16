package app;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import org.bson.BsonArray;
import org.bson.conversions.Bson;
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
                String postcode = ctx.pathParam("postcode");
                List<Document> list = props
                        .find(new Document("post_code", postcode))
                        .limit(20)
                        .into(new ArrayList<>());
                ctx.json(list);
            });

            app.get("/salesby", ctx -> {
                List<Bson> pipeline = List.of(
                        Aggregates.sample(100),
                        Aggregates.group("$council_name", Accumulators.sum("total_sales", 1)),
                        Aggregates.project(Projections.fields(
                                Projections.computed("council", "$council_name"),
                                Projections.computed("total_sales", "$total_sales")
                        ))
                );
                List<Document> results = new ArrayList<>();
                props.aggregate(pipeline).into(results);

                ctx.json(results);
            });
            app.get("/sales/download_date/{download_date}", ctx -> {
                String ID = ctx.pathParam("download_date");
                List<Document> list = props
                        .find(new Document("download_date", ID))
                        .limit(100)
                        .into(new ArrayList<>());
                ctx.json(list);
            });
        });



        LOG.info("Server listening on http://localhost:7070");
    }
}
