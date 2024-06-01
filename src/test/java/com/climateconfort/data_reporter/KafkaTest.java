package com.climateconfort.data_reporter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.Producer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.climateconfort.data_reporter.kafka.KafkaPublisher;

public class KafkaTest 
{
    KafkaPublisher kafkaPublisher;
    Properties mockProperties;

    @Mock
    Producer<String, String> mockKafkaProducer;

    @BeforeEach
    void setUp() throws FileNotFoundException, IOException
    {
        MockitoAnnotations.openMocks(this);

        mockProperties = new Properties();
        mockProperties.setProperty("kafka_broker_ip", "localhost");
        mockProperties.setProperty("kafka_broker_port", "9092");

        kafkaPublisher = new KafkaPublisher(mockProperties);
    }

    @Test
    public void sendDataTest()
    {
        String topic = "test-topic";
        String payload = "test-payload";

        kafkaPublisher.sendData(topic, payload);
    }

}
