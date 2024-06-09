package com.climateconfort.data_reporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.avro.file.DataFileWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.climateconfort.common.SensorData;
import com.climateconfort.data_reporter.avro.AvroRecordPacker;
import com.climateconfort.data_reporter.avro.SensorDataAvro;

class AvroRecordPackerTest {

    private Random random;

    @Mock
    private SpecificDatumWriter<SensorDataAvro> specificDatumWriter;

    @Mock
    private DataFileWriter<SensorDataAvro> dataFileWriter;

    @InjectMocks
    private AvroRecordPacker avroSerializer;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setup() {
        specificDatumWriter = mock(SpecificDatumWriter.class);
        dataFileWriter = mock(DataFileWriter.class);
        random = new Random();
    }

    @Test
    void constructorTest() throws NoSuchMethodException, SecurityException, InstantiationException,
            IllegalAccessException, IllegalArgumentException {
        Constructor<AvroRecordPacker> constructor = AvroRecordPacker.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Expected UnsupportedOperationException to be thrown");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof UnsupportedOperationException);
            assertEquals("AvroSerializer class should not be instantiated", cause.getMessage());
        }
    }

    @Test
    void testPackToFile() throws IOException {
        List<SensorData> sensorDataList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            sensorDataList.add(new SensorData(random.nextLong(), 1, random.nextLong(), random.nextLong(),
                    random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat(),
                    random.nextFloat()));
        }

        List<SensorDataAvro> sensorDataAvros = AvroRecordPacker.packToList(sensorDataList);
        assertEquals(sensorDataAvros.size(), sensorDataList.size());
        for (int i = 0; i < sensorDataAvros.size(); i++) {
            SensorDataAvro sensorDataAvro = sensorDataAvros.get(i);
            SensorData sensorData = sensorDataList.get(i);
            assertEquals(sensorDataAvro.getUnixTime(), sensorData.getUnixTime());
            assertEquals(sensorDataAvro.getRoomId(), sensorData.getRoomId());
            assertEquals(sensorDataAvro.getBuildingId(), sensorData.getBuildingId());
            assertEquals(sensorDataAvro.getClientId(), sensorData.getClientId());
            assertEquals(sensorDataAvro.getTemperature(), sensorData.getTemperature());
            assertEquals(sensorDataAvro.getSoundLevel(), sensorData.getSoundLevel());
            assertEquals(sensorDataAvro.getHumidity(), sensorData.getHumidity());
            assertEquals(sensorDataAvro.getPressure(), sensorData.getPressure());
        }
    }
}
