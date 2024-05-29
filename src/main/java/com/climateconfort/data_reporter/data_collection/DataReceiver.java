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

import com.climateconfort.common.Constants;
import com.climateconfort.common.SensorData;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

public class DataReceiver {

    private final long clientId;
    private final ConnectionFactory connectionFactory;
    private final List<String> publisherIdList;
    private final Queue<SensorData> dataQueue;

    private boolean isStop;

    // PublisherID: String = String(buildingId) + "-" + String(roomId);
    public DataReceiver(Properties properties, List<String> publisherIdList) throws NumberFormatException {
        this.clientId = Integer.parseInt(properties.getProperty("client_id", "NaN"));
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(properties.getProperty("rabbitmq_server_ip", "localhost"));
        this.connectionFactory.setPort(Integer.parseInt(properties.getProperty("rabbitmq_server_port", "5672")));
        this.connectionFactory.setUsername(properties.getProperty("rabbitmq_server_user", "guest"));
        this.connectionFactory.setPassword(properties.getProperty("rabbitmq_server_password", "guest"));
        this.publisherIdList = publisherIdList;
        this.dataQueue = new ConcurrentLinkedQueue<>();
        this.isStop = false;
    }

    public void subscribe() throws IOException, TimeoutException, InterruptedException {
        try (Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(Constants.SENSOR_EXCHANGE_NAME, "direct");
            String queueName = channel.queueDeclare().getQueue();

            for (String publisherId : publisherIdList) {
                channel.queueBind(queueName, Constants.SENSOR_EXCHANGE_NAME, publisherId);
            }

            SensorDataConsumer sensorDataConsumer = new SensorDataConsumer(channel);
            String tag = channel.basicConsume(queueName, true, sensorDataConsumer);
            synchronized (this) {
                while (!isStop) {
                    this.wait();
                }
            }
            channel.basicCancel(tag);
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
            ObjectInputStream inputObject = new ObjectInputStream(new ByteArrayInputStream(body));
            try {
                SensorData sensorData = (SensorData) inputObject.readObject();
                dataQueue.add(sensorData);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
