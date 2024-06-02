package com.climateconfort.data_reporter.cassandra.domain.parametroa.dao.impl;

import java.util.ArrayList;
import java.util.List;

import com.climateconfort.data_reporter.cassandra.domain.parametroa.dao.ParametroaDao;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.model.Parametroa;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class ParametroaDaoImpl implements ParametroaDao {
    Session session;

    public ParametroaDaoImpl(Session session) {
        this.session = session;
    }

    @Override
    public List<Parametroa> findByGelaId(int gelaId) {
        String query = "SELECT * FROM Parametroa WHERE gela_id = ? ALLOW FILTERING";
        ResultSet result = session.execute(query, gelaId);
        List<Parametroa> parametroaList = new ArrayList<>();

        for (Row row : result.all()) {
            Parametroa parametroa = new Parametroa();
            parametroa.setId(row.getInt("id"));
            parametroa.setBalioMax(row.getFloat("balio_max"));
            parametroa.setBalioMin(row.getFloat("balio_min"));
            parametroa.setMota(row.getString("mota"));

            parametroaList.add(parametroa);
        }

        return parametroaList;
    }

}
