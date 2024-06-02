package com.climateconfort.data_reporter.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaPublisher {
    private static final String KEY = "SensorData";

    private Properties kafkaProperties;
    private Producer<String, String> kafkaProducer;

    public KafkaPublisher(Properties properties) {
        kafkaProperties = new Properties();

        String kafkaIP = properties.getProperty("kafka_broker_ip");
        String kafkaPort = properties.getProperty("kafka_broker_port");

        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaIP + ":" + kafkaPort);
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        kafkaProducer = new KafkaProducer<>(kafkaProperties);
    }

    public void sendData(String topic, String payload) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, KEY, payload);

        kafkaProducer.send(producerRecord);
    }

}
