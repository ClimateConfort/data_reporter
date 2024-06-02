package com.climateconfort.data_reporter.cassandra.domain.gela;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Gela {
    @PartitionKey
    int id;

    @CqlName("eraikina_id")
    int eraikinaId;
}
