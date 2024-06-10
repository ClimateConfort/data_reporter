package com.climateconfort.data_reporter.cassandra.domain.parametroa;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface ParametroaMapper {
    @DaoFactory
    ParametroaDao parametroaDao();
}
