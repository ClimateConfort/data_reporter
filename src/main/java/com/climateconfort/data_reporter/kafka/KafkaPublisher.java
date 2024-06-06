package com.climateconfort.data_reporter.kafka;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaPublisher implements AutoCloseable {
    private final int clientId;
    private final List<String> publisherIdList;
    private final Producer<String, byte[]> kafkaProducer;
    private final String kafkaKey;

    public KafkaPublisher(Properties properties) {
        this.clientId = Integer.parseInt(properties.getProperty("climateconfort.client_id", "NaN"));
        this.publisherIdList = Arrays
                .asList(properties
                        .getProperty("climateconfort.publishers")
                        .split(","));
        this.kafkaKey = properties.getProperty("kafka.key", "SensorData");
        String kafkaIp = properties.getProperty("kafka.ip", "localhost");
        String kafkaPort = properties.getProperty("kafka.port", "9092");

        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaIp + ":" + kafkaPort);
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        kafkaProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, Integer.parseInt(properties.getProperty("kafka.request.timeout.ms", "NaN")));

        kafkaProducer = new KafkaProducer<>(kafkaProperties);
    }

    public void createTopics() {
        throw new UnsupportedOperationException("'KafkaPublisher::createTopics' not implemented!");
    }

    public Future<RecordMetadata> sendData(String topic, byte[] payload) {
        return kafkaProducer.send(new ProducerRecord<>(topic, kafkaKey, payload));
    }

    @Override
    public void close() {
        kafkaProducer.close();
    }
}
