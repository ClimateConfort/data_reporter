package com.climateconfort.data_reporter.cassandra;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

public class CassandraConnector 
{
    private Properties prop;
    private int enpresa_id;
    private final String FICHEROPROPIEDADES = "src\\main\\java\\com\\climateconfort\\resources\\application.properties";
    private final String ERAIKIN_QUERY = "SELECT id FROM climate_confort.Eraikina WHERE enpresa_id=";
    private final String GELA_QUERY = "SELECT id, parametro_min, parametro_max, eraikina_id FROM climate_confort.Gela WHERE eraikina_id=";
    private Map<Integer, Float[]> gelaParameters;
    private Map<Integer, Map<Integer, Float[]>> eraikinak;

    /**
     * @throws  IOException 
     * @throws  FileNotFoundException 
     * 
     * @params  enpresa_id
     * 
     * @brief   CassandraConnector konstruktorea
     */
    public CassandraConnector(int enpresa_id) throws FileNotFoundException, IOException 
    {
        this.enpresa_id = enpresa_id;
        prop = new Properties();
        prop.load(new FileInputStream(FICHEROPROPIEDADES));
        gelaParameters = new HashMap<>();
        eraikinak = new HashMap<>();
    }

    /**
     * @brief   Parametroak lortu Cassandra datu basetik eta Map bat sortu eraikin eta gela guztiekin.
     */
    public Map<Integer, Map<Integer, Float[]>> getParameters() 
    {
        /* Obtener propiedades */
        int cassandra_port = Integer.parseInt(prop.getProperty("spring.datasource.cassandra_port"));
        String cassandra_host = prop.getProperty("spring.datasource.cassandra_ip");
        String cassandra_user = prop.getProperty("spring.datasource.cassandra_user");
        String cassandra_password = prop.getProperty("spring.datasource.cassandra_password");
        String cassandra_database = prop.getProperty("spring.datasource.cassandra_database");

        try (CqlSession session = CqlSession.builder()
            .addContactPoint(new InetSocketAddress(cassandra_host, cassandra_port))
            .withLocalDatacenter(cassandra_database)
            .withAuthCredentials(cassandra_user, cassandra_password)
            .build())
        {

            ResultSet raiknResultSet = session.execute(SimpleStatement.newInstance(ERAIKIN_QUERY + enpresa_id));
            Float[] temporal_parameters = new Float[2];

            for (Row eraikina_row : raiknResultSet) 
            {
                int eraikina_id = eraikina_row.getInt("id");   
                ResultSet gelaResultSet = session.execute(SimpleStatement.newInstance(GELA_QUERY + eraikina_id));
                
                for (Row gela_row : gelaResultSet) 
                {
                    int gela_id = gela_row.getInt("id");
                    temporal_parameters[0] = gela_row.getFloat("parametro_min");
                    temporal_parameters[1] = gela_row.getFloat("parametro_max");
            
                    gelaParameters.put(gela_id, temporal_parameters);
                }
                eraikinak.put(eraikina_id, gelaParameters);
            }
            return eraikinak;
        }
    }
}