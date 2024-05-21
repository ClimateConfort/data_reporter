package com.climateconfort.data_reporter.kafka;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaPublisher {
    private final String PROPERTIES_FILE = "application.properties";
    private final String TOPIC = "mi-topic";

    Properties properties = new Properties();
    Producer<String, String> kafkaPublisher;
    
    /*
     * 
     */
    public KafkaPublisher() throws FileNotFoundException, IOException
    {
        properties.load(new FileInputStream(PROPERTIES_FILE));
        // properties.put("bootstrap.servers", IP);
        properties.put("acks", "all");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        kafkaPublisher = new KafkaProducer<>(properties);
    }

    /**
     * @brief Kafka zerbitzari mezuaren sorrera eta bidalketa gauzatzen duen funtzioa
     */
    public void sendData() 
    {
        /* ajustar el mensaje a lo que queremos mandar */
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, "key", "value");
     
        /* Otra posible implementacion de la creacion del mensaje */
        // MyData data = new MyData();
        // data.setField1("valor1");
        // data.setField2(42);
        // String json = new Gson().toJson(data);

        kafkaPublisher.send(record);
    }

}
