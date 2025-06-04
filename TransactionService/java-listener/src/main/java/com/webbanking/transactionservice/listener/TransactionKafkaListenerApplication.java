package com.webbanking.transactionservice.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka // Enable Kafka listener functionalities
public class TransactionKafkaListenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionKafkaListenerApplication.class, args);
    }

} 