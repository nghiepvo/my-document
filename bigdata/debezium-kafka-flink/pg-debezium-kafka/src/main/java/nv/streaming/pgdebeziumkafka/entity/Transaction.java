package nv.streaming.pgdebeziumkafka.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private String transaction_id;
    private String user_id;
    private Timestamp timestamp;
    private double amount;
    private String currency;
    private String city;
    private String country;
    private String merchant_name;
    private String payment_method;
    private String ip_address;
    private String voucher_code;
    private String affiliate_id;
}
