package com.climateconfort.data_reporter.heartbeat;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.climateconfort.common.Constants;
import com.climateconfort.data_reporter.TlsManager;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class HeartbeatSender {
    
    private static final Logger LOGGER = LogManager.getLogger(HeartbeatSender.class);
    private static final String MESSAGE = "heartbeat";
    private final ConnectionFactory connectionFactory;

    public HeartbeatSender(Properties properties) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        TlsManager tlsManager = new TlsManager(properties);
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(properties.getProperty("rabbitmq.server.ip", "localhost"));
        this.connectionFactory.setPort(Integer.parseInt(properties.getProperty("rabbitmq.server.port", "5672")));
        this.connectionFactory.setUsername(properties.getProperty("rabbitmq.server.user", "guest"));
        this.connectionFactory.setPassword(properties.getProperty("rabbitmq.server.password", "guest"));
        this.connectionFactory.useSslProtocol(tlsManager.getSslContext());
    }

    public void publish() throws IOException, TimeoutException {
        try (Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(Constants.HEARTBEAT_EXCHANGE, "fanout");
            channel.basicPublish(Constants.HEARTBEAT_EXCHANGE, "", null, MESSAGE.getBytes());
            LOGGER.info("Heartbeat sent");
        }
    }

}
