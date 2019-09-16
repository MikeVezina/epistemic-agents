package eis.messages;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.Closeable;
import java.io.IOException;

public abstract class MQConnector implements Closeable {
    public static final String HOST = "localhost";
    private Connection connection = null;
    private Channel channel = null;
    private String queueName;

    public MQConnector(String queueName) {
        this.queueName = queueName;
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(HOST);

        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            // Delete the queue unless there is another connection. (So that the queue can be cleared)
            channel.queueDeclare(queueName, false, false, true, null);
            channel.queuePurge(queueName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected Channel getChannel() {
        return channel;
    }

    public String getQueueName() {
        return queueName;
    }

    @Override
    public void close() {
        try {
            if (connection.isOpen()) {
                connection.close();
            }
        } catch (IOException ioE)
        {
            System.err.println("Failed to close the MQ connection: ");
            ioE.printStackTrace();
        }
    }
}
