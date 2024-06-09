package com.climateconfort.data_reporter.cassandra.domain.eraikina;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Eraikina {
    @PartitionKey(1)
    long enpresaId;

    @PartitionKey(2)
    long eraikinaId;
    
    String lokalizazioa;
}
