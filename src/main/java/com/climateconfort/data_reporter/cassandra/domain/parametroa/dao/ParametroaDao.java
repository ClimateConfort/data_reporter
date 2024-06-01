package com.climateconfort.data_reporter.cassandra.domain.parametroa.dao;

import java.util.List;

import com.climateconfort.data_reporter.cassandra.domain.parametroa.model.Parametroa;

public interface ParametroaDao 
{
    List<Parametroa> findByGelaId(int gela_id);
}
