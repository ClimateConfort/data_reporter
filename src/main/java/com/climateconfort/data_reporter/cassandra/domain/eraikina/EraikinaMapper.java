package com.climateconfort.data_reporter.cassandra.domain.eraikina;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface EraikinaMapper {
    @DaoFactory
    EraikinaDao eraikinaDao();
}
