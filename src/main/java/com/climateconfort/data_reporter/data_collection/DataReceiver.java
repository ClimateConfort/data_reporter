package com.climateconfort.data_reporter.data_collection;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class DataReceiver {

    static final String EXCHANGE_NAME = "sensor-data";

    private final ConnectionFactory factory;
    private final List<String> topics;

    private boolean isStop;

    DataReceiver(Properties properties, List<String> topics) {
        this.factory = new ConnectionFactory();
        this.factory.setHost(properties.getProperty("rabbitmq_server_ip", "localhost"));
        this.factory.setPort(Integer.parseInt(properties.getProperty("rabbitmq_server_port", "5672")));
        this.factory.setUsername(properties.getProperty("rabbitmq_server_user", "guest"));
        this.factory.setPassword(properties.getProperty("rabbitmq_server_password", "guest"));
        this.topics = topics;
        this.isStop = false;
    }

    public void subscribe() {
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String queueName = channel.queueDeclare().getQueue();

            for (String topic : topics) {
                channel.queueBind(queueName, EXCHANGE_NAME, topic);
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

    public class SensorDataConsumer extends DefaultConsumer {

        public SensorDataConsumer(Channel channel) {
            super(channel);
        }

        
    }
}
