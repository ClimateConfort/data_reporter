package com.climateconfort.data_reporter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import com.climateconfort.data_reporter.cassandra.CassandraConnector;
import com.climateconfort.data_reporter.cassandra.GCP;

public class Main {
    static CassandraConnector cs;
    public static void main(String[] args) throws FileNotFoundException, IOException 
    {
        GCP gcp = new GCP();
        Map<String, String[]> map = gcp.listInstances();

        CassandraConnector cassandraConnector = new CassandraConnector(1, map);
        //cassandraConnector.connect("35.238.37.29", 9042);
        // cassandraConnector.connectKeyspace();
        System.out.println(cassandraConnector.getSession().toString());
        cassandraConnector.close();
    }
}