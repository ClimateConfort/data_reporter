package com.climateconfort.data_reporter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.climateconfort.data_reporter.cassandra.GCP;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AggregatedListInstancesRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesScopedList;
import com.google.cloud.compute.v1.InstancesSettings;
import com.google.cloud.compute.v1.NetworkInterface;

class GCPTest {
    @Mock
    private GoogleCredentials mockCredentials;

    @Mock
    private InstancesClient mockInstancesClient;

    @Mock
    private InstancesClient.AggregatedListPagedResponse mockResponse;

    @Mock
    private Instance mockInstance;

    @Mock
    private NetworkInterface mockNetworkInterface;

    @Mock
    private AccessConfig mockAccessConfig;

    @Mock
    private InstancesScopedList mockInstancesScopedList;

    private GCP gcp;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        gcp = new GCP();

        MockedStatic<InstancesClient> mockedInstancesClient = mockStatic(InstancesClient.class);

        mockedInstancesClient.when(() -> InstancesClient.create(any(InstancesSettings.class)))
                .thenReturn(mockInstancesClient);
        when(mockInstancesClient.aggregatedList(any(AggregatedListInstancesRequest.class))).thenReturn(mockResponse);

        when(mockInstance.getName()).thenReturn("test-instance");
        when(mockNetworkInterface.getNetworkIP()).thenReturn("10.0.0.1");
        when(mockAccessConfig.getNatIP()).thenReturn("35.0.0.1");
        when(mockNetworkInterface.getAccessConfigsList()).thenReturn(Collections.singletonList(mockAccessConfig));
        when(mockInstance.getNetworkInterfacesList()).thenReturn(Collections.singletonList(mockNetworkInterface));

        when(mockInstancesScopedList.getInstancesList()).thenReturn(List.of(mockInstance));
        Iterable<Map.Entry<String, InstancesScopedList>> mockZoneInstances;
        Map.Entry<String, InstancesScopedList> entry = new AbstractMap.SimpleEntry<>("zones/us-central1-a",
                mockInstancesScopedList);
        mockZoneInstances = Collections.singletonList(entry);
        when(mockResponse.iterateAll()).thenReturn(mockZoneInstances);
    }

    @Test
    void testListInstances() throws IOException {
        Map<String, String[]> result = gcp.listInstances();

        assertNotNull(result);
        assertTrue(result.containsKey("test-instance"));
        assertArrayEquals(new String[] { "10.0.0.1", "35.0.0.1" }, result.get("test-instance"));
    }

}
