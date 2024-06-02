package com.climateconfort.data_reporter.cassandra.domain.gela.dao.impl;

import java.util.ArrayList;
import java.util.List;

import com.climateconfort.data_reporter.cassandra.domain.gela.dao.GelaDao;
import com.climateconfort.data_reporter.cassandra.domain.gela.model.Gela;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class GelaDaoImpl implements GelaDao {
    private Session session;

    public GelaDaoImpl(Session session) {
        this.session = session;
    }

    @Override
    public List<Gela> findByEraikinaId(int eraikinaId) {
        String query = "SELECT * FROM Gela WHERE eraikina_id = ? ALLOW FILTERING";
        ResultSet result = session.execute(query, eraikinaId);
        List<Gela> gelaList = new ArrayList<>();

        for (Row row : result.all()) {
            Gela gela = new Gela();
            gela.setId(row.getInt("id"));

            gelaList.add(gela);
        }

        return gelaList;
    }

}
