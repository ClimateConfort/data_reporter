package com.climateconfort.data_reporter.cassandra;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.climateconfort.data_reporter.cassandra.domain.eraikina.Eraikina;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.EraikinaDao;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.EraikinaMapper;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.EraikinaMapperBuilder;
import com.climateconfort.data_reporter.cassandra.domain.gela.Gela;
import com.climateconfort.data_reporter.cassandra.domain.gela.GelaDao;
import com.climateconfort.data_reporter.cassandra.domain.gela.GelaMapperBuilder;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.Parametroa;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.ParametroaDao;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.ParametroaMapperBuilder;
import com.datastax.oss.driver.api.core.CqlSession;

// public class CassandraConnector {
//     private static final String PROPERTY_FILE = "src/main/java/com/climateconfort/resources/application.properties";
//     private static final int PORT = 9042;

//     private EraikinaDao eraikinaDao;
//     private GelaDao gelaDao;
//     private ParametroaDao parametroaDao;

//     private Map<InetSocketAddress, InetSocketAddress> addressMap;
//     private Cluster cluster;
//     private Session session;
//     private Properties prop;

//     private int enpresaId;

//     /**
//      * @throws IOException
//      * @throws FileNotFoundException
//      * 
//      * @params enpresa_id
//      * 
//      * @brief CassandraConnector konstruktorea
//      */
//     public CassandraConnector(int enpresaId, Map<String, String[]> addresses) throws IOException {
//         this.enpresaId = enpresaId;
//         this.addressMap = Map.of(
//                 new InetSocketAddress(addresses.get("node1")[0], PORT), new InetSocketAddress(addresses.get("node1")[1], PORT),
//                 new InetSocketAddress(addresses.get("node2")[0], PORT), new InetSocketAddress(addresses.get("node2")[1], PORT),
//                 new InetSocketAddress(addresses.get("node3")[0], PORT), new InetSocketAddress(addresses.get("node3")[1], PORT));

//         prop = new Properties();
//         prop.load(new FileInputStream(PROPERTY_FILE));

//         eraikinaDao = new EraikinaDaoImpl(session);
//         gelaDao = new GelaDaoImpl(session);
//         parametroaDao = new ParametroaDaoImpl(session);

//         /* Konektatu lehenengo nodoaren IP publikora */
//         connect(addresses.get("node1")[1]);
//     }

//     public void connect(String node) {
//         String user = prop.getProperty("spring.datasource.cassandra_user");
//         String password = prop.getProperty("spring.datasource.cassandra_password");

//         Builder builder = Cluster.builder()
//                 .addContactPoint(node)
//                 .withPort(PORT)
//                 .withCredentials(user, password)
//                 .withAddressTranslator(new PublicIpAddressTranslator(addressMap));

//         cluster = builder.build();
//         session = cluster.connect();
//     }

//     /**
//      * @brief Parametroak lortu Cassandra datu basetik eta Map bat sortu eraikin eta
//      *        gela guztiekin.
//      */
//     public Map<Integer, Map<Integer, Map<String, Float[]>>> getParameters() {
//         String database = prop.getProperty("spring.datasource.cassandra_database");
//         session.execute("use " + database + ";");

//         List<Eraikina> eraikinaList = eraikinaDao.findByEnpresaId(enpresaId);

//         Map<Integer, Map<Integer, Map<String, Float[]>>> eraikinaMap = new HashMap<>();
//         for (Eraikina eraikina : eraikinaList) {
//             List<Gela> gelaList = gelaDao.findByEraikinaId(eraikina.getId());

//             Map<Integer, Map<String, Float[]>> gelaMap = new HashMap<>();
//             for (Gela gela : gelaList) {
//                 List<Parametroa> parametroaList = parametroaDao.findByGelaId(gela.getId());

//                 Map<String, Float[]> parametroaMap = new HashMap<>();
//                 for (Parametroa parametroa : parametroaList) {
//                     parametroaMap.put(parametroa.getMota(),
//                             new Float[] { parametroa.getBalioMin(), parametroa.getBalioMax() });
//                 }
//                 gelaMap.put(gela.getId(), parametroaMap);
//             }
//             eraikinaMap.put(eraikina.getId(), gelaMap);
//         }
//         return eraikinaMap;
//     }

//     public Session getSession() {
//         return this.session;
//     }

//     public void close() {
//         session.close();
//         cluster.close();
//     }

//     public class PublicIpAddressTranslator implements AddressTranslator {
//         private final Map<InetSocketAddress, InetSocketAddress> addressMap;

//         public PublicIpAddressTranslator(Map<InetSocketAddress, InetSocketAddress> addressMap) {
//             this.addressMap = addressMap;
//         }

//         @Override
//         public InetSocketAddress translate(InetSocketAddress address) {
//             return addressMap.getOrDefault(address, address);
//         }

//         @Override
//         public void close() {
//         }

//         @Override
//         public void init(Cluster cluster) {
//         }
//     }

// }

public class CassandraConnector implements AutoCloseable {

    private final int clientId;
    private final CqlSession session;
    private final EraikinaDao eraikinaDao;
    private final GelaDao gelaDao;
    private final ParametroaDao parametroaDao;

    public CassandraConnector(Properties properties) {
        this.clientId = Integer.parseInt(properties.getProperty("climate_confort.client_id", "NaN"));
        String username = properties.getProperty("cassandra.username", "cassandra");
        String password = properties.getProperty("cassandra.password", "cassandra");
        String datacenterName = properties.getProperty("cassandra.datacenter", "datacenter1");
        String keyspace = properties.getProperty("cassandra.keyspace");
        int port = Integer.parseInt(properties.getProperty("cassandra.port", "9042"));
        List<InetSocketAddress> inetSocketAddressList = Arrays
                .asList(properties
                        .getProperty("cassandra.nodes")
                        .split(","))
                .stream()
                .map(nodeString -> new InetSocketAddress(nodeString, port))
                .toList();

        this.session = CqlSession.builder()
                .addContactPoints(inetSocketAddressList)
                .withAuthCredentials(username, password)
                .withLocalDatacenter(datacenterName)
                .withKeyspace(keyspace)
                .build();
        this.eraikinaDao = new EraikinaMapperBuilder(session)
                .build()
                .eraikinaDao();
        this.gelaDao = new GelaMapperBuilder(session)
                .build()
                .gelaDao();
        this.parametroaDao = new ParametroaMapperBuilder(session)
                .build()
                .parametroaDao();
    }

    public Map<Integer, Map<Integer, List<Parametroa>>> getParameters() {
        Map<Integer, Map<Integer, List<Parametroa>>> eraikinMap = new HashMap<>();
        for (Eraikina eraikina : eraikinaDao.findAllByEnpresaId(clientId)) {
            Map<Integer, List<Parametroa>> gelaMap = new HashMap<>();
            for (Gela gela : gelaDao.findAllByEraikinaId(eraikina.getId())) {
                List<Parametroa> parametroaList = new ArrayList<>();
                for (Parametroa parametroa : parametroaDao.findAllByGelaId(gela.getId())) {
                    parametroaList.add(parametroa);
                }
                gelaMap.put(gela.getId(), parametroaList);
            }
            eraikinMap.put(eraikina.getId(), gelaMap);
        }
        return eraikinMap;
    }

    @Override
    public void close() throws Exception {
        session.close();
    }
}