package com.climateconfort.data_reporter.cassandra;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.climateconfort.data_reporter.cassandra.domain.eraikina.Eraikina;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.EraikinaDao;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.EraikinaMapperBuilder;
import com.climateconfort.data_reporter.cassandra.domain.gela.Gela;
import com.climateconfort.data_reporter.cassandra.domain.gela.GelaDao;
import com.climateconfort.data_reporter.cassandra.domain.gela.GelaMapperBuilder;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.Parametroa;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.ParametroaDao;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.ParametroaMapperBuilder;
import com.datastax.oss.driver.api.core.CqlSession;

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
    public void close() {
        session.close();
    }
}