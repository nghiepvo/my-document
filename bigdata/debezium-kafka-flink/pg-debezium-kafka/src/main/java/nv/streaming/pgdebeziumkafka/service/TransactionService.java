package nv.streaming.pgdebeziumkafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.data.Envelope;
import nv.streaming.pgdebeziumkafka.entity.Transaction;
import nv.streaming.pgdebeziumkafka.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void replicateData(Map<String, Object> transactionData, Envelope.Operation operation) {
        final ObjectMapper mapper = new ObjectMapper();
        final Transaction transaction = mapper.convertValue(transactionData, Transaction.class);

        if (Envelope.Operation.DELETE == operation) {
            transactionRepository.deleteById(transaction.getTransaction_id());
        } else {
            transactionRepository.save(transaction);
        }
    }

    public void handleDownOrInit(Timestamp downTime, Map<String, Object> transactionData) {
        final ObjectMapper mapper = new ObjectMapper();
        final Transaction transaction = mapper.convertValue(transactionData, Transaction.class);
        if (transaction.getTimestamp().before(downTime)) {
            transactionRepository.save(transaction);
        }
    }
}