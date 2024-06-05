package com.climateconfort.data_reporter.cassandra.domain.eraikina;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;

@Dao
public interface EraikinaDao {
    @Select(customWhereClause = "enpresa_id = :enpresaId", allowFiltering = true)
    PagingIterable<Eraikina> findAllByEnpresaId(long enpresaId);
}
