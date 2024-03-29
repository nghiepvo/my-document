package nv.streamingdata.debezium.config;

import io.debezium.connector.mysql.MySqlConnectorConfig;
import io.debezium.embedded.EmbeddedEngine;
import io.debezium.relational.history.MemorySchemaHistory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
public class DebeziumConnectorConfig {

    /**
     * Database details.
     */
    @Value("${customer.datasource.host}")
    private String customerDbHost;

    @Value("${customer.datasource.database}")
    private String customerDbName;

    @Value("${customer.datasource.port}")
    private String customerDbPort;

    @Value("${customer.datasource.username}")
    private String customerDbUsername;

    @Value("${customer.datasource.password}")
    private String customerDbPassword;

    /**
     * Customer Database Connector Configuration
     */
    @Bean
    public io.debezium.config.Configuration customerConnector() throws IOException {
        File offsetStorageTempFile = File.createTempFile("offsets_", ".dat");

        return io.debezium.config.Configuration.create()
                .with(EmbeddedEngine.ENGINE_NAME, "customer-mysql-connector")
                .with(EmbeddedEngine.CONNECTOR_CLASS, "io.debezium.connector.mysql.MySqlConnector")
                .with(EmbeddedEngine.OFFSET_STORAGE, "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with(EmbeddedEngine.OFFSET_STORAGE_FILE_FILENAME, offsetStorageTempFile.getAbsolutePath())
//                .with(EmbeddedEngine.OFFSET_STORAGE, "org.apache.kafka.connect.storage.MemoryOffsetBackingStore")
                .with(MySqlConnectorConfig.SCHEMA_HISTORY, MemorySchemaHistory.class.getName())
                .with(MySqlConnectorConfig.HOSTNAME, customerDbHost)
                .with(MySqlConnectorConfig.PORT, customerDbPort)
                .with(MySqlConnectorConfig.USER, customerDbUsername)
                .with(MySqlConnectorConfig.PASSWORD, customerDbPassword)
                .with(MySqlConnectorConfig.DATABASE_NAME, customerDbName)
                .with(MySqlConnectorConfig.DATABASE_INCLUDE_LIST, customerDbName)
//                .with(MySqlConnectorConfig.TABLE_EXCLUDE_LIST, "customer")
                .with(MySqlConnectorConfig.TOPIC_PREFIX, "cdc")
                .with(MySqlConnectorConfig.INCLUDE_SCHEMA_CHANGES, "false")
                .with("database.allowPublicKeyRetrieval", "true")
                .with(MySqlConnectorConfig.SERVER_ID, "10181")
                .build();
    }
}