package com.climateconfort.data_reporter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.climateconfort.data_reporter.data_collection.DataReceiver;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
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