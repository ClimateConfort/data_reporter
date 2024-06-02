package com.climateconfort.data_reporter.cassandra.domain.parametroa.model;

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
@CqlName("Parametroa")
public class Parametroa {
    int id;
    String mota;
    float balioMin;
    float balioMax;
    int gelaId;
}
