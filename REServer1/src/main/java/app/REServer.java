package app;

import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import static io.javalin.apibuilder.ApiBuilder.*;


public class REServer {
    private static final Logger LOG = LoggerFactory.getLogger(REServer.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Real Estate server…");

        // grab your singletons/DAOs
        SqlDAO dao = SqlDAO.getInstance();
        Connection conn = dao.getConnection();
        PropertyDAO propertyDAO = new PropertyDAO(conn);
        PostCodeDAO postcodeSearchDAO = new PostCodeDAO(conn);

        Javalin app = Javalin.create(cfg -> {
            // set JSON as the default content type
            cfg.http.defaultContentType = "application/json";

            // define all your endpoints in one place
            cfg.router.apiBuilder(() -> {
                get("/", ctx -> ctx.result("Real Estate server is running"));

                get("/sales/{ID}", ctx -> {
                    String id = ctx.pathParam("ID");
                    var p = propertyDAO.findById(id);
                    if (p == null) {
                        ctx.status(404).result("Property not found");
                    } else {
                        ctx.json(p);
                    }
                });

                get("/sales", ctx -> {
                    List<Property> all = propertyDAO.findAll();
                    ctx.json(all);
                });

                get("/sales/ID_Count/{ID}", ctx -> {
                    String id = ctx.pathParam("ID");
                    int count = propertyDAO.findIDCount(id);
                    ctx.json(Map.of("property_id", id, "id_counter", count));
                });

                // example POST to hard-coded record
                post("/post", ctx -> {
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

                get("/sales/postcode/{postcode}", ctx -> {
                    String pc = ctx.pathParam("postcode");
                    postcodeSearchDAO.incrementAndReturnCount(pc);
                    List<Property> list = propertyDAO.findByPostcode(pc);
                    ctx.json(list);
                });

                get("/salesby", ctx -> {
                    ctx.json(propertyDAO.findSellerValues());
                });

                get("/sales/download_date/{download_date}", ctx -> {
                    String date = ctx.pathParam("download_date");
                    ctx.json(propertyDAO.findByDownloadDate(date));
                });
            });
        }).start(7070);

        LOG.info("Server listening on http://localhost:7070");
    }
}
