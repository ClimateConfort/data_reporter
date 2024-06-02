package com.climateconfort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.climateconfort.data_reporter.cassandra.domain.eraikina.dao.impl.EraikinaDaoImpl;
import com.climateconfort.data_reporter.cassandra.domain.eraikina.model.Eraikina;
import com.climateconfort.data_reporter.cassandra.domain.gela.dao.impl.GelaDaoImpl;
import com.climateconfort.data_reporter.cassandra.domain.gela.model.Gela;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.dao.impl.ParametroaDaoImpl;
import com.climateconfort.data_reporter.cassandra.domain.parametroa.model.Parametroa;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

class DaoTest {
    @Mock
    private Session mockSession;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private Row mockRow;

    private EraikinaDaoImpl eraikinaDao;
    private GelaDaoImpl gelaDao;
    private ParametroaDaoImpl parametroaDao;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        eraikinaDao = new EraikinaDaoImpl(mockSession);
        gelaDao = new GelaDaoImpl(mockSession);
        parametroaDao = new ParametroaDaoImpl(mockSession);
    }

    @Test
    void findByEnpresaIdTest() {
        when(mockSession.execute(anyString(), eq(1))).thenReturn(mockResultSet);

        List<Row> mockRows = new ArrayList<>();
        mockRows.add(mockRow);

        when(mockResultSet.all()).thenReturn(mockRows);
        when(mockRow.getInt("id")).thenReturn(1);
        when(mockRow.getString("lokalizazioa")).thenReturn("Lokalizazioa1");

        List<Eraikina> result = eraikinaDao.findByEnpresaId(1);

        assertEquals(1, result.size());
        Eraikina eraikina = result.get(0);
        assertEquals(1, eraikina.getId());
        assertEquals("Lokalizazioa1", eraikina.getLokalizazioa());

        verify(mockSession).execute(anyString(), eq(1));
        verify(mockResultSet).all();
        verify(mockRow).getInt("id");
        verify(mockRow).getString("lokalizazioa");
    }

    @Test
    void findByEraikinaIdTest() {
        when(mockSession.execute(anyString(), eq(1))).thenReturn(mockResultSet);
        List<Row> mockRows = new ArrayList<>();
        mockRows.add(mockRow);

        when(mockResultSet.all()).thenReturn(mockRows);
        when(mockRow.getInt("id")).thenReturn(1);

        List<Gela> result = gelaDao.findByEraikinaId(1);

        assertEquals(1, result.size());
        Gela gela = result.get(0);
        assertEquals(1, gela.getId());

        verify(mockSession).execute(anyString(), eq(1));
        verify(mockResultSet).all();
        verify(mockRow).getInt("id");
    }

    @Test
    void testFindByGelaId() {
        when(mockSession.execute(anyString(), eq(1))).thenReturn(mockResultSet);
        List<Row> mockRows = new ArrayList<>();
        mockRows.add(mockRow);

        when(mockResultSet.all()).thenReturn(mockRows);
        when(mockRow.getInt("id")).thenReturn(1);
        when(mockRow.getFloat("balio_max")).thenReturn(100.0f);
        when(mockRow.getFloat("balio_min")).thenReturn(10.0f);
        when(mockRow.getString("mota")).thenReturn("Temperature");

        List<Parametroa> result = parametroaDao.findByGelaId(1);

        assertEquals(1, result.size());
        Parametroa parametroa = result.get(0);
        assertEquals(1, parametroa.getId());
        assertEquals(100.0f, parametroa.getBalioMax(), 0.01);
        assertEquals(10.0f, parametroa.getBalioMin(), 0.01);
        assertEquals("Temperature", parametroa.getMota());

        verify(mockSession).execute(anyString(), eq(1));
        verify(mockResultSet).all();
        verify(mockRow).getInt("id");
        verify(mockRow).getFloat("balio_max");
        verify(mockRow).getFloat("balio_min");
        verify(mockRow).getString("mota");
    }
}
