package com.climateconfort.data_reporter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import com.climateconfort.data_reporter.cassandra.CassandraConnector;
import com.climateconfort.data_reporter.cassandra.GCP;

public class Main {
    
    public static void main(String[] args) throws FileNotFoundException, IOException 
    {
        GCP gcp = new GCP();
        Map<String, String[]> map = gcp.listInstances();

        CassandraConnector cassandraConnector = new CassandraConnector(1, map);
        Map<Integer, Map<Integer, Map<String, Float[]>>> map2 = cassandraConnector.getParameters();
        cassandraConnector.close();
    }
}