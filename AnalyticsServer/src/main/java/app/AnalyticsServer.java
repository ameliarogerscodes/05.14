package app;

import app.IDDAO;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class AnalyticsServer {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsServer.class);

    public static void main(String[] args) {
        LOG.info("Starting Real Estate server…");

        var dao = DAO.getInstance();
        MongoDatabase db = dao.getDatabase();
        // make sure this matches the collection you loaded
        IDDAO iddao = new IDDAO(db.getCollection("ID_Search_Counter"));
        PostCodeDAO postCodeDAO = new PostCodeDAO(db.getCollection("Post_Search_Counter"));
        RabbitConsumer.startIDConsumer(iddao);
        RabbitConsumer.startPostConsumer(postCodeDAO);


        var app = Javalin.create()
                .get("/", ctx -> ctx.result("Real Estate server is running"))

                .start(7072);

        // configure endpoint handlers to process HTTP requests
        JavalinConfig config = new JavalinConfig();
        config.router.apiBuilder(() -> {
            // Sales records are immutable hence no PUT and DELETE

            // return a sale by sale ID


            app.get("/track/ID_Count/{ID}", ctx -> {
                String ID = ctx.pathParam("ID");
                int result = iddao.getCount(ID);
                ctx.json(new Document("id", ID).append("search_count", result));
            });

            // Get all sales for a specified postcode
            app.get("/track/Post_Count/{postcode}", ctx -> {
                String postcode = ctx.pathParam("postcode");
                int list = postCodeDAO.getCount(postcode);
                ctx.json(new Document("post_code", postcode).append("search_count", list));
            });



        });



        LOG.info("Server listening on http://localhost:7072");
    }
}
