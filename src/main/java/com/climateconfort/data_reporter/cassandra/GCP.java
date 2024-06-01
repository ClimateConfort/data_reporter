package com.climateconfort.data_reporter.cassandra;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.AggregatedListInstancesRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesScopedList;
import com.google.cloud.compute.v1.InstancesSettings;
import com.google.cloud.compute.v1.NetworkInterface;

public class GCP 
{
    private final String CREDENTIALS_PATH = "src/main/java/com/climateconfort/resources/gcp_certificate/pbl6-422712-4d2d1628f0a5.json";
    private final String PROJECT = "pbl6-422712";
    private HashMap<String, String[]> IP_map;

    public GCP() 
    { 
        IP_map = new HashMap<>();
    }

    public Map<String, String[]> listInstances() throws IOException 
    {
        InputStream iStream = new FileInputStream(CREDENTIALS_PATH);

        GoogleCredentials credentials = GoogleCredentials.fromStream(iStream);

        // Configurar el cliente de Compute Engine con las credenciales e iniciarlo
        InstancesSettings instancesSettings = InstancesSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
        InstancesClient instancesClient = InstancesClient.create(instancesSettings);

        AggregatedListInstancesRequest aggregatedListInstancesRequest = AggregatedListInstancesRequest
                .newBuilder()
                .setProject(PROJECT)
                .build();

        InstancesClient.AggregatedListPagedResponse response = instancesClient
                .aggregatedList(aggregatedListInstancesRequest);

        for (Map.Entry<String, InstancesScopedList> zoneInstances : response.iterateAll()) {
            String zone = zoneInstances.getKey();
            if (!zoneInstances.getValue().getInstancesList().isEmpty()) 
            {
                System.out.printf("Instances at %s: ", zone.substring(zone.lastIndexOf('/') + 1));
                for (Instance instance : zoneInstances.getValue().getInstancesList()) {
                    System.out.println(instance.getName());
                    String[] ip_list = new String[2];

                    for (NetworkInterface networkInterface : instance.getNetworkInterfacesList()) {
                        System.out.println("Internal IP: " + networkInterface.getNetworkIP());
                        ip_list[0] = networkInterface.getNetworkIP();
                        if (!networkInterface.getAccessConfigsList().isEmpty()) {
                            System.out.println("External IP: " + networkInterface.getAccessConfigsList().get(0).getNatIP());
                            ip_list[1] = networkInterface.getAccessConfigsList().get(0).getNatIP();
                        }
                    }
                    IP_map.put(zone, ip_list);
                    IP_map.put(instance.getName(), ip_list);
                }
            }
        }
        return IP_map;
    }

}