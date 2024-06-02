package com.climateconfort.data_reporter;

import static org.mockito.ArgumentMatchers.any;
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
    Producer<String, String> kafkaProducer;

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

    @SuppressWarnings("unchecked")
    @Test
    void sendDataTest() {
        String topic = "test-topic";
        String payload = "test-payload";
        kafkaPublisher.sendData(topic, payload);
        verify(kafkaProducer).send(any(ProducerRecord.class));
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("kafka_broker_ip", "localhost");
        properties.setProperty("kafka_broker_port", "9092");
        return properties;
    }

    private <T, E> void setField(T target, String fieldName, E newValue)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, newValue);
    }
}
