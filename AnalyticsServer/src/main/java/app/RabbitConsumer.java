package app;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitConsumer.class);
    private static final String ID_QUEUE = "id-queue";
    private static final String POSTCODE_QUEUE = "postcode-queue";
    public static void startIDConsumer(IDDAO iddao) {
        new Thread(() -> {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare(ID_QUEUE, false, false, false, null);


                LOG.info("RabbitConsumer listening on {}", ID_QUEUE);

                channel.basicConsume(ID_QUEUE, true, (tag, delivery) -> {
                    String id = new String(delivery.getBody(), "UTF-8");
                    LOG.info("Received ID from RabbitMQ: {}", id);

                    int count = iddao.incrementAndReturnCount(id);
                    LOG.info("Updated count for ID {} to {}", id, count);
                }, tag -> {});
            } catch (Exception e) {
                LOG.error("Error in RabbitConsumer", e);
            }
        }).start();
    }
    public static void startPostConsumer(PostCodeDAO postCodeDAO) {
        new Thread(() -> {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare(POSTCODE_QUEUE, false, false, false, null);


                LOG.info("RabbitConsumer listening on {}", POSTCODE_QUEUE);

                channel.basicConsume(POSTCODE_QUEUE, true, (tag, delivery) -> {
                    String id = new String(delivery.getBody(), "UTF-8");
                    LOG.info("Received PostCode from RabbitMQ: {}", id);

                    int count = postCodeDAO.incrementAndReturnCount(id);
                    LOG.info("Updated count for PostCode {} to {}", id, count);
                }, tag -> {
                });
            } catch (Exception e) {
                LOG.error("Error in RabbitConsumer", e);
            }
        }).start();
    }
}
