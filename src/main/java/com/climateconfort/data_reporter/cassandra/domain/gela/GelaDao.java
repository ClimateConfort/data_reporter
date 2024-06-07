package com.climateconfort.data_reporter.cassandra.domain.gela;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;

@Dao
public interface GelaDao {
    @Select(customWhereClause = "enpresa_id = :enpresaId AND eraikina_id = :eraikinaId", allowFiltering = true)
    PagingIterable<Gela> findAllByEnpresaIdEraikinaId(long enpresaId, long eraikinaId);
}
