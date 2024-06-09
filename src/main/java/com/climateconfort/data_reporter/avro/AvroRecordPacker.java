package com.climateconfort.data_reporter.avro;

import java.util.List;

import com.climateconfort.common.SensorData;

public class AvroRecordPacker {

    private AvroRecordPacker() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("AvroSerializer class should not be instantiated");
    }

    public static List<SensorDataAvro> packToList(List<SensorData> sensorDataList) {
        return sensorDataList.
                stream()
                .map(AvroRecordPacker::createSensorDataAvro)
                .toList();
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
