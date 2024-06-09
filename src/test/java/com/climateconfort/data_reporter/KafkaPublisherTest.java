package com.climateconfort.data_reporter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

import com.climateconfort.data_reporter.avro.SensorDataAvro;
import com.climateconfort.data_reporter.kafka.KafkaPublisher;

class KafkaPublisherTest {

    @Mock
    Producer<String, SensorDataAvro> kafkaProducer;

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
        properties.setProperty("kafka.request.timeout.ms", "1000");
        properties.setProperty("kafka.schema_registry.url", "Hey, Listen!");
        return properties;
    }

    @SuppressWarnings("unchecked")
    @Test
    void sendDataTest() throws InterruptedException, ExecutionException {
        Future<RecordMetadata> mockFuture = mock(Future.class);
        RecordMetadata mockMetadata = mock(RecordMetadata.class);
        when(mockFuture.get()).thenReturn(mockMetadata);
        when(kafkaProducer.send(any())).thenReturn(mockFuture);
        when(mockMetadata.partition()).thenReturn(1);
        when(mockMetadata.offset()).thenReturn(1L);
        String topic = "test-topic";
        List<SensorDataAvro> packedData = new ArrayList<>();
        packedData.add(new SensorDataAvro());
        kafkaPublisher.sendData(topic, packedData);
        verify(kafkaProducer, times(packedData.size())).send(any());
    }

    @Test
    void closeTest() {
        kafkaPublisher.close();
        verify(kafkaProducer).close();
    }

    private <T, E> void setField(T target, String fieldName, E newValue)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, newValue);
    }
}
