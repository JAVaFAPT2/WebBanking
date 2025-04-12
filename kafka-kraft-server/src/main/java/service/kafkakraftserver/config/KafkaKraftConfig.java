package service.kafkakraftserver.config;


import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

@Configuration
public class KafkaKraftConfig {
    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        return new KafkaProducer<>(kafkaProducerConfig());
    }

    @Bean
    public KafkaConsumer<String, String> kafkaConsumer() {
        return new KafkaConsumer<>(kafkaConsumerConfig());
    }

    private Properties kafkaProducerConfig() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");  // KRaft mode with Kafka broker running on localhost
        props.put("key.serializer", StringSerializer.class);
        props.put("value.serializer", StringSerializer.class);
        return props;
    }

    private Properties kafkaConsumerConfig() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", StringDeserializer.class);
        return props;
    }
}
