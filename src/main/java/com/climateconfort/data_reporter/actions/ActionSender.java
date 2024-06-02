package com.climateconfort.data_reporter.actions;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.climateconfort.common.Constants;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ActionSender {

    private final ConnectionFactory connectionFactory;

    public ActionSender(Properties properties) {
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(properties.getProperty("rabbitmq.server.ip", "localhost"));
        this.connectionFactory.setPort(Integer.parseInt(properties.getProperty("rabbitmq.server.port", "5672")));
        this.connectionFactory.setUsername(properties.getProperty("rabbitmq.server.user", "guest"));
        this.connectionFactory.setPassword(properties.getProperty("rabbitmq.server.password", "guest"));
    }

    public void publish(long roomId, long buildingId, String action) throws IOException, TimeoutException {
        try (Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(Constants.SENSOR_ACTION_EXCHANGE, "topic");
            channel.basicPublish(Constants.SENSOR_ACTION_EXCHANGE, String.format(buildingId + "." + roomId), null,
                    action.getBytes());
        }
    }
}