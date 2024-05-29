package com.climateconfort.data_reporter.cassandra;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.AddressTranslator;

public class CassandraConnector 
{
    private final String FICHEROPROPIEDADES = "src\\main\\java\\com\\climateconfort\\resources\\application.properties";
    private final String ERAIKIN_QUERY = "SELECT id FROM climate_confort.Eraikina WHERE enpresa_id=";
    private final String GELA_QUERY = "SELECT id FROM climate_confort.Gela WHERE eraikina_id=";
    private final String PARAMETRO_QUERY = "SELECT mota, balio_min, balio_max FROM climate_confort.Parametroa WHERE gela_id=";
    private final Integer PORT = 9042;

    private Map<InetSocketAddress, InetSocketAddress> addressMap;
    private Cluster cluster;
    private Session session;
    private Properties prop;

    private int enpresa_id;

    /**
     * @throws  IOException 
     * @throws  FileNotFoundException 
     * 
     * @params  enpresa_id
     * 
     * @brief   CassandraConnector konstruktorea
     */
    public CassandraConnector(int enpresa_id, Map<String, String[]> addresses) throws FileNotFoundException, IOException 
    {
        this.addressMap = Map.of(
            new InetSocketAddress(addresses.get("node1")[0], PORT), new InetSocketAddress(addresses.get("node1")[1], PORT),
            new InetSocketAddress(addresses.get("node2")[0], PORT), new InetSocketAddress(addresses.get("node2")[1], PORT),
            new InetSocketAddress(addresses.get("node3")[0], PORT), new InetSocketAddress(addresses.get("node3")[1], PORT)
        );

        this.enpresa_id = enpresa_id;
        prop = new Properties();
        prop.load(new FileInputStream(FICHEROPROPIEDADES));
        
        /* Konektatu lehenengo nodoaren IP publikora */
        connect(addresses.get("node1")[1]);
    }

    public void connect(String node) 
    {
        String user = prop.getProperty("spring.datasource.cassandra_user");
        String password = prop.getProperty("spring.datasource.cassandra_password");

        Builder builder = Cluster.builder().addContactPoint(node).withPort(PORT)
        .withCredentials(user, password)
        .withAddressTranslator(new PublicIpAddressTranslator(addressMap));

        cluster = builder.build();
        session = cluster.connect();
    }

    /**
     * @brief   Parametroak lortu Cassandra datu basetik eta Map bat sortu eraikin eta gela guztiekin.
     */
    public Map<Integer, Map<Integer, Map<String, Float[]>>> getParameters() 
    {
        String database = prop.getProperty("spring.datasource.cassandra_database=climate_confort");
        session.execute("use " + database + ";");

        Map<Integer, Map<Integer, Map<String, Float[]>>> eraikinak = new HashMap<>();

        ResultSet eraiknResultSet = session.execute(ERAIKIN_QUERY + enpresa_id + ";");
        for (Row eraikina_row : eraiknResultSet) 
        {
            Map<Integer, Map<String, Float[]>> gelak = new HashMap<>();

             /* 'Gela' taulan query-a exekutatu eta bere balioak mapan gorde */
            int eraikina_id = eraikina_row.getInt("id");   
            ResultSet gelaResultSet = session.execute(GELA_QUERY + eraikina_id + ";");
            for (Row gela_row : gelaResultSet) 
            {
                Map<String, Float[]> parametroak = new HashMap<>();

                /* 'Parametroa' taulan query-a exekutatu eta bere balioak mapan gorde */
                int gela_id = gela_row.getInt("id");
                ResultSet parametroResultSet = session.execute(PARAMETRO_QUERY + gela_id + ";");
                for (Row parametro_row : parametroResultSet)
                {
                    String mota = parametro_row.getString("mota");
                    Float[] temporal_parameters = new Float[2];

                    temporal_parameters[0] = parametro_row.getFloat("balio_min");
                    temporal_parameters[1] = parametro_row.getFloat("balio_max");

                    parametroak.put(mota, temporal_parameters);
                }
                gelak.put(gela_id, parametroak);
            }
            eraikinak.put(eraikina_id, gelak);
        }
        return eraikinak;
    }

    public Session getSession() 
    {
        return this.session;
    }

    public void close() 
    {
        session.close();
        cluster.close();
    }

    public class PublicIpAddressTranslator implements AddressTranslator {
        private final Map<InetSocketAddress, InetSocketAddress> addressMap;
    
        public PublicIpAddressTranslator(Map<InetSocketAddress, InetSocketAddress> addressMap) 
        {
            this.addressMap = addressMap;
        }
    
        @Override
        public InetSocketAddress translate(InetSocketAddress address) 
        {
            return addressMap.getOrDefault(address, address);
        }
    
        @Override
        public void close() {  }

        @Override
        public void init(Cluster cluster) {  }
    }

    

}