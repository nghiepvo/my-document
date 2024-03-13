package nv.streamingdata.debezium.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.data.Envelope;
import nv.streamingdata.debezium.entity.Customer;
import nv.streamingdata.debezium.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void replicateData(Map<String, Object> customerData, Envelope.Operation operation) {
        final ObjectMapper mapper = new ObjectMapper();
        final Customer customer = mapper.convertValue(customerData, Customer.class);

        if (Envelope.Operation.DELETE == operation) {
            customerRepository.deleteById(customer.getId());
        } else {
            customerRepository.save(customer);
        }
    }
}