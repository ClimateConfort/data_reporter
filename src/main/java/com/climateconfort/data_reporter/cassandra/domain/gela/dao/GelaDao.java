package com.climateconfort.data_reporter.cassandra.domain.gela.dao;

import java.util.List;

import com.climateconfort.data_reporter.cassandra.domain.gela.model.Gela;
import com.datastax.oss.driver.api.mapper.annotations.Dao;

@Dao
public interface GelaDao {
    List<Gela> findByEraikinaId(int eraikinaId);
}
