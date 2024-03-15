package nv.streaming.pgdebeziumkafka.repository;

import nv.streaming.pgdebeziumkafka.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
}
