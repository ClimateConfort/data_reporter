package com.climateconfort.data_reporter.cassandra.domain.eraikina.dao;

import java.util.List;

import com.climateconfort.data_reporter.cassandra.domain.eraikina.model.Eraikina;
import com.datastax.oss.driver.api.mapper.annotations.Dao;

@Dao
public interface EraikinaDao {
    List<Eraikina> findByEnpresaId(int enpresaId);
}
