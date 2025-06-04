package com.webbanking.notificationservice.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.webbanking.notificationservice.listener", "com.webbanking.notificationservice.config", "com.webbanking.notificationservice.dto"})
@EnableKafka // Enable Kafka listener functionalities
public class NotificationKafkaListenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationKafkaListenerApplication.class, args);
        System.out.println("NotificationService Kafka Listener started..."); // Added for quick startup verification
    }

} 