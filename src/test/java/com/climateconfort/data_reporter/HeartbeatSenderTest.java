package com.climateconfort.data_reporter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

import com.climateconfort.common.Constants;
import com.climateconfort.data_reporter.heartbeat.HeartbeatSender;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

class HeartbeatSenderTest {

    @Mock
    ConnectionFactory connectionFactory;

    HeartbeatSender heartbeatSender;

    @BeforeEach
    void setUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        MockitoAnnotations.openMocks(this);
        try (MockedConstruction<TlsManager> mockedConstruction = mockConstruction(TlsManager.class, (mock, context) -> when(mock.getSslContext()).thenReturn(mock(SSLContext.class)))) {
            heartbeatSender = new HeartbeatSender(new Properties());
        }
        setField(heartbeatSender, "connectionFactory", connectionFactory);
    }

    @Test
    void publishTest() throws IOException, TimeoutException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Connection connection = mock(Connection.class);
        Channel channel = mock(Channel.class);
        when(connectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);
        heartbeatSender.publish();
        verify(channel).exchangeDeclare(Constants.HEARTBEAT_EXCHANGE, "fanout");
        verify(channel).basicPublish(Constants.HEARTBEAT_EXCHANGE, "", null, ((String) setFieldPublic(HeartbeatSender.class, "MESSAGE").get(null)).getBytes());
    }

    private <T, E> void setField(T target, String fieldName, E newValue)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, newValue);
    }

    private <T> Field setFieldPublic(Class<T> target, String fieldName)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = target.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
