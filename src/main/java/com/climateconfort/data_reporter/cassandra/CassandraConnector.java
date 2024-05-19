package com.climateconfort.data_reporter.cassandra;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

@PropertySource("application.properties")
public class CassandraConnector {
    Properties prop;
    @Value("${spring.datasource.cassandra_ip}") private String CASSANDRA_HOST;
    
    private final int CASSANDRA_PORT = 0000;
    private final String CASSANDRA_DATABASE = "database";
    private final String CASSANDRA_USERNAME = "user";
    private final String CASSANDRA_PASSWORD = "user";
    private final String QUERY = "SELECT columna1, columna2 FROM tabla_de_ejemplo";
    Map<String, Integer[]> parameters;

    /**
     * @brief   CassandraConnector class constructor
     */
    public CassandraConnector() 
    {
        prop = new Properties();
        parameters = new HashMap<>();
    }

    /**
     * @brief   
     */
    public void getParameters() 
    {
        /* Usar uno de los metodos para obtener la IP */
        System.out.println(prop.getProperty("spring.datasource.cassandra_ip"));
        System.out.println(CASSANDRA_HOST);


        try (CqlSession session = CqlSession.builder()
            .addContactPoint(new InetSocketAddress(CASSANDRA_HOST, CASSANDRA_PORT))
            .withLocalDatacenter(CASSANDRA_DATABASE)  // Reemplaza con el nombre de tu datacenter
            .withAuthCredentials(CASSANDRA_USERNAME, CASSANDRA_PASSWORD)
            .withKeyspace("nombre_del_keyspace")  // Reemplaza con el nombre de tu keyspace si deseas conectarte a uno específico
            .build())
        {
            ResultSet resultSet = session.execute(SimpleStatement.newInstance(QUERY));

            for (Row row : resultSet) 
            {    
                /* Definir tablas de contenido */
                int paramtero = row.getInt("parametro1");
                String paramtero2 = row.getString("parametro1");

                /* Primero printear valores, para ver si funciona */
            }
        }
    }

}