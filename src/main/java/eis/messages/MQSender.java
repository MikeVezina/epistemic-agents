package eis.messages;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MQSender extends MQConnector {
    private static final Logger LOG = LoggerFactory.getLogger(MQSender.class);

    public MQSender(String queueName) {
        super(queueName);

        // Only start the MQ producer if there is a consumer waiting.

        if (!getChannel().isOpen()) {
            LOG.error("Channel is not open.");
            close();
        } else {
            // Reset message should be the first message to synchronize any consumers
            // Reset messages do not need a body
            sendMessage(Message.createResetMessage());
        }
    }


    public synchronized void sendMessage(Message message) {
        // Fail Silently if the connection is closed.
        if (!getChannel().isOpen())
            return;

        try {
            AMQP.BasicProperties locationProps = new AMQP.BasicProperties.Builder().contentType(message.getContentType()).build();
            getChannel().basicPublish("", getQueueName(), locationProps, message.getMessageBody().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        // Delete the queue after we are done
        try {
            getChannel().queueDelete(getQueueName());
        } catch (IOException e) {
            LOG.error("Failed to delete queue.");
            e.printStackTrace();
        }
        super.close();
    }

}
