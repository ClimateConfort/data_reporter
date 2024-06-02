package com.climateconfort.data_reporter.cassandra.domain.gela;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface GelaMapper {
    @DaoFactory
    GelaDao gelaDao();
}
