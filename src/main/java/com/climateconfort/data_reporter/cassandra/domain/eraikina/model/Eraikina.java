package com.climateconfort.data_reporter.cassandra.domain.eraikina.model;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@CqlName("Eraikina")
public class Eraikina
{
    int id;
    String lokalizazioa;
    int enpresa_id;
}
