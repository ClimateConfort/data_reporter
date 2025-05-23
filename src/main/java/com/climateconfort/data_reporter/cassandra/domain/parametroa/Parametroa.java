package com.climateconfort.data_reporter.cassandra.domain.parametroa;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Parametroa {
    @PartitionKey(1)
    long enpresaId;

    @PartitionKey(2)
    long eraikinaId;

    @PartitionKey(3)
    long gelaId;

    @PartitionKey(4)
    long parametroaId;

    String mota;

    boolean minimoaDu;

    float balioMin;
    
    float balioMax;
}
