package com.climateconfort.data_reporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.climateconfort.common.Constants;
import com.climateconfort.data_reporter.actions.ActionSender;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ActionSenderTest {

    private static final int roomId = 1;
    private static final int buildingId = 1;

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Connection connection;

    @Mock
    private Channel channel;

    @Captor
    private ArgumentCaptor<byte[]> byteArrayCaptor;

    private ActionSender actionSender;

    @BeforeEach
    void setUp() throws IOException, TimeoutException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);
        actionSender = new ActionSender(getProperties());
        setField(actionSender, "connectionFactory", connectionFactory);
    }

    @Test
    void publishTest() throws IOException, TimeoutException {
        actionSender.publish(roomId, buildingId, "Action1");
        verify(connectionFactory).newConnection();
        verify(connection).createChannel();
        verify(channel).exchangeDeclare(eq(Constants.SENSOR_ACTION_EXCHANGE), eq("topic"));
        verify(channel).basicPublish(eq(Constants.SENSOR_ACTION_EXCHANGE), eq(String.format(buildingId + "." + roomId)),
                isNull(),
                byteArrayCaptor.capture());
        
        byte[] capturedArray = byteArrayCaptor.getValue();
        String action = new String(capturedArray);
        assertEquals(action, "Action1");
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("room_id", String.valueOf(roomId));
        properties.setProperty("building_id", String.valueOf(buildingId));
        return properties;
    }

    private <T, E> void setField(T target, String fieldName, E newValue)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, newValue);
    }
}
