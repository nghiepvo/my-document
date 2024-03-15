package nv.streaming.pgdebeziumkafka.listener;

import io.debezium.config.Configuration;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import lombok.extern.slf4j.Slf4j;

import nv.streaming.pgdebeziumkafka.service.ManagementDownTimeService;
import nv.streaming.pgdebeziumkafka.service.TransactionService;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.debezium.data.Envelope.FieldName.*;
import static io.debezium.data.Envelope.Operation;
import static java.util.stream.Collectors.toMap;
@Slf4j
@Component
public class DebeziumListener {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final TransactionService transactionService;
    private final ManagementDownTimeService managementDownTimeService;
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;

    public DebeziumListener(Configuration debeziumConnectorConfig, TransactionService transactionService, ManagementDownTimeService managementDownTimeService) {

        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(debeziumConnectorConfig.asProperties())

                .notifying(this::handleChangeEvent)
                .build();

        this.transactionService = transactionService;
        this.managementDownTimeService = managementDownTimeService;
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();

        log.info("Key = '" + sourceRecord.key() + "' value = '" + sourceRecord.value() + "'");

        Struct sourceRecordChangeValue= (Struct) sourceRecord.value();

        if (sourceRecordChangeValue != null) {
            Operation operation = Operation.forCode((String) sourceRecordChangeValue.get(OPERATION));

            if(operation != Operation.READ) {
                String record = operation == Operation.DELETE ? BEFORE : AFTER; // Handling Update & Insert operations.

                Struct struct = (Struct) sourceRecordChangeValue.get(record);
                Map<String, Object> payload = struct.schema().fields().stream()
                        .map(Field::name)
                        .filter(fieldName -> struct.get(fieldName) != null)
                        .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
                        .collect(toMap(Pair::getKey, Pair::getValue));

                this.transactionService.replicateData(payload, operation);
                log.info("Updated Data: {} with Operation: {}", payload, operation.name());
            } else {
                Struct struct = (Struct) sourceRecordChangeValue.get(Operation.READ.code());
                Map<String, Object> payload = struct.schema().fields().stream()
                        .map(Field::name)
                        .filter(fieldName -> struct.get(fieldName) != null)
                        .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
                        .collect(toMap(Pair::getKey, Pair::getValue));
                try {
                    this.transactionService.handleDownOrInit(this.managementDownTimeService.getDownTime(), payload);
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                }

            }
        }
    }

    @PostConstruct
    private void start() {
        log.info("Start DEBEZIUM service.");
        this.executor.execute(debeziumEngine);
    }

    @PreDestroy
    private void stop() throws IOException {
        log.info("Stop DEBEZIUM service.");
        if (this.debeziumEngine != null) {
            this.debeziumEngine.close();
        }
        this.managementDownTimeService.writeDownTime();
    }

}