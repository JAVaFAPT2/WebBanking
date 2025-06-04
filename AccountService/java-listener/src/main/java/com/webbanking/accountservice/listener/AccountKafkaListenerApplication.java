package com.webbanking.accountservice.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka // Enable Kafka listener functionalities
public class AccountKafkaListenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountKafkaListenerApplication.class, args);
    }

} 