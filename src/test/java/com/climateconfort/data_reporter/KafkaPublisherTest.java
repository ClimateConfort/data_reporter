package com.climateconfort.data_reporter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

import com.climateconfort.data_reporter.kafka.KafkaPublisher;

class KafkaPublisherTest {

    @Mock
    Producer<String, byte[]> kafkaProducer;

    KafkaPublisher kafkaPublisher;

    @BeforeEach
    void setUp() throws FileNotFoundException, IOException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        try (@SuppressWarnings("rawtypes")
        MockedConstruction<KafkaProducer> mockedConstruction = mockConstruction(KafkaProducer.class)) {
            kafkaPublisher = new KafkaPublisher(getProperties());
            setField(kafkaPublisher, "kafkaProducer", kafkaProducer);
        }

    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("climateconfort.client_id", "1");
        properties.setProperty("climateconfort.publishers", "1-1,1-2");
        return properties;
    }

    @SuppressWarnings("unchecked")
    @Test
    void sendDataTest() {
        String topic = "test-topic";
        byte[] payload = "test-payload".getBytes();
        kafkaPublisher.sendData(topic, payload);
        verify(kafkaProducer).send(any(ProducerRecord.class));
    }

    private <T, E> void setField(T target, String fieldName, E newValue)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, newValue);
    }
}
