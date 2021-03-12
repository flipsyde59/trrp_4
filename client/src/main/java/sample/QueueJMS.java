package sample;

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;

import javax.jms.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.amazon.sqs.javamessaging.SQSSession.UNORDERED_ACKNOWLEDGE;

public class QueueJMS {

    public static String queueName = "TRRP4";

    private static MessageProducer producer;
    public static MessageProducer getProducer() {
        return producer;
    }

    private static MessageConsumer consumer;
    public static MessageConsumer getConsumer() {
        return consumer;
    }

    private static Session session;
    public static Session getSession() { return session; }

    private static Queue queue;
    private static SQSConnection connection;
    public static SQSConnection getConnection() {
        return connection;
    }

    public static void createConnect() throws JMSException {
        FileInputStream fis;
        Properties property = new Properties();
        try {
            fis = new FileInputStream("client/src/main/resources/config.properties");
            property.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        AWSCredentialsProvider credentials = new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return property.getProperty("AWS_ACCESS_KEY_ID");
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return property.getProperty("AWS_SECRET_ACCESS_KEY");
                    }
                };
            }

            @Override
            public void refresh() {

            }
        };

        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withCredentials(credentials)
                        .withEndpointConfiguration(new EndpointConfiguration(
                                "https://message-queue.api.cloud.yandex.net",
                                "ru-central1"))
        );

        connection = connectionFactory.createConnection();

        AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();

        if (!client.queueExists(queueName)) {
            System.out.println("QUEUE not exist");
            client.createQueue(queueName);
        }

        session = connection.createSession(false, UNORDERED_ACKNOWLEDGE);

        queue = session.createQueue(queueName);

        producer = session.createProducer(queue);

        consumer = session.createConsumer(queue);

    }

    public static void sendMsg(String text) throws JMSException {

        Message message = session.createTextMessage(text);
        producer.send(message);
    }

    public static String receiveMsg() throws JMSException {
        connection.start();
        Message message = consumer.receive(1000);
        System.out.println(((TextMessage) message).getText());
        // Cast the received message as TextMessage and print the text to screen. Also acknowledge the message.

        //connection.close();
        return ((TextMessage) message).getText();
    }
}
