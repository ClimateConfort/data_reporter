package com.climateconfort.data_reporter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.climateconfort.common.SensorData;
import com.climateconfort.data_reporter.actions.ActionSender;
import com.climateconfort.data_reporter.avro.AvroSerializer;
import com.climateconfort.data_reporter.cassandra.CassandraConnector;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.ParametroMota;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.Parametroa;
import com.climateconfort.data_reporter.data_collection.DataReceiver;
import com.climateconfort.data_reporter.kafka.KafkaPublisher;

public class Main {

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAX_DATA_PER_PACKAGE = 600;
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final int UPDATE_TIME_MIN = 1;
    private static final Properties COMPILATION_PROPERTIES = new Properties();
    private static final String PROGRAM_NAME = "data_reporter";
    private static final String PROGRAM_VERSION = "1.0.0";
    private static final String SEQUENTIAL_PROPERTY_NAME = "sequentialExecution";

    public static void main(String[] args) {
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("define.properties")) {
            if (input == null) {
                LOGGER.error("Unable to find 'define.properties' file");
                return;
            }
            COMPILATION_PROPERTIES.load(input);
        } catch (IOException e) {
            LOGGER.error("Failed access 'define.properties'", e);
        }

        if (Boolean.parseBoolean(
                COMPILATION_PROPERTIES.getProperty(SEQUENTIAL_PROPERTY_NAME))) {
            LOGGER.warn("Sequential Mode");
        }

        try {
            CommandLine cmd = parseArguments(args);
            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(PROGRAM_NAME, generateArgumentOptions());
                return;
            }

            if (cmd.hasOption("v")) {
                LOGGER.info("Version: {}", PROGRAM_VERSION);
                return;
            }

            if (!cmd.hasOption("p")) {
                LOGGER.error("No valid options provided. Use -h for help.");
                return;
            }

            Main main = new Main(Paths.get(cmd.getOptionValue("p")));
            main.setup(new Scanner(System.in));
            main.start();
        } catch (ParseException e) {
            LOGGER.error("Error parsing command line arguments", e);
        } catch (Exception e) {
            LOGGER.error("Unknown error", e);
        }
    }

    private static Options generateArgumentOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Show help");
        options.addOption("v", "version", false, "Show version");
        options.addOption("p", "properties", true, "Properties file path");
        return options;
    }

    private static CommandLine parseArguments(String[] args) throws ParseException {
        Options argOptions = generateArgumentOptions();
        CommandLineParser parser = new DefaultParser();
        return parser.parse(argOptions, args);
    }

    private final ActionSender actionSender;
    private final CassandraConnector cassandraConnector;
    private final DataReceiver dataReceiver;
    private final ExecutorService executorService;
    private final KafkaPublisher kafkaPublisher;

    private final ReadWriteLock readWriteLock;
    private boolean isStop;
    private Map<Long, Map<Long, List<Parametroa>>> valueMap;

    public Main(Path propertiesPath) throws IOException {
        Properties properties = new Properties();
        try (BufferedReader bufferedReader = Files.newBufferedReader(propertiesPath)) {
            properties.load(bufferedReader);
        }
        this.actionSender = new ActionSender(properties);
        this.cassandraConnector = new CassandraConnector(properties);
        this.dataReceiver = new DataReceiver(properties);
        this.executorService = Executors.newWorkStealingPool(THREAD_COUNT);
        this.kafkaPublisher = new KafkaPublisher(properties);
        this.readWriteLock = new ReentrantReadWriteLock(true);
        this.isStop = false;
    }

    public void setup(Scanner scanner) {
        LOGGER.info("Setting Up...");
        Thread subscriberThread = new Thread(() -> {
            try {
                dataReceiver.subscribe();
            } catch (IOException | TimeoutException | InterruptedException e) {
                LOGGER.error("Subscriber Thread Interrupted", e);
                Thread.currentThread().interrupt();
            }
        });

        Thread waitThread = new Thread(() -> {
            scanner.nextLine();
            cassandraConnector.close();
            kafkaPublisher.close();
            dataReceiver.stop();
            isStop = true;
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                LOGGER.error("Executor Service Error", e);
                Thread.currentThread().interrupt();
            }
        });

        // kafkaPublisher.createTopics();
        subscriberThread.start();
        waitThread.start();
        LOGGER.info("Setting Up... - done");
    }

    public void start() {
        long totalMilisecs = 0;
        Map<Long, Map<Long, List<SensorData>>> sensorDataMap = new HashMap<>();
        sequentialUpdateValues();
        while (!isStop) {
            long start = System.currentTimeMillis();
            dataReceiver
                    .getSensorData()
                    .ifPresent(sensorData -> {
                        long buildingId = sensorData.getBuildingId();
                        long roomId = sensorData.getRoomId();
                        sensorDataMap
                                .computeIfAbsent(buildingId, k -> new HashMap<>())
                                .computeIfAbsent(roomId, k -> new ArrayList<>());
                        sensorDataMap
                                .get(buildingId)
                                .computeIfPresent(roomId,
                                        (key, dataList) -> {
                                            if (Boolean.parseBoolean(
                                                    COMPILATION_PROPERTIES.getProperty(SEQUENTIAL_PROPERTY_NAME))) {
                                                return sequentialProgramLogic(sensorData, dataList);
                                            }
                                            return concurrentProgramLogic(sensorData, dataList);
                                        });
                    });

            totalMilisecs += System.currentTimeMillis() - start;
            if (totalMilisecs >= TimeUnit.MINUTES.toMillis(UPDATE_TIME_MIN)) {
                totalMilisecs = 0;
                if (Boolean.parseBoolean(COMPILATION_PROPERTIES.getProperty(SEQUENTIAL_PROPERTY_NAME))) {
                    sequentialUpdateValues();
                } else {
                    concurrentUpdateValues();
                }
            }
        }
    }

    private Map<Long, Map<Long, List<Parametroa>>> getValueMap() {
        readWriteLock.readLock().lock();
        try {
            return valueMap;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private List<SensorData> concurrentProgramLogic(SensorData sensorData, List<SensorData> dataList) {
        // CountDownLatch bat behar da, listak ez duelako denbora nahikorik
        // kopiatzeko.
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        if (dataList.size() >= MAX_DATA_PER_PACKAGE) {
            executorService.execute(() -> {
                List<SensorData> copySensorDataList = new ArrayList<>(dataList);
                countDownLatch.countDown(); // dataList-aren kopia egin dela abixatu
                try {
                    packAndPublish(sensorData, copySensorDataList);
                } catch (IOException | InterruptedException | ExecutionException e) {
                    LOGGER.error("Kafka Sender Thread Interrupted", e);
                    Thread.currentThread().interrupt();
                }
            });
            executorService.execute(() -> {
                List<SensorData> copySensorDataList = new ArrayList<>(dataList);
                countDownLatch.countDown(); // dataList-aren kopia egin dela abixatu
                try {
                    concurrentTakeAction(sensorData.getBuildingId(), sensorData.getRoomId(), copySensorDataList);
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Action Taking Thread Interrupted", e);
                    Thread.currentThread().interrupt();
                }
            });
            try {
                countDownLatch.await(); // Itxoin kopia guztiak egitea
            } catch (InterruptedException e) {
                LOGGER.error("Action Taking Thread Interrupted", e);
                Thread.currentThread().interrupt();
            }
            dataList.clear();
        }
        dataList.add(sensorData);
        return dataList;
    }

    private List<SensorData> sequentialProgramLogic(SensorData sensorData, List<SensorData> dataList) {
        if (dataList.size() >= MAX_DATA_PER_PACKAGE) {
            try {
                packAndPublish(sensorData, dataList);
            } catch (IOException | InterruptedException | ExecutionException e) {
                LOGGER.error("Kafka sender interrupted", e);
                Thread.currentThread().interrupt();
            }
            sequentialTakeAction(sensorData.getBuildingId(), sensorData.getRoomId(), dataList);
            dataList.clear();
        }
        dataList.add(sensorData);
        return dataList;
    }

    private void concurrentTakeAction(long buildingId, long roomId, List<SensorData> dataList)
            throws InterruptedException, ExecutionException {
        final int threshold = Math.max(1, dataList.size() / (THREAD_COUNT * 2));
        List<Callable<List<Integer>>> tasks = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i += threshold) {
            int start = i;
            int end = Math.min(i + threshold, dataList.size());
            tasks.add(() -> {
                int temperature = 0;
                int soundLevel = 0;
                int humidity = 0;
                int pressure = 0;
                for (int j = start; j < end; j++) {
                    temperature += dataList.get(j).getTemperature();
                    soundLevel += dataList.get(j).getSoundLevel();
                    humidity += dataList.get(j).getHumidity();
                    pressure += dataList.get(j).getPressure();
                }
                return Arrays.asList(temperature, soundLevel, humidity, pressure);
            });
        }

        List<Future<List<Integer>>> futures = executorService.invokeAll(tasks);

        // Sum up the results from all tasks
        int temperatureMean = 0;
        int soundLevelMean = 0;
        int humidityMean = 0;
        int pressureMean = 0;
        for (Future<List<Integer>> future : futures) {
            List<Integer> results = future.get();
            temperatureMean += results.get(0);
            soundLevelMean += results.get(1);
            humidityMean += results.get(2);
            pressureMean += results.get(3);
        }

        temperatureMean /= dataList.size();
        soundLevelMean /= dataList.size();
        humidityMean /= dataList.size();
        pressureMean /= dataList.size();

        performAction(buildingId, roomId, temperatureMean, soundLevelMean, humidityMean, pressureMean);
    }

    private void packAndPublish(SensorData sensorData, List<SensorData> copySensorDataList)
            throws IOException, InterruptedException, ExecutionException {
        try (ByteArrayOutputStream avroFileStream = AvroSerializer.packToAvroFile(copySensorDataList)) {
            String queue = String.format("%d.%d.%d", sensorData.getClientId(), sensorData.getBuildingId(),
                    sensorData.getRoomId());
            RecordMetadata metadata = kafkaPublisher.sendData(queue, avroFileStream.toByteArray()).get();
            LOGGER.info("Data from Client {}, Building {}, Room: {} has been published. File size: {} bytes",
                    sensorData.getClientId(), sensorData.getBuildingId(), sensorData.getRoomId(),
                    avroFileStream.size());
            LOGGER.debug("Data sent to Partition: '{}', Offset: {}", metadata.partition(), metadata.offset());
        }
    }

    private void sequentialTakeAction(long buildingId, long roomId, List<SensorData> dataList) {
        int temperatureMean = 0;
        int soundLevelMean = 0;
        int humidityMean = 0;
        int pressureMean = 0;

        for (SensorData sensorData : dataList) {
            temperatureMean += sensorData.getTemperature();
            soundLevelMean += sensorData.getSoundLevel();
            humidityMean += sensorData.getHumidity();
            pressureMean += sensorData.getPressure();
        }

        temperatureMean /= dataList.size();
        soundLevelMean /= dataList.size();
        humidityMean /= dataList.size();
        pressureMean /= dataList.size();

        performAction(buildingId, roomId, temperatureMean, soundLevelMean, humidityMean, pressureMean);
    }

    private void performAction(long buildingId, long roomId, int temperatureMean, int soundLevelMean, int humidityMean,
            int pressureMean) {
        try {
            getValueMap()
                .get(buildingId)
                .get(roomId)
                .forEach(parameter -> {
                    String action = "";
                    switch (parameter.getMota()) {
                        case ParametroMota.TEMPERATURE:
                            action = actionCalculate(parameter, temperatureMean, "temperature",
                                    parameter.isMinimoaDu());
                            break;
                        case ParametroMota.SOUND_LEVEL:
                            action = actionCalculate(parameter, soundLevelMean, "sound", parameter.isMinimoaDu());
                            break;
                        case ParametroMota.HUMIDITY:
                            action = actionCalculate(parameter, humidityMean, "humidity", parameter.isMinimoaDu());
                            break;
                        case ParametroMota.PRESSURE:
                            action = actionCalculate(parameter, pressureMean, "pressure", parameter.isMinimoaDu());
                            break;
                        default:
                            throw new UnsupportedOperationException("Unsupported action: " + parameter.getMota());
                    }
                    try {
                        if (!action.isEmpty()) {
                            actionSender.publish(roomId, buildingId, action);
                            LOGGER.info("Action '{}' published to Building: {}, Room: {}", action, buildingId, roomId);
                        }
                    } catch (IOException | TimeoutException e) {
                        LOGGER.error("Action publishing error", e);
                    }
                });
        } catch (NullPointerException e) {
            LOGGER.error("Not existing BuildingId: {} or RoomId: {}", buildingId, roomId, e);
        }
    }

    private String actionCalculate(Parametroa parameter, int valueMean, String parameterName, boolean hasMinimum) {
        String action = "";
        if (hasMinimum && parameter.getBalioMin() > valueMean) {
            action = "Raise the " + parameterName;
        }
        if (parameter.getBalioMax() < valueMean) {
            action = "Lower the " + parameterName;
        }
        return action;
    }

    private void concurrentUpdateValues() {
        executorService.execute(() -> {
            readWriteLock.writeLock().lock();
            try {
                sequentialUpdateValues();
            } finally {
                readWriteLock.writeLock().unlock();
            }
        });
    }

    private void sequentialUpdateValues() {
        LOGGER.info("Retrieving data from cassandra...");
        valueMap = cassandraConnector.getParameters();
        LOGGER.info("Retrieving data from cassandra... - done");
    }
}