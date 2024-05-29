package com.climateconfort.data_reporter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import com.climateconfort.common.SensorData;
import com.climateconfort.data_reporter.avro.AvroSerializer;

import com.climateconfort.data_reporter.data_collection.DataReceiver;

public class Main {
    public static void main(String[] args) throws IOException {
        List<SensorData> sensorDatas = new ArrayList<>();

        Random random = new Random();

        for (int i = 0; i < 10000; i++) {
            sensorDatas.add(new SensorData(random.nextLong(), 1, random.nextLong(), random.nextLong(),
                    random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat(),
                    random.nextFloat()));
        }

        var stream = AvroSerializer.packToAvroFile(sensorDatas);
        FileOutputStream fileOutputStream = new FileOutputStream("data.avro");
        fileOutputStream.write(stream.toByteArray());
        fileOutputStream.close();
      
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/main/resources/application.properties"));
        List<String> publisherIdList = new ArrayList<>();
        publisherIdList.add("1-1");
        DataReceiver dataReceiver = new DataReceiver(properties, publisherIdList);

        (new Thread(() -> {
            try {
                dataReceiver.subscribe();
            } catch (IOException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }
        })).start();

        while (true) {
            dataReceiver.getSensorData().ifPresent(System.out::println);
        }
    }
}