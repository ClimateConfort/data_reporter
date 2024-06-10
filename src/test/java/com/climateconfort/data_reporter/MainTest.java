package com.climateconfort.data_reporter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.net.ssl.SSLContext;

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
import com.climateconfort.common.SensorData;
import com.climateconfort.data_reporter.actions.ActionSender;
import com.climateconfort.data_reporter.cassandra.CassandraConnector;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.ParametroMota;
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
            IllegalAccessException, UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
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
                        KafkaPublisher.class);
                MockedConstruction<TlsManager> mockedConstruction = mockConstruction(TlsManager.class,
                        (mock, context) -> when(mock.getSslContext()).thenReturn(mock(SSLContext.class)))) {
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
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> verify(dataReceiver).subscribe());
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
    void countDownLatchFactoryTest() {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        assertEquals(countDownLatch.getCount(), main.countDownLatchFactory(2).getCount());
    }

    @Test
    void concurrentProgramLogicSmallListTest() {
        SensorData sensorData = new SensorData(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1);
        List<SensorData> sensorDataList = new ArrayList<>();
        var retList = main.concurrentProgramLogic(sensorData, sensorDataList);
        assertTrue(retList.contains(sensorData));
    }

    @Test
    void concurrentProgramLogicBigListTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, IOException, InterruptedException, ExecutionException {
        List<SensorData> sensorDataList = new ArrayList<>();
        SensorData sensorData = new SensorData(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1);
        Field field = setFieldPublic(Main.class, "MAX_DATA_PER_PACKAGE");

        for (int i = 0; i < (int) field.get(null); i++) {
            sensorDataList.add(sensorData);
        }

        ExecutorService mockExecutorService = mock(ExecutorService.class);
        setField(main, "executorService", mockExecutorService);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        CountDownLatch mockCountDownLatch = mock(CountDownLatch.class);
        doNothing().when(mockExecutorService).execute(runnableCaptor.capture());
        doThrow(InterruptedException.class).when(main).packAndPublish(any(), any());
        doThrow(InterruptedException.class).when(main).concurrentTakeAction(anyLong(), anyLong(), anyList());
        doReturn(mockCountDownLatch).when(main).countDownLatchFactory(anyInt());
        doThrow(InterruptedException.class).when(mockCountDownLatch).await();
        var retList = main.concurrentProgramLogic(sensorData, sensorDataList);
        var runnables = runnableCaptor.getAllValues();

        for (Runnable runnable : runnables) {
            runnable.run();
        }

        verify(mockExecutorService, times(2)).execute(any());
        assertTrue(retList.contains(sensorData));
    }

    @SuppressWarnings("unchecked")
    @Test
    void sequentialProgramLogicBigListTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchFieldException, IOException,
            InterruptedException, ExecutionException {
        List<SensorData> sensorDataList = new ArrayList<>();
        SensorData sensorData = new SensorData(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1);
        Field field = setFieldPublic(Main.class, "MAX_DATA_PER_PACKAGE");

        for (int i = 0; i < (int) field.get(null); i++) {
            sensorDataList.add(sensorData);
        }

        doThrow(InterruptedException.class).when(main).packAndPublish(sensorData, sensorDataList);

        Method method = setMethodPublic(Main.class, "sequentialProgramLogic", SensorData.class, List.class);
        List<SensorData> retList = (List<SensorData>) method.invoke(main, sensorData, sensorDataList);
        assertTrue(retList.contains(sensorData));
    }

    @Test
    void sequentialProgramLogicSmallListTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        SensorData sensorData = new SensorData(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1);
        List<SensorData> sensorDataList = new ArrayList<>();
        var retList = main.sequentialProgramLogic(sensorData, sensorDataList);
        assertTrue(retList.contains(sensorData));
    }

    @Test
    void concurrentTakeActionTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, InterruptedException, ExecutionException {
        SensorData sensorData = new SensorData(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1);
        List<SensorData> sensorDataList = new ArrayList<>();
        sensorDataList.add(sensorData);
        ExecutorService spyExecutorService = Executors.newSingleThreadExecutor();
        spyExecutorService = spy(spyExecutorService);
        setField(main, "executorService", spyExecutorService);
        main.concurrentTakeAction(1, 1, sensorDataList);
        verify(spyExecutorService).invokeAll(anyList());
    }

    @Test
    void performActionTemperatureTest() throws IOException, TimeoutException {
        // Mock the getValueMap method
        Parametroa parametroa = mock(Parametroa.class);
        Map<Long, List<Parametroa>> roomMap = Map.of(1L, List.of(parametroa));
        Map<Long, Map<Long, List<Parametroa>>> buildingMap = Map.of(1L, roomMap);
        doReturn(buildingMap).when(main).getValueMap();

        // Mock parameter methods
        when(parametroa.getMota()).thenReturn(ParametroMota.TEMPERATURE);
        when(parametroa.isMinimoaDu()).thenReturn(true);
        when(parametroa.getBalioMin()).thenReturn(10.0f);
        when(parametroa.getBalioMax()).thenReturn(50.0f);

        // Mock actionCalculate method
        doReturn("Raise the temperature").when(main).actionCalculate(parametroa, 5, "temperature", true);

        // Call the method to test
        main.performAction(1L, 1L, 5, 0, 0, 0);

        // Capture the arguments for the actionSender.publish method
        ArgumentCaptor<Long> roomIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> buildingIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(actionSender).publish(roomIdCaptor.capture(), buildingIdCaptor.capture(), actionCaptor.capture());

        // Assert the captured arguments
        assertEquals(1L, roomIdCaptor.getValue());
        assertEquals(1L, buildingIdCaptor.getValue());
        assertEquals("Raise the temperature", actionCaptor.getValue());

        // Verify the logging
        verify(main).performAction(1L, 1L, 5, 0, 0, 0);
    }

    @Test
    void performActionSoundTest() throws IOException, TimeoutException {
        // Mock the getValueMap method
        Parametroa parametroa = mock(Parametroa.class);
        Map<Long, List<Parametroa>> roomMap = Map.of(1L, List.of(parametroa));
        Map<Long, Map<Long, List<Parametroa>>> buildingMap = Map.of(1L, roomMap);
        doReturn(buildingMap).when(main).getValueMap();

        // Mock parameter methods
        when(parametroa.getMota()).thenReturn(ParametroMota.SOUND_LEVEL);
        when(parametroa.isMinimoaDu()).thenReturn(true);
        when(parametroa.getBalioMin()).thenReturn(10.0f);
        when(parametroa.getBalioMax()).thenReturn(50.0f);

        // Mock actionCalculate method
        doReturn("Raise the sound").when(main).actionCalculate(parametroa, 5, "sound", true);

        // Call the method to test
        main.performAction(1L, 1L, 5, 0, 0, 0);

        // Capture the arguments for the actionSender.publish method
        ArgumentCaptor<Long> roomIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> buildingIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(actionSender).publish(roomIdCaptor.capture(), buildingIdCaptor.capture(), actionCaptor.capture());

        // Assert the captured arguments
        assertEquals(1L, roomIdCaptor.getValue());
        assertEquals(1L, buildingIdCaptor.getValue());
        assertEquals("Raise the sound", actionCaptor.getValue());

        // Verify the logging
        verify(main).performAction(1L, 1L, 5, 0, 0, 0);
    }

    @Test
    void performActionHumidityTest() throws IOException, TimeoutException {
        // Mock the getValueMap method
        Parametroa parametroa = mock(Parametroa.class);
        Map<Long, List<Parametroa>> roomMap = Map.of(1L, List.of(parametroa));
        Map<Long, Map<Long, List<Parametroa>>> buildingMap = Map.of(1L, roomMap);
        doReturn(buildingMap).when(main).getValueMap();

        // Mock parameter methods
        when(parametroa.getMota()).thenReturn(ParametroMota.HUMIDITY);
        when(parametroa.isMinimoaDu()).thenReturn(true);
        when(parametroa.getBalioMin()).thenReturn(10.0f);
        when(parametroa.getBalioMax()).thenReturn(50.0f);

        // Mock actionCalculate method
        doReturn("Raise the humidity").when(main).actionCalculate(parametroa, 5, "humidity", true);

        // Call the method to test
        main.performAction(1L, 1L, 5, 0, 0, 0);

        // Capture the arguments for the actionSender.publish method
        ArgumentCaptor<Long> roomIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> buildingIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(actionSender).publish(roomIdCaptor.capture(), buildingIdCaptor.capture(), actionCaptor.capture());

        // Assert the captured arguments
        assertEquals(1L, roomIdCaptor.getValue());
        assertEquals(1L, buildingIdCaptor.getValue());
        assertEquals("Raise the humidity", actionCaptor.getValue());

        // Verify the logging
        verify(main).performAction(1L, 1L, 5, 0, 0, 0);
    }

    @Test
    void performActionPressureTest() throws IOException, TimeoutException {
        // Mock the getValueMap method
        Parametroa parametroa = mock(Parametroa.class);
        Map<Long, List<Parametroa>> roomMap = Map.of(1L, List.of(parametroa));
        Map<Long, Map<Long, List<Parametroa>>> buildingMap = Map.of(1L, roomMap);
        doReturn(buildingMap).when(main).getValueMap();

        // Mock parameter methods
        when(parametroa.getMota()).thenReturn(ParametroMota.PRESSURE);
        when(parametroa.isMinimoaDu()).thenReturn(true);
        when(parametroa.getBalioMin()).thenReturn(10.0f);
        when(parametroa.getBalioMax()).thenReturn(50.0f);

        // Mock actionCalculate method
        doReturn("Raise the pressure").when(main).actionCalculate(parametroa, 5, "pressure", true);

        // Call the method to test
        main.performAction(1L, 1L, 5, 0, 0, 0);

        // Capture the arguments for the actionSender.publish method
        ArgumentCaptor<Long> roomIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> buildingIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(actionSender).publish(roomIdCaptor.capture(), buildingIdCaptor.capture(), actionCaptor.capture());

        // Assert the captured arguments
        assertEquals(1L, roomIdCaptor.getValue());
        assertEquals(1L, buildingIdCaptor.getValue());
        assertEquals("Raise the pressure", actionCaptor.getValue());

        // Verify the logging
        verify(main).performAction(1L, 1L, 5, 0, 0, 0);
    }

    @Test
    void performActionTypeExceptionTest() throws IOException, TimeoutException {
        // Mock the getValueMap method
        Parametroa parametroa = mock(Parametroa.class);
        Map<Long, List<Parametroa>> roomMap = Map.of(1L, List.of(parametroa));
        Map<Long, Map<Long, List<Parametroa>>> buildingMap = Map.of(1L, roomMap);
        doReturn(buildingMap).when(main).getValueMap();

        // Mock parameter methods
        when(parametroa.getMota()).thenReturn("ZABORRA");
        when(parametroa.isMinimoaDu()).thenReturn(true);
        when(parametroa.getBalioMin()).thenReturn(10.0f);
        when(parametroa.getBalioMax()).thenReturn(50.0f);

        // Mock actionCalculate method
        doReturn("Raise the pressure").when(main).actionCalculate(parametroa, 5, "pressure", true);

        assertThrows(UnsupportedOperationException.class, () -> main.performAction(1L, 1L, 5, 0, 0, 0));
    }

    @Test
    void performActionPublishExceptionTest() throws IOException, TimeoutException {
        // Mock the getValueMap method
        Parametroa parametroa = mock(Parametroa.class);
        Map<Long, List<Parametroa>> roomMap = Map.of(1L, List.of(parametroa));
        Map<Long, Map<Long, List<Parametroa>>> buildingMap = Map.of(1L, roomMap);
        doReturn(buildingMap).when(main).getValueMap();

        // Mock parameter methods
        when(parametroa.getMota()).thenReturn(ParametroMota.PRESSURE);
        when(parametroa.isMinimoaDu()).thenReturn(true);
        when(parametroa.getBalioMin()).thenReturn(10.0f);
        when(parametroa.getBalioMax()).thenReturn(50.0f);
        doReturn("Raise the pressure").when(main).actionCalculate(parametroa, 5, "pressure", true);
        doThrow(IOException.class).when(actionSender).publish(anyLong(), anyLong(), anyString());

        assertDoesNotThrow(() -> main.performAction(1L, 1L, 5, 0, 0, 0));
    }

    @Test
    void actionCalculateTest() {
        // Mock the Parametroa dependency
        Parametroa mockParameter = Mockito.mock(Parametroa.class);

        String parameterName = "TestParameter";

        when(mockParameter.getBalioMin()).thenReturn(10.0f);
        when(mockParameter.getBalioMax()).thenReturn(50.0f);

        assertEquals("Raise the TestParameter", main.actionCalculate(mockParameter, 5, parameterName, true));
        assertEquals("Lower the TestParameter", main.actionCalculate(mockParameter, 60, parameterName, true));
        assertEquals("", main.actionCalculate(mockParameter, 30, parameterName, true));
        assertEquals("Lower the TestParameter", main.actionCalculate(mockParameter, 60, parameterName, false));
        assertEquals("", main.actionCalculate(mockParameter, 30, parameterName, false));
    }

    @Test
    void concurrentUpdateValuesTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, InterruptedException, NoSuchFieldException {
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
    void sequentialUpdateValuesTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
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
