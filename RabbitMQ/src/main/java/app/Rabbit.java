package app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rabbit {
    private static final String ID_QUEUE = "id-queue";
    private static final String POSTCODE_QUEUE = "postcode-queue";    private static final Logger LOG = LoggerFactory.getLogger(Rabbit.class);

    public static void main(String[] args) throws Exception {
        // Setup RabbitMQ connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // Or your container's name/IP if using Docker
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Declare the queue (won’t throw error if it already exists)
        channel.queueDeclare(ID_QUEUE, false, false, false, null);
        channel.queueDeclare(POSTCODE_QUEUE, false, false, false, null);

        // Start HTTP server
        Javalin app = Javalin.create().start(7075);
        app.get("/", ctx -> ctx.result("RabbitMQ Bridge Microservice is Running"));

        app.post("/track/id/{ID}", ctx -> {
            String ID = ctx.pathParam("ID");
            LOG.info("Publishing to RabbitMQ: {}", ID);
            channel.basicPublish("", ID_QUEUE, null, ID.getBytes());

            ctx.status(202).result("Published ID " + ID + " to RabbitMQ");
        });

        app.post("/track/postcode/{Post}", ctx -> {
            String post = ctx.pathParam("Post");
            LOG.info("Publishing to RabbitMQ: {}", post);
            channel.basicPublish("", POSTCODE_QUEUE, null, post.getBytes());

            ctx.status(202).result("Published ID " + post + " to RabbitMQ");
        });

        LOG.info("RabbitMQ HTTP bridge listening at http://localhost:7075");
    }
}
