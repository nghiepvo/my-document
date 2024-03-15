package nv.streaming.pgdebeziumkafka.config;

import io.debezium.connector.postgresql.PostgresConnectorConfig;
import io.debezium.embedded.EmbeddedEngine;
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
    @Value("${financial.datasource.host}")
    private String financialDbHost;

    @Value("${financial.datasource.database}")
    private String financialDbName;

    @Value("${financial.datasource.port}")
    private String financialDbPort;

    @Value("${financial.datasource.username}")
    private String financialDbUsername;

    @Value("${financial.datasource.password}")
    private String financialDbPassword;

    /**
     * Financial Database Connector Configuration
     */
    @Bean
    public io.debezium.config.Configuration customerConnector() throws IOException {
        File offsetStorageTempFile = File.createTempFile("offsets_", ".dat");

        return io.debezium.config.Configuration.create()
                .with(EmbeddedEngine.ENGINE_NAME, "pg-connector")
                .with(EmbeddedEngine.CONNECTOR_CLASS, "io.debezium.connector.postgresql.PostgresConnector")
                .with(EmbeddedEngine.OFFSET_STORAGE, "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with(EmbeddedEngine.OFFSET_STORAGE_FILE_FILENAME, offsetStorageTempFile.getAbsolutePath())
                .with(EmbeddedEngine.OFFSET_FLUSH_INTERVAL_MS, 60000)

//                .with(EmbeddedEngine.OFFSET_STORAGE, "org.apache.kafka.connect.storage.MemoryOffsetBackingStore")
                .with(PostgresConnectorConfig.HOSTNAME, financialDbHost)
                .with(PostgresConnectorConfig.PORT, financialDbPort)
                .with(PostgresConnectorConfig.USER, financialDbUsername)
                .with(PostgresConnectorConfig.PASSWORD, financialDbPassword)
                .with(PostgresConnectorConfig.DATABASE_NAME, financialDbName)

                .with(PostgresConnectorConfig.SCHEMA_INCLUDE_LIST, "public")
                .with(PostgresConnectorConfig.TABLE_INCLUDE_LIST, "public.transactions")
                .with(PostgresConnectorConfig.PLUGIN_NAME, "pgoutput")
                .with(PostgresConnectorConfig.TOPIC_PREFIX, "cdc")
                .with(PostgresConnectorConfig.PUBLICATION_AUTOCREATE_MODE, "all_tables")
                .with(PostgresConnectorConfig.SLOT_NAME, "dbz_" + financialDbName+"_listener")
                .with(PostgresConnectorConfig.INCLUDE_SCHEMA_CHANGES, "false")
                .build();
    }
}