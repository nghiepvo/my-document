package nv.streamingdata.debezium.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nv.streamingdata.debezium.entity.Customer;
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
