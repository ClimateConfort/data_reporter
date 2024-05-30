package com.climateconfort.data_reporter.cassandra;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.climateconfort.data_reporter.cassandra.domain.eraikina.dao.EraikinaDao;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.dao.impl.EraikinaDaoImpl;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.model.Eraikina;
import com.climateconfort.data_reporter.cassandra.domain.gela.dao.GelaDao;
import com.climateconfort.data_reporter.cassandra.domain.gela.dao.impl.GelaDaoImpl;
import com.climateconfort.data_reporter.cassandra.domain.gela.model.Gela;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.dao.ParametroaDao;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.dao.impl.ParametroaDaoImpl;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.model.Parametroa;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.AddressTranslator;

public class CassandraConnector 
{
    private final String PROPERTY_FILE = "src\\main\\java\\com\\climateconfort\\resources\\application.properties";
    private final Integer PORT = 9042;

    private EraikinaDao         eraikinaDao;
    private GelaDao             gelaDao;
    private ParametroaDao       parametroaDao;

    private Map<InetSocketAddress, InetSocketAddress> addressMap;
    private Cluster             cluster;
    private Session             session;
    private Properties          prop;

    private int                 enpresa_id;

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
        this.enpresa_id = enpresa_id;
        this.addressMap = Map.of(
            new InetSocketAddress(addresses.get("node1")[0], PORT), new InetSocketAddress(addresses.get("node1")[1], PORT),
            new InetSocketAddress(addresses.get("node2")[0], PORT), new InetSocketAddress(addresses.get("node2")[1], PORT),
            new InetSocketAddress(addresses.get("node3")[0], PORT), new InetSocketAddress(addresses.get("node3")[1], PORT)
        );

        prop = new Properties();
        prop.load(new FileInputStream(PROPERTY_FILE));
        
        /* Konektatu lehenengo nodoaren IP publikora */
        connect(addresses.get("node1")[1]);

        eraikinaDao = new EraikinaDaoImpl(session);
        gelaDao = new GelaDaoImpl(session);
        parametroaDao = new ParametroaDaoImpl(session); 
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
        String database = prop.getProperty("spring.datasource.cassandra_database");
        session.execute("use " + database + ";");

        List<Eraikina> eraikinaList = eraikinaDao.findByEnpresaId(enpresa_id);

        Map<Integer, Map<Integer, Map<String, Float[]>>> eraikinaMap = new HashMap<>();
        for (Eraikina iEraikina : eraikinaList)
        {
            List<Gela> gelaList = gelaDao.findByEraikinaId(iEraikina.getId());

            Map<Integer, Map<String, Float[]>> gelaMap = new HashMap<>();
            for (Gela iGela : gelaList)
            {
                List<Parametroa> parametroaList = parametroaDao.findByGelaId(iGela.getId()); 
                
                Map<String, Float[]> parametroaMap = new HashMap<>();
                for (Parametroa iParametroa : parametroaList)
                {
                    parametroaMap.put(iParametroa.getMota(), new Float[] {iParametroa.getBalio_min(), iParametroa.getBalio_max()});
                }
                gelaMap.put(iGela.getId(), parametroaMap);
            }
            eraikinaMap.put(iEraikina.getId(), gelaMap);
        }
        return eraikinaMap;
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