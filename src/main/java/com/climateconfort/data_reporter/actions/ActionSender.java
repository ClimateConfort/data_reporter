package com.climateconfort.data_reporter.actions;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.climateconfort.common.Constants;
import com.climateconfort.data_reporter.TlsManager;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ActionSender {

    private final ConnectionFactory connectionFactory;

    public ActionSender(Properties properties) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        TlsManager tlsManager = new TlsManager(properties);
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(properties.getProperty("rabbitmq.server.ip", "localhost"));
        this.connectionFactory.setPort(Integer.parseInt(properties.getProperty("rabbitmq.server.port", "5672")));
        this.connectionFactory.setUsername(properties.getProperty("rabbitmq.server.user", "guest"));
        this.connectionFactory.setPassword(properties.getProperty("rabbitmq.server.password", "guest"));
        this.connectionFactory.useSslProtocol(tlsManager.getSslContext());
    }

    public void publish(long roomId, long buildingId, String action) throws IOException, TimeoutException {
        try (Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(Constants.SENSOR_ACTION_EXCHANGE, "direct");
            channel.basicPublish(Constants.SENSOR_ACTION_EXCHANGE, String.format("%d.%d", buildingId, roomId), null,
                    action.getBytes());
        }
    }
}