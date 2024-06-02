package com.climateconfort;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.climateconfort.data_reporter.cassandra.CassandraConnector;
import com.climateconfort.data_reporter.cassandra.CassandraConnector.PublicIpAddressTranslator;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.dao.EraikinaDao;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.dao.impl.EraikinaDaoImpl;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.model.Eraikina;
import com.climateconfort.data_reporter.cassandra.domain.gela.dao.GelaDao;
import com.climateconfort.data_reporter.cassandra.domain.gela.dao.impl.GelaDaoImpl;
import com.climateconfort.data_reporter.cassandra.domain.gela.model.Gela;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.dao.ParametroaDao;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.dao.impl.ParametroaDaoImpl;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.model.Parametroa;

import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Cluster;

class CassandraConnectorTest {
    CassandraConnector cassandraConnector;

    Map<String, String[]> mockAddress;

    @Mock
    Cluster mockCluster;

    @Mock
    Builder mockBuilder;

    @Mock
    Session mockSession;

    @Mock
    ResultSet mockEraikinResultSet;

    @Mock
    ResultSet mockGelaResultSet;

    @Mock
    ResultSet mockParametroResultSet;

    @Mock
    EraikinaDao mockEraikinaDao;

    @Mock
    EraikinaDaoImpl mockEraikinaDaoImpl;

    @Mock
    GelaDao mockGelaDao;

    @Mock
    GelaDaoImpl mockGelaDaoImpl;

    @Mock
    ParametroaDao mockParametroaDao;

    @Mock
    ParametroaDaoImpl mockParametroaDaoImpl;

    @BeforeEach
    public void setUp() throws FileNotFoundException, IOException {
        MockitoAnnotations.openMocks(this);

        MockedStatic<Cluster> staticMockCluster = mockStatic(Cluster.class);

        mockAddress = new HashMap<>();
        mockAddress.put("node1", new String[] { "10.0.0.1", "35.0.0.1" });
        mockAddress.put("node2", new String[] { "10.0.0.2", "35.0.0.2" });
        mockAddress.put("node3", new String[] { "10.0.0.3", "35.0.0.3" });

        staticMockCluster.when(() -> Cluster.builder()).thenReturn(mockBuilder);
        when(mockBuilder.addContactPoint(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.withPort(anyInt())).thenReturn(mockBuilder);
        when(mockBuilder.withCredentials(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.withAddressTranslator(any(PublicIpAddressTranslator.class))).thenReturn(mockBuilder);

        when(mockBuilder.build()).thenReturn(mockCluster);
        when(mockCluster.connect()).thenReturn(mockSession);

        cassandraConnector = new CassandraConnector(1, mockAddress);
    }

    @Test
    void getParametersTest() {
        when(mockEraikinaDao.findByEnpresaId(anyInt()))
                .thenReturn(Collections.singletonList(new Eraikina(1, "LOKALIZAZIOA", 1)));
        when(mockGelaDao.findByEraikinaId(anyInt())).thenReturn(Collections.singletonList(new Gela(1, 1)));
        when(mockParametroaDao.findByGelaId(anyInt()))
                .thenReturn(Collections.singletonList(new Parametroa(1, "tmp", 10.0f, 20.0f, 1)));

        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockSession.execute(anyString())).thenReturn(mockResultSet);
        Row mockRow = mock(Row.class);
        when(mockResultSet.all()).thenReturn(Collections.singletonList(mockRow));

        // Map<Integer, Map<Integer, Map<String, Float[]>>> parametersMap =
        // cassandraConnector.getParameters();

        // assertEquals(parametersMap.size(), 1);
    }

}