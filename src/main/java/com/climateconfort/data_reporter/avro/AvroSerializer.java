package com.climateconfort.data_reporter.avro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;

import com.climateconfort.common.SensorData;

public class AvroSerializer {

    private AvroSerializer() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("AvroSerializer class should not be instantiated");
    }

    private static final DatumWriter<SensorDataAvro> sensorDatumWriter = new SpecificDatumWriter<>(
            SensorDataAvro.class);

    public static ByteArrayOutputStream packToAvroFile(List<SensorData> sensorDataList) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (DataFileWriter<SensorDataAvro> dataFileWriter = new DataFileWriter<>(sensorDatumWriter)) {
            dataFileWriter.setCodec(CodecFactory.zstandardCodec(22, true, true));
            dataFileWriter.create(SensorDataAvro.SCHEMA$, outputStream);
            for (SensorData sensorData : sensorDataList) {
                SensorDataAvro sensorDataAvro = createSensorDataAvro(sensorData);
                dataFileWriter.append(sensorDataAvro);
            }
        }

        return outputStream;
    }

    private static SensorDataAvro createSensorDataAvro(SensorData sensorData) {
        return SensorDataAvro.newBuilder()
                .setUnixTime(sensorData.getUnixTime())
                .setRoomId(sensorData.getRoomId())
                .setBuildingId(sensorData.getBuildingId())
                .setClientId(sensorData.getClientId())
                .setTemperature(sensorData.getTemperature())
                .setSoundLevel(sensorData.getSoundLevel())
                .setHumidity(sensorData.getHumidity())
                .setPressure(sensorData.getPressure())
                .build();
    }
}
