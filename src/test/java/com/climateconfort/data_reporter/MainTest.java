package com.climateconfort.data_reporter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.commons.cli.CommandLine;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.climateconfort.common.SensorData;
import com.climateconfort.data_reporter.actions.ActionSender;
import com.climateconfort.data_reporter.cassandra.CassandraConnector;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.Parametroa;
import com.climateconfort.data_reporter.data_collection.DataReceiver;
import com.climateconfort.data_reporter.kafka.KafkaPublisher;

class MainTest {

    private static final int roomId = 1;
    private static final int buildingId = 1;
    private static final int clientId = 1;

    @TempDir
    private Path tempDir;

    @Mock
    private ActionSender actionSender;

    @Mock
    private CassandraConnector cassandraConnector;

    @Mock
    private DataReceiver dataReceiver;

    @Mock
    private KafkaPublisher kafkaPublisher;

    @Mock
    private ReadWriteLock readWriteLock;

    private Main main;
    private Path propertiesPath;

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        // Create temporary test properties file
        propertiesPath = tempDir.resolve("test.properties");
        Properties properties = getProperties();
        try (var writer = Files.newBufferedWriter(propertiesPath)) {
            properties.store(writer, "Test properties");
        }

        try (MockedConstruction<CassandraConnector> mockedConstructionCassandraConnector = mockConstruction(
                CassandraConnector.class);
                MockedConstruction<DataReceiver> mockedConstructionDataReceiver = mockConstruction(DataReceiver.class);
                MockedConstruction<KafkaPublisher> mockedConstructionKafkaPublisher = mockConstruction(
                        KafkaPublisher.class)) {
            main = new Main(propertiesPath);
        }

        main = spy(main);
        setField(main, "actionSender", actionSender);
        setField(main, "cassandraConnector", cassandraConnector);
        setField(main, "dataReceiver", dataReceiver);
        setField(main, "kafkaPublisher", kafkaPublisher);
        setField(main, "readWriteLock", readWriteLock);
    }

    @Test
    void testGenerateArgumentOptions() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Method generateArgumentOptionsMethod = setMethodPublic(Main.class, "generateArgumentOptions");
        assertNotNull(generateArgumentOptionsMethod.invoke(null));
    }

    @Test
    void testParseArguments() throws Exception {
        String[] args = { "-p", propertiesPath.toString() };

        Method parseArgumentsMethod = setMethodPublic(Main.class, "parseArguments", String[].class);

        CommandLine cmd = (CommandLine) parseArgumentsMethod.invoke(null, (Object) args);

        assertTrue(cmd.hasOption("p"));
        assertEquals(propertiesPath.toString(), cmd.getOptionValue("p"));
    }

    @Test
    void testSetupCorrect() throws IOException, TimeoutException, InterruptedException {
        Scanner scanner = mock(Scanner.class);
        main.setup(scanner);
        verify(dataReceiver).subscribe();
        verify(scanner).nextLine();
    }

    @Test
    void testSetupIncorrect() throws Exception {
        Scanner scanner = mock(Scanner.class);
        ExecutorService executorService = mock(ExecutorService.class);
        setField(main, "executorService", executorService);
        doThrow(IOException.class).when(dataReceiver).subscribe();
        doThrow(InterruptedException.class).when(executorService).awaitTermination(1, TimeUnit.MINUTES);
        assertDoesNotThrow(() -> main.setup(scanner));
    }

    @Test
    void getValueMapTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        when(readWriteLock.readLock()).thenReturn(mock(Lock.class));
        Map<Long, Map<Long, List<Parametroa>>> map = new HashMap<>();
        setField(main, "valueMap", map);
        Method method = setMethodPublic(Main.class, "getValueMap");
        assertEquals(map, method.invoke(main));
    }

    @Test
    void sequentialProgramLogicTestBigListTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchFieldException, IOException, InterruptedException, ExecutionException {
        List<SensorData> sensorDataList = new ArrayList<>();
        SensorData sensorData = new SensorData(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1);
        Field field = setFieldPublic(Main.class, "MAX_DATA_PER_PACKAGE");

        for (int i = 0; i < (int) field.get(null); i++) {
            sensorDataList.add(sensorData);
        }
        
        doThrow(IOException.class).when(main).packAndPublish(sensorData, sensorDataList);
        
        Method method = setMethodPublic(Main.class, "sequentialProgramLogic", SensorData.class, List.class);
        List<SensorData> retList = (List<SensorData>) method.invoke(main, sensorData, sensorDataList);
        assertTrue(retList.contains(sensorData));
    }

    @Test
    void sequentialProgramLogicSmallListTest() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        SensorData sensorData = new SensorData(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1);
        List<SensorData> sensorDataList = new ArrayList<>();
        Method method = setMethodPublic(Main.class, "sequentialProgramLogic", SensorData.class, List.class);
        List<SensorData> retList = (List<SensorData>) method.invoke(main, sensorData, sensorDataList);
        assertTrue(retList.contains(sensorData));
    }

    @Test
    void actionCalculateTest() {
        // Mock the Parametroa dependency
        Parametroa mockParameter = Mockito.mock(Parametroa.class);

        // Define test cases
        String parameterName = "TestParameter";

        // Case 1: hasMinimum is true, parameter.getBalioMin() > valueMean
        Mockito.when(mockParameter.getBalioMin()).thenReturn(10.0f);
        Mockito.when(mockParameter.getBalioMax()).thenReturn(50.0f);
        assertEquals("Raise the TestParameter", main.actionCalculate(mockParameter, 5, parameterName, true));

        // Case 2: parameter.getBalioMax() < valueMean
        assertEquals("Lower the TestParameter", main.actionCalculate(mockParameter, 60, parameterName, true));

        // Case 3: Both conditions are false
        assertEquals("", main.actionCalculate(mockParameter, 30, parameterName, true));

        // Case 4: hasMinimum is false, should not check BalioMin
        assertEquals("Lower the TestParameter", main.actionCalculate(mockParameter, 60, parameterName, false));
        assertEquals("", main.actionCalculate(mockParameter, 30, parameterName, false));
    }

    @Test
    void concurrentUpdateValuesTest() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException, NoSuchFieldException {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        Lock lockMock = mock(Lock.class);
        when(readWriteLock.writeLock()).thenReturn(lockMock);        

        setField(main, "executorService", mockExecutor);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doNothing().when(mockExecutor).execute(runnableCaptor.capture());

        main.concurrentUpdateValues();

        Runnable capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();

        verify(lockMock, times(1)).lock();
        verify(lockMock, times(1)).unlock();
        verify(mockExecutor, times(1)).execute(any(Runnable.class)); 
    }

    @Test
    void sequentialUpdateValuesTest() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        main.sequentialUpdateValues();
        verify(cassandraConnector).getParameters();
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("climateconfort.room_id", String.valueOf(roomId));
        properties.setProperty("climateconfort.building_id", String.valueOf(buildingId));
        properties.setProperty("climateconfort.client_id", String.valueOf(clientId));
        properties.setProperty("cassandra.nodes", "1-1,1-2");
        return properties;
    }

    private <T, E> void setField(T target, String fieldName, E newValue)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, newValue);
    }

    private <T> Field setFieldPublic(Class<T> target, String fieldName)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = target.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    private <T> Method setMethodPublic(Class<T> target, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        Method method = target.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method;
    }
}
