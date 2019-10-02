package messages;

import com.rabbitmq.client.*;

import java.io.IOException;

public class MQReceiver extends MQConnector {


    public MQReceiver(String queueName, DeliverCallback deliverCallback) {
        super(queueName);

        try {
            getChannel().basicConsume(queueName, true, deliverCallback, consumerTag -> {
                System.out.println("Consumer cancelled: " + consumerTag);
            });
        } catch (IOException ex) {
            System.err.println("Failed to set up receiver callback.");
            ex.printStackTrace();
        }
    }
}
