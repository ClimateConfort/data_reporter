package com.climateconfort.data_reporter.cassandra.domain.parametroa;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Parametroa {
    @PartitionKey
    int id;

    String mota;
    
    @CqlName("balio_min")
    float balioMin;
    
    @CqlName("balio_max")
    float balioMax;
    
    @CqlName("gela_id")
    int gelaId;
}
