# data_reporter
## Description
It manages all the sensor data of a bussiness.

## Installation
First, install the [ClimateConfort/common](https://github.com/ClimateConfort/common) library:

```
$> git clone https://github.com/ClimateConfort/common.git
$> cd common
$> mvn clean install
```

After installing the library, clone this repo and create an executable jar:
```
$> git clone https://github.com/ClimateConfort/data_reporter.git
$> cd data_reporter
$> mvn clean package -DskipTests
```

The generated jar will be located in `target/data_reporter-1.0.0-jar-with-dependencies.jar`:


## Usage
The usage can be seen by executing the following command:

```
$> java -jar target/data_reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -h
usage: data_reporter
 -h,--help               Show help
 -p,--properties <arg>   Properties file path
 -v,--version            Show version
```

The [properties file](config/conf.properties) is located under `config/conf.properties`.

For TLS, extra content will be provided to be able to run it. This content will be a Docker image with necessary client-side keys and certificates, that must be specified in the properties file.

Example execution:
```
$> java -jar target/data_reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -p config/application.properties 
21:35:37.066 [main] INFO  com.datastax.oss.driver.internal.core.DefaultMavenCoordinates - DataStax Java driver for Apache Cassandra(R) (com.datastax.oss:java-driver-core) version 4.17.0
...
```

**DISCLAIMER: ONLY TESTED IN LINUX**

## Configuration File
These are the configuration file options:

- `climateconfort.client_id`: This is the Room ID where the sensor is located.
- `climateconfort.publishers`: A list of publisher ID (buildingID-roomID) separated by commas. Eg. "1-1,1-2,1-4".
- `cassandra.username`: Cassandra database username.
- `cassandra.password`: Cassandra database password.
- `cassandra.datacenter`: Cassandra database datacenter name.
- `cassandra.keyspace`: Cassandra database keyspace.
- `cassandra.port`: Cassandra database port.
- `cassandra.nodes`: A list of Cassandra cluster endpoints.
- `kafka.ip`: Kafka cluster endpoint IP.
- `kafka.port`: Kafka port.
- `kafka.connect.url`: Kafka Connect API URL.
- `kafka.connect.hdfs.url`: Kafka Connect HDFS url.
- `kafka.connect.hadoop.conf.dir`: Kafka Connect hadoop configuration directory.
- `kafka.connect.hadoop.home`: Kafka Connect Hadoop installation path.
- `kafka.schema_registry.url`: Kafka Connect schema registry URL.
- `kafka.request.tiemout.ms`: Kafka Tiemout time in miliseconds.
- `rabbitmq.server.ip`: The RabbitMQ server IP.
- `rabbitmq.server.port`: The RabbitMQ server port.
- `rabbitmq.server.user`: The RabbitMQ server user name.
- `rabbitmq.server.password`: The RabbitMQ server password.
- `rabbitmq.tls.pkcs12_key_path`: The PKCS12 key path.
- `rabbitmq.tls.pcks12_password`: The PKCS12 key password.
- `rabbitmq.tls.java_key_store_path`: The Java Key Store path.
- `rabbitmq.tls.java_key_store_password`: The Java Key Store password.

For a working example look under `config/conf.properties`