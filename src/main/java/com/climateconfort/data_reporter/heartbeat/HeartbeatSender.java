package com.climateconfort.data_reporter.heartbeat;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.climateconfort.common.Constants;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class HeartbeatSender {
    
    private static final String MESSAGE = "heartbeat";
    private final ConnectionFactory connectionFactory;

    public HeartbeatSender(Properties properties) {
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(properties.getProperty("rabbitmq_server_ip", "localhost"));
        this.connectionFactory.setPort(Integer.parseInt(properties.getProperty("rabbitmq_server_port", "5672")));
        this.connectionFactory.setUsername(properties.getProperty("rabbitmq_server_user", "guest"));
        this.connectionFactory.setPassword(properties.getProperty("rabbitmq_server_password", "guest"));
    }

    public void publish() throws IOException, TimeoutException {
        try (Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(Constants.HEARTBEAT_EXCHANGE, "fanout");
            channel.basicPublish(Constants.HEARTBEAT_EXCHANGE, "", null, MESSAGE.getBytes());
            System.out.println("message sent: " + MESSAGE);
        }
    }

}
