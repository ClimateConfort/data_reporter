package com.climateconfort.data_reporter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import com.climateconfort.common.SensorData;
import com.climateconfort.data_reporter.avro.AvroSerializer;
import com.climateconfort.data_reporter.avro.SensorDataAvro;

public class AvroSerializerTest {

    private static final long roomId = 1L;
    private static final long buildingId = 1L;
    private static final long clientId = 1L;

    private Random random;

    @Mock
    private SpecificDatumWriter<SensorDataAvro> specificDatumWriter;

    @Mock
    private DataFileWriter<SensorDataAvro> dataFileWriter;

    @InjectMocks
    private AvroSerializer avroSerializer;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setup() {
        specificDatumWriter = mock(SpecificDatumWriter.class);
        dataFileWriter = mock(DataFileWriter.class);
        random = new Random();
    }

    @Test
    void constructorTest() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException {
        Constructor<AvroSerializer> constructor = AvroSerializer.class.getDeclaredConstructor();
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
    public void testPackToAvroFile() throws IOException {
        // Arrange
        List<SensorData> sensorDataList = new ArrayList<>();

        for (int i = 0; i < 10000; i++) {
            sensorDataList.add(new SensorData(random.nextLong(), 1, random.nextLong(), random.nextLong(),
                    random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat(),
                    random.nextFloat()));
        }

        ByteArrayOutputStream arrayOutputStream = AvroSerializer.packToAvroFile(sensorDataList);
        assertNotNull(arrayOutputStream);

        try (DataFileReader<SensorDataAvro> dataFileReader = new DataFileReader<>(
                new SeekableByteArrayInput(arrayOutputStream.toByteArray()),
                new SpecificDatumReader<>(SensorDataAvro.class))) {
            int i = 0;
            while (dataFileReader.hasNext()) {
                SensorDataAvro sensorDataAvro = dataFileReader.next();
                SensorData sensorData = sensorDataList.get(i);
                assertEquals(sensorDataAvro.getUnixTime(), sensorData.getUnixTime());
                assertEquals(sensorDataAvro.getRoomId(), sensorData.getRoomId());
                assertEquals(sensorDataAvro.getBuildingId(), sensorData.getBuildingId());
                assertEquals(sensorDataAvro.getClientId(), sensorData.getClientId());
                assertEquals(sensorDataAvro.getTemperature(), sensorData.getTemperature());
                assertEquals(sensorDataAvro.getSoundLevel(), sensorData.getSoundLevel());
                assertEquals(sensorDataAvro.getHumidity(), sensorData.getHumidity());
                assertEquals(sensorDataAvro.getPressure(), sensorData.getPressure());
                i++;
            }

            assertEquals(sensorDataList.size(), i);
        }

    }
}
