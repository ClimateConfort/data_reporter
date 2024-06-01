package com.climateconfort.data_reporter.cassandra.domain.eraikina.dao.impl;

import java.util.ArrayList;
import java.util.List;

import com.climateconfort.data_reporter.cassandra.domain.eraikina.dao.EraikinaDao;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.model.Eraikina;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class EraikinaDaoImpl implements EraikinaDao 
{
    private Session session;

    public EraikinaDaoImpl(Session session)
    {
        this.session = session;
    }

    @Override
    public List<Eraikina> findByEnpresaId(int enpresa_id) {
        String query = "SELECT * FROM Eraikina WHERE enpresa_id = ? ALLOW FILTERING";
        ResultSet result = session.execute(query, enpresa_id);
        
        List<Eraikina> eraikinak = new ArrayList<>();

        for (Row row : result.all()) 
        {
            Eraikina eraikina = new Eraikina();
            eraikina.setId(row.getInt("id"));
            eraikina.setLokalizazioa(row.getString("lokalizazioa"));

            eraikinak.add(eraikina);
        }

        return eraikinak;
    }
    
}
