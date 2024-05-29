package com.climateconfort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.climateconfort.data_reporter.cassandra.CassandraConnector;
import com.climateconfort.data_reporter.cassandra.CassandraConnector.PublicIpAddressTranslator;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Cluster;

public class CassandraConnectorTest 
{
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
    List<Row> mockRow;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);

        MockedStatic<Cluster> staticMockCluster = mockStatic(Cluster.class); 

        mockAddress = new HashMap<>();
        mockAddress.put("node1", new String[] {"10.0.0.1", "35.0.0.1"});
        mockAddress.put("node2", new String[] {"10.0.0.2", "35.0.0.2"});
        mockAddress.put("node3", new String[] {"10.0.0.3", "35.0.0.3"});
        
        // mockCluster = mock(Cluster.class);
        mockBuilder = mock(Builder.class);
        mockSession = mock(Session.class);

        staticMockCluster.when(() -> Cluster.builder()).thenReturn(mockBuilder);
        when(mockBuilder.addContactPoint(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.withPort(anyInt())).thenReturn(mockBuilder);
        when(mockBuilder.withCredentials(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.withAddressTranslator(any(PublicIpAddressTranslator.class))).thenReturn(mockBuilder);

        when(mockBuilder.build()).thenReturn(mockCluster);
        when(mockCluster.connect()).thenReturn(mockSession);
    }

    @Test
    public void connectTest() throws FileNotFoundException, IOException
    {
        CassandraConnector cassandraConnector = new CassandraConnector(1, mockAddress);

        assertNotNull(cassandraConnector);
        assertEquals(cassandraConnector.getSession(), mockSession); 
    }

    @Test
    public void getParametersTest()
    {
        // mockRow = mock(Row.class);

        when(mockEraikinResultSet.iterator()).thenReturn(mockRow.iterator());

        when(mockSession.execute(anyString()))
            .thenReturn(null)
            .thenReturn(mockEraikinResultSet)
            .thenReturn(mockGelaResultSet)
            .thenReturn(mockParametroResultSet);


    }

}