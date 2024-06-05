package com.climateconfort.data_reporter.cassandra.domain.parametroa;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;

@Dao
public interface ParametroaDao {
    @Select(customWhereClause = "gela_id = :gelaId", allowFiltering = true)
    PagingIterable<Parametroa> findAllByGelaId(long gelaId);
}
