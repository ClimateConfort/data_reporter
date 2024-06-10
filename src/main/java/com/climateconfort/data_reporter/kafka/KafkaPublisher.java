package com.climateconfort.data_reporter.kafka;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
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
    private final String schemaRegistryUrl;
    private final HttpClient httpClient;
    private final String kafkaConnectUrl;
    private final String kafkaConnectHdfsUrl;
    private final String kafkaConnectHadoopConfDir;
    private final String kafkaConnectHadoopHome;

    public KafkaPublisher(Properties properties) {
        this.clientId = Integer.parseInt(properties.getProperty("climateconfort.client_id", "NaN"));
        this.publisherIdList = Arrays
                .asList(properties
                        .getProperty("climateconfort.publishers")
                        .split(","));
        this.kafkaKey = properties.getProperty("kafka.key", "SensorData");
        this.schemaRegistryUrl = properties.getProperty("kafka.schema_registry.url");
        this.httpClient = HttpClient.newHttpClient();
        this.kafkaConnectUrl = properties.getProperty("kafka.connect.url");
        this.kafkaConnectHdfsUrl = properties.getProperty("kafka.connect.hdfs.url");
        this.kafkaConnectHadoopConfDir = properties.getProperty("kafka.connect.hadoop.conf.dir");
        this.kafkaConnectHadoopHome = properties.getProperty("kafka.connect.hadoop.home");
        String kafkaIp = properties.getProperty("kafka.ip", "localhost");
        String kafkaPort = properties.getProperty("kafka.port", "9092");

        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaIp + ":" + kafkaPort);
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        kafkaProperties.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        kafkaProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                Integer.parseInt(properties.getProperty("kafka.request.timeout.ms", "NaN")));

        kafkaProducer = new KafkaProducer<>(kafkaProperties);
    }

    public void createTopics() throws URISyntaxException, IOException, InterruptedException {
        String json = """
                {
                    "connector.class": "io.confluent.connect.hdfs.HdfsSinkConnector",
                    "tasks.max": "1",
                    "topics": "%s",
                    "hdfs.url": "%s",
                    "hadoop.conf.dir": "%s",
                    "hadoop.home": "%s",
                    "flush.size": "3",
                    "rotate.interval.ms": "1000",
                    "format.class":"io.confluent.connect.hdfs.avro.AvroFormat",
                    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
                    "value.converter":"io.confluent.connect.avro.AvroConverter",
                    "value.converter.schema.registry.url":"%s"
                }
                """;

        for (String publisherId : publisherIdList) {
            String topic = clientId + "." + publisherId.replace('-', '.');
            String requestBody = String.format(json, topic, kafkaConnectHdfsUrl, kafkaConnectHadoopConfDir,
                    kafkaConnectHadoopHome, schemaRegistryUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(kafkaConnectUrl))
                    .header("Content-Type", "application/json")
                    .PUT(BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String responseBody = response.body();
            LOGGER.info("Topic '{}' creation. Status code: {}, Response body: {}", topic, statusCode, responseBody);
        }
    }

    public void sendData(String topic, List<SensorDataAvro> sensorDataAvroList)
            throws InterruptedException, ExecutionException {
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
