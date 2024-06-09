package com.climateconfort.data_reporter.kafka;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.climateconfort.data_reporter.avro.SensorDataAvro;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

public class KafkaPublisher implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger(KafkaPublisher.class);
    private final int clientId;
    private final List<String> publisherIdList;
    private final Producer<String, SensorDataAvro> kafkaProducer;
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
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        kafkaProperties.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, properties.getProperty("kafka.schema_registry.url"));
        kafkaProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, Integer.parseInt(properties.getProperty("kafka.request.timeout.ms", "NaN")));

        kafkaProducer = new KafkaProducer<>(kafkaProperties);
    }

    public void createTopics() {
        throw new UnsupportedOperationException("'KafkaPublisher::createTopics' not implemented!");
    }

    public void sendData(String topic, List<SensorDataAvro> sensorDataAvroList) throws InterruptedException, ExecutionException {
        for (SensorDataAvro sensorDataAvro : sensorDataAvroList) {
            RecordMetadata metadata = kafkaProducer.send(new ProducerRecord<>(topic, kafkaKey, sensorDataAvro)).get();
            LOGGER.debug("Data sent to Partition: '{}', Offset: {}", metadata.partition(), metadata.offset());
        }
    }

    @Override
    public void close() {
        kafkaProducer.close();
    }
}
