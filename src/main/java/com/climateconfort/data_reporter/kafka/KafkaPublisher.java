package com.climateconfort.data_reporter.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaPublisher {
    private final Producer<String, byte[]> kafkaProducer;
    private final String kafkaKey;

    public KafkaPublisher(Properties properties) {
        this.kafkaKey = properties.getProperty("kafka.key");
        String kafkaIp = properties.getProperty("kafka.ip");
        String kafkaPort = properties.getProperty("kafka.port");
        
        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaIp + ":" + kafkaPort);
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        kafkaProducer = new KafkaProducer<>(kafkaProperties);
    }

    public void sendData(String topic, byte[] payload) {
        kafkaProducer.send(new ProducerRecord<>(topic, kafkaKey, payload));
    }

}
