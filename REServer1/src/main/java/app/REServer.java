package app;

import com.mongodb.client.model.*;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import org.bson.BsonArray;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class REServer {
    private static final Logger LOG = LoggerFactory.getLogger(REServer.class);

    public static void main(String[] args) {
        LOG.info("Starting Real Estate server…");

        var dao = SqlDAO.getInstance(); // your SQL connection manager
        Connection conn = dao.getConnection();

        PropertyDAO propertyDAO = new PropertyDAO(conn);
        PostCodeDAO postcodeSearchDAO = new PostCodeDAO(conn);


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
                Property updatedDoc = propertyDAO.findById(ID);
                ctx.json(updatedDoc);
            });
            app.get("/sales", ctx -> {
                List<Property> list = propertyDAO.findAll();
                ctx.json(list);
            });
            app.get("/sales/ID_Count/{ID}", ctx -> {
                String ID = ctx.pathParam("ID");
                int result = propertyDAO.findIDCount(ID);
                ctx.json(Map.of(
                        "property_id", ID,
                        "id_counter", result
                ));
            });

        });
            app.get("/sales/Post_Count/{post_code}", ctx -> {
                String postcode = ctx.pathParam("post_code");

                int result = postcodeSearchDAO.getCount(postcode);
                ctx.json(new Document("post_code", postcode).append("search_count", result));

            });
//            app.get("/get", ctx -> {
//                List<Document> list = props
//                    .find(new Document("property_id", 1))
//                    .into(new ArrayList<>());
//            ctx.json(list);
//        });
            // create a new sales record
        app.post("/post", ctx -> {
            Property doc = new Property(
                    0,
                    "123456",
                    "123 Fake Street",
                    "2000",
                    1,
                    2,
                    new BigDecimal("500"),
                    "H",
                    "R2",
                    "Lot 1",
                    "Sunny Villa"
            );
            propertyDAO.insert(doc);
            ctx.status(201).json(doc);
        });

        // Get all sales for a specified postcode
            app.get("/sales/postcode/{postcode}", ctx -> {
                String postcode = ctx.pathParam("postcode");
                List<Property> list = propertyDAO.findByPostcode(postcode);
                int updateResult = postcodeSearchDAO.incrementAndReturnCount(postcode);
                ctx.json(list);
            });

            app.get("/salesby", ctx -> {
                List<SellerCount> list = propertyDAO.findSellerValues();
                ctx.json(list);
            });
            app.get("/sales/download_date/{download_date}", ctx -> {
                String date = ctx.pathParam("download_date");
                List<Property> list = propertyDAO.findByDownloadDate(date);
                ctx.json(list);
            });



        LOG.info("Server listening on http://localhost:7070");
    }
}
