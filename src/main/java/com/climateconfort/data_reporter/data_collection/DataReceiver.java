package com.climateconfort.data_reporter.data_collection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import com.climateconfort.common.SensorData;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

public class DataReceiver {

    static final String EXCHANGE_NAME = "sensor-data";

    private final long clientId;
    private final ConnectionFactory factory;
    private final List<String> publisherIdList;
    private final Queue<SensorData> dataQueue;

    private boolean isStop;

    public DataReceiver(Properties properties, List<String> publisherIdList) throws NumberFormatException {  // PublisherID: String = String(buildingId) + "-" + String(roomId);
        this.clientId = Integer.parseInt(properties.getProperty("client_id", "NaN"));
        this.factory = new ConnectionFactory();
        this.factory.setHost(properties.getProperty("rabbitmq_server_ip", "localhost"));
        this.factory.setPort(Integer.parseInt(properties.getProperty("rabbitmq_server_port", "5672")));
        this.factory.setUsername(properties.getProperty("rabbitmq_server_user", "guest"));
        this.factory.setPassword(properties.getProperty("rabbitmq_server_password", "guest"));
        this.publisherIdList = publisherIdList;
        this.dataQueue = new ConcurrentLinkedQueue<>();
        this.isStop = false;
    }

    public void subscribe() {
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            String queueName = channel.queueDeclare().getQueue();

            for (String publisherId : publisherIdList) {
                channel.queueBind(queueName, EXCHANGE_NAME, publisherId);
            }

            SensorDataConsumer sensorDataConsumer = new SensorDataConsumer(channel);
            String tag = channel.basicConsume(queueName, true, sensorDataConsumer);
            synchronized (this) {
                while (!isStop) {
                    this.wait();
                }
            }
            channel.basicCancel(tag);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void stop() {
        isStop = true;
        this.notifyAll();
    }

    public Optional<SensorData> getSensorData() {
        Optional<SensorData> sensorData = Optional.ofNullable(dataQueue.poll());
        sensorData.ifPresent(data -> data.setClientId(clientId));
        return sensorData;
    }

    public class SensorDataConsumer extends DefaultConsumer {

        public SensorDataConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
                throws IOException {
            try {
                ObjectInputStream inputObject = new ObjectInputStream(new ByteArrayInputStream(body));
                SensorData sensorData = (SensorData) inputObject.readObject();
                dataQueue.add(sensorData);
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
