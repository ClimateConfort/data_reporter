package com.climateconfort.data_reporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.climateconfort.data_reporter.cassandra.CassandraConnector;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.Eraikina;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.EraikinaDao;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.EraikinaMapper;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.EraikinaMapperBuilder;
import com.climateconfort.data_reporter.cassandra.domain.gela.Gela;
import com.climateconfort.data_reporter.cassandra.domain.gela.GelaDao;
import com.climateconfort.data_reporter.cassandra.domain.gela.GelaMapper;
import com.climateconfort.data_reporter.cassandra.domain.gela.GelaMapperBuilder;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.Parametroa;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.ParametroaDao;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.ParametroaMapper;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.ParametroaMapperBuilder;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.PagingIterable;

class CassandraConnectorTest {

    @Mock
    private CqlSessionBuilder builder;

    @Mock
    private CqlSession session;

    @Mock
    private EraikinaMapperBuilder eraikinaMapperBuilder;

    @Mock
    private EraikinaMapper eraikinaMapper;

    @Mock
    private EraikinaDao eraikinaDao;

    @Mock
    private GelaMapperBuilder gelaMapperBuilder;

    @Mock
    private GelaMapper gelaMapper;

    @Mock
    private GelaDao gelaDao;

    @Mock
    private ParametroaMapperBuilder parametroaMapperBuilder;

    @Mock
    private ParametroaMapper parametroaMapper;

    @Mock
    private ParametroaDao parametroaDao;

    @Mock
    private PagingIterable<Eraikina> pagingIterableEraikina;

    @Mock
    private PagingIterable<Gela> pagingIterableGela;

    @Mock
    private PagingIterable<Parametroa> pagingIterableParametroa;

    private CassandraConnector cassandraConnector;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        // Mocking the builder chain
        try (MockedStatic<CqlSession> mockedStatic = Mockito.mockStatic(CqlSession.class);
                MockedConstruction<EraikinaMapperBuilder> mockedConstructionEraikinaMapperBuilder = mockConstruction(
                        EraikinaMapperBuilder.class, (mock, context) -> when(mock.build()).thenReturn(eraikinaMapper));
                MockedConstruction<GelaMapperBuilder> mockedConstructionGelaMapperBuilder = mockConstruction(
                        GelaMapperBuilder.class, (mock, context) -> when(mock.build()).thenReturn(gelaMapper));
                MockedConstruction<ParametroaMapperBuilder> mockedConstructionParametroaMapperBuilder = mockConstruction(
                        ParametroaMapperBuilder.class,
                        (mock, context) -> when(mock.build()).thenReturn(parametroaMapper))) {
            mockedStatic.when(CqlSession::builder).thenReturn(builder);
            when(builder.addContactPoints(any(List.class))).thenReturn(builder);
            when(builder.withAuthCredentials(anyString(), anyString())).thenReturn(builder);
            when(builder.withLocalDatacenter(anyString())).thenReturn(builder);
            when(builder.withKeyspace(anyString())).thenReturn(builder);
            when(builder.build()).thenReturn(session);
            when(eraikinaMapper.eraikinaDao()).thenReturn(eraikinaDao);
            when(gelaMapper.gelaDao()).thenReturn(gelaDao);
            when(parametroaMapper.parametroaDao()).thenReturn(parametroaDao);
            cassandraConnector = new CassandraConnector(getProperties());
        }

        // setField(cassandraConnector, "session", session);
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("climateconfort.client_id", "1");
        properties.setProperty("cassandra.username", "cassandra");
        properties.setProperty("cassandra.password", "cassandra");
        properties.setProperty("cassandra.datacenter", "datacenter1");
        properties.setProperty("cassandra.keyspace", "test_keyspace");
        properties.setProperty("cassandra.port", "9042");
        properties.setProperty("cassandra.nodes", "127.0.0.1");
        return properties;
    }

    @Test
    void testGetParameters() {
        // Prepare test data
        Eraikina eraikina = generateEraikina(1, "Mondragon", 1);
        Gela gela = generateGela(1, 1);
        Parametroa parametroa = generateParametroa(1, "TYPE1", 0.9f, 0.9f, 1);

        // Mocking the behavior of DAO methods
        when(eraikinaDao.findAllByEnpresaId(1)).thenReturn(pagingIterableEraikina);
        when(gelaDao.findAllByEraikinaId(1)).thenReturn(pagingIterableGela);
        when(parametroaDao.findAllByGelaId(1)).thenReturn(pagingIterableParametroa);

        // Mocking PagingIterables
        when(pagingIterableEraikina.iterator()).thenReturn(Collections.singletonList(eraikina).iterator());
        when(pagingIterableGela.iterator()).thenReturn(Collections.singletonList(gela).iterator());
        when(pagingIterableParametroa.iterator()).thenReturn(Collections.singletonList(parametroa).iterator());

        // Call the method to test
        Map<Long, Map<Long, List<Parametroa>>> parameters = cassandraConnector.getParameters();

        // Verify the result
        assertEquals(1, parameters.size());
        assertEquals(1, parameters.get(1L).size());
        assertEquals(1, parameters.get(1L).get(1L).size());
        assertEquals(parametroa, parameters.get(1L).get(1L).get(0));

        // Verify interactions
        verify(eraikinaDao).findAllByEnpresaId(1);
        verify(gelaDao).findAllByEraikinaId(1);
        verify(parametroaDao).findAllByGelaId(1);
    }

    @Test
    void closeTest() {
        cassandraConnector.close();
        verify(session).close();
    }

    private Parametroa generateParametroa(int id, String mota, float balioMax, float balioMin, int gelaId) {
        Parametroa parametroa = new Parametroa();
        parametroa.setParametroaId(id);
        parametroa.setMota(mota);
        parametroa.setBalioMax(balioMax);
        parametroa.setBalioMin(balioMin);
        parametroa.setGelaId(gelaId);
        return parametroa;
    }

    private Gela generateGela(int id, int eraikinaId) {
        Gela gela = new Gela();
        gela.setGelaId(id);
        gela.setEraikinaId(eraikinaId);
        return gela;
    }

    private Eraikina generateEraikina(int id, String lokalizazioa, int enpresaId) {
        Eraikina eraikina = new Eraikina();
        eraikina.setEraikinaId(id);
        eraikina.setLokalizazioa(lokalizazioa);
        eraikina.setEnpresaId(enpresaId);
        return eraikina;
    }
}