package com.climateconfort.data_reporter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.climateconfort.common.Constants;
import com.climateconfort.common.SensorData;
import com.climateconfort.data_reporter.data_collection.DataReceiver;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;

public class DataReceiverTest {

    private class IncorrectClass implements Serializable {
        @SuppressWarnings("unused")
        private String field;

        public IncorrectClass(String field) {
            this.field = field;
        }
    }

    private static final int clientId = 1;

    private static final String QUEUE_NAME = "queue-string";

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Connection connection;

    @Mock
    private Channel channel;

    @Mock
    private DeclareOk declareOk;

    @Mock
    private Properties properties;
    private DataReceiver dataReceiver;
    private List<String> publisherIdList;

    private SensorData sensorData;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);
        when(channel.queueDeclare()).thenReturn(declareOk);
        when(declareOk.getQueue()).thenReturn("queue-string");
        publisherIdList = new ArrayList<>();
        populatePublisherId();
        dataReceiver = new DataReceiver(getProperties(), publisherIdList);
        sensorData = new SensorData(-1, -1, -1, clientId, -1, -1, -1, -1, -1, -1);
        setField(dataReceiver, "connectionFactory", connectionFactory);
    }

    @Test
    void subscribeTest() throws InterruptedException, IOException, TimeoutException {
        (new Thread(() -> {
            try {
                dataReceiver.subscribe();
            } catch (IOException | TimeoutException | InterruptedException e) {
                throw new RuntimeException("Runtime error!");
            }
        })).start();
        Thread.sleep(1000);
        dataReceiver.stop();
        verify(connectionFactory).newConnection();
        verify(connection).createChannel();
        verify(channel).exchangeDeclare(Constants.SENSOR_EXCHANGE_NAME, "direct");

        for (String publisherId : publisherIdList) {
            verify(channel).queueBind(QUEUE_NAME, Constants.SENSOR_EXCHANGE_NAME, publisherId);
        }
        verify(channel).basicCancel(null);
    }

    @Test
    void getSensorDataTest()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ConcurrentLinkedQueue<SensorData> dataQueue = new ConcurrentLinkedQueue<>();
        dataQueue.add(sensorData);
        setField(dataReceiver, "dataQueue", dataQueue);
        assertEquals(sensorData, dataReceiver.getSensorData().get());
    }

    @Test
    void handleDeliveryCorrectTest() throws IOException {
        byte[] body = serialize(sensorData);

        DataReceiver.SensorDataConsumer consumer = dataReceiver.new SensorDataConsumer(channel);

        consumer.handleDelivery("consumerTag", mock(Envelope.class), mock(BasicProperties.class), body);

        Optional<SensorData> result = dataReceiver.getSensorData();
        assertTrue(result.isPresent());
        assertEquals(sensorData.getUnixTime(), result.get().getUnixTime());
        assertEquals(sensorData.getRoomId(), result.get().getRoomId());
        assertEquals(sensorData.getBuildingId(), result.get().getBuildingId());
        assertEquals(sensorData.getClientId(), result.get().getClientId());
        assertEquals(sensorData.getTemperature(), result.get().getTemperature());
        assertEquals(sensorData.getLightLevel(), result.get().getLightLevel());
        assertEquals(sensorData.getAirQuality(), result.get().getAirQuality());
        assertEquals(sensorData.getSoundLevel(), result.get().getSoundLevel());
        assertEquals(sensorData.getHumidity(), result.get().getHumidity());
        assertEquals(sensorData.getPressure(), result.get().getPressure());
    }

    @Test()
    void handleDeliveryFailTest() throws IOException {
        DataReceiver.SensorDataConsumer consumer = dataReceiver.new SensorDataConsumer(channel);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        // Write the stream header for an object stream
        dataOutputStream.writeShort(ObjectStreamConstants.STREAM_MAGIC);
        dataOutputStream.writeShort(ObjectStreamConstants.STREAM_VERSION);

        // Write a TC_OBJECT (0x73) marker
        dataOutputStream.writeByte(ObjectStreamConstants.TC_OBJECT);

        // Write a TC_CLASSDESC (0x72) marker
        dataOutputStream.writeByte(ObjectStreamConstants.TC_CLASSDESC);

        // Write a non-existent class name
        dataOutputStream.writeUTF("com.nonexistent.NonExistentClass");

        // Write class serial version UID
        dataOutputStream.writeLong(1L);

        // Write the class descriptor flags
        dataOutputStream.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);
        
        // Write the field count (0 for simplicity)
        dataOutputStream.writeShort(0);

        // Write a TC_ENDBLOCKDATA (0x78) marker
        dataOutputStream.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);

        // Write a TC_NULL (0x70) marker
        dataOutputStream.writeByte(ObjectStreamConstants.TC_NULL);

        
        assertDoesNotThrow(() -> {
            consumer.handleDelivery("consumerTag", mock(Envelope.class), mock(BasicProperties.class),
                byteArrayOutputStream.toByteArray());
        });
    }

    private void populatePublisherId() {
        publisherIdList.add("1-1");
        publisherIdList.add("1-2");
        publisherIdList.add("1-3");
        publisherIdList.add("1-4");
        publisherIdList.add("1-5");
        publisherIdList.add("1-6");
        publisherIdList.add("1-7");
        publisherIdList.add("1-8");
        publisherIdList.add("1-9");
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("client_id", String.valueOf(clientId));
        return properties;
    }

    private <T, E> void setField(T target, String fieldName, E newValue)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, newValue);
    }

    private <T> byte[] serialize(T obj) throws IOException {
        try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return bos.toByteArray();
        }
    }
}
