package com.climateconfort.data_reporter.cassandra.domain.gela;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Gela {
    @PartitionKey(1)
    long enpresaId;

    @PartitionKey(2)
    long eraikinaId;

    @PartitionKey(3)
    long gelaId;
}
