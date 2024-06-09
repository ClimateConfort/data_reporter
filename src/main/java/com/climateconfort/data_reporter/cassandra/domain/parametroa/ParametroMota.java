package com.climateconfort.data_reporter.cassandra.domain.parametroa;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParametroMota {
    public static final String TEMPERATURE = "TMP";
    public static final String SOUND_LEVEL = "SND";
    public static final String HUMIDITY = "HMD";
    public static final String PRESSURE = "PRS";
}
