package service.monitorservice.service;

import lombok.Getter;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Service for interacting with the Kafka KRaft-based service registry.
 * This service provides methods to discover and interact with registered services.
 */
@Service
public class ServiceRegistryService {

    private final KafkaAdmin kafkaAdmin;

    @Value("${service-registry.topic}")
    private String serviceRegistryTopic;

    @Value("${service-registry.timeout}")
    private long timeout;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private KafkaConsumer<String, String> consumer;
    private Thread consumerThread;
    private volatile boolean running = true;

    // Map to store service instances
    private final Map<String, List<ServiceInstance>> serviceInstances = new HashMap<>();

    @Autowired
    public ServiceRegistryService(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;

        // For demonstration, populate with some example services
        // In a real implementation, this would be populated by consuming from the service registry topic
        initializeServices();
    }

    @PostConstruct
    public void init() {
        // Initialize Kafka consumer
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(props);

        // Start consumer thread to listen for service registry updates
        consumerThread = new Thread(this::consumeServiceRegistryTopic);
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    @PreDestroy
    public void cleanup() {
        running = false;
        if (consumer != null) {
            consumer.close();
        }
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }

    /**
     * Consumer thread that listens to the service registry topic.
     * This method actively uses the serviceRegistryTopic field.
     */
    private void consumeServiceRegistryTopic() {
        try {
            // Check if topic exists, if not, we'll use the example services
            try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
                ListTopicsResult topics = adminClient.listTopics();
                Set<String> topicNames = topics.names().get(timeout, TimeUnit.MILLISECONDS);

                if (topicNames.contains(serviceRegistryTopic)) {
                    consumer.subscribe(Collections.singletonList(serviceRegistryTopic));

                    while (running) {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                        for (ConsumerRecord<String, String> record : records) {
                            try {
                                processServiceRegistryRecord(record);
                            } catch (Exception e) {
                                System.err.println("Error processing record: " + e.getMessage());
                            }
                        }
                        consumer.commitSync();
                    }
                } else {
                    // Log that we're using example services because the topic doesn't exist
                    System.out.println("Service registry topic '" + serviceRegistryTopic + "' not found. Using example services.");
                }
            }
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error in service registry consumer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                consumer.close();
            } catch (Exception e) {
                System.err.println("Error closing consumer: " + e.getMessage());
            }
        }
    }
    /**
     * Process a record from the service registry topic.
     * This would parse the service registration/deregistration message and update the serviceInstances map.
     */
    private void processServiceRegistryRecord(ConsumerRecord<String, String> record) {
        // In a real implementation, this would parse JSON or other format
        // and update the serviceInstances map accordingly

        // Example implementation (assuming key is serviceName and value is JSON with instance details)
        String serviceName = record.key();
        String value = record.value();

        // Simple parsing for demonstration - in reality, use JSON parsing
        if (value != null && !value.isEmpty()) {
            if (value.startsWith("register:")) {
                // Registration message
                String[] parts = value.substring(9).split(",");
                if (parts.length >= 3) {
                    String id = parts[0];
                    String url = parts[1];
                    Map<String, String> metadata = new HashMap<>();
                    for (int i = 2; i < parts.length; i++) {
                        String[] kv = parts[i].split("=");
                        if (kv.length == 2) {
                            metadata.put(kv[0], kv[1]);
                        }
                    }
                    addServiceInstance(serviceName, id, url, metadata);
                }
            } else if (value.startsWith("deregister:")) {
                // Deregistration message
                String id = value.substring(11);
                removeServiceInstance(serviceName, id);
            }
        }
    }

    /**
     * Remove a service instance.
     */
    private void removeServiceInstance(String serviceName, String id) {
        List<ServiceInstance> instances = serviceInstances.get(serviceName);
        if (instances != null) {
            instances.removeIf(instance -> instance.id().equals(id));
            if (instances.isEmpty()) {
                serviceInstances.remove(serviceName);
            }
        }
    }

    /**
     * Get all registered service names.
     */
    public List<String> getAllServices() {
        return new ArrayList<>(serviceInstances.keySet());
    }

    /**
     * Get an instance of a specific service.
     * In a real implementation, this would use load balancing to select an instance.
     */
    public ServiceInstance getServiceInstance(String serviceName) {
        List<ServiceInstance> instances = serviceInstances.get(serviceName);
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        // Simple round-robin or first instance selection
        return instances.getFirst();
    }

    /**
     * Check if Kafka KRaft server is healthy.
     */
    public boolean isKafkaHealthy() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            // Check if we can get cluster info within timeout
            clusterResult.clusterId().get(timeout, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }

    /**
     * Check if the service registry topic exists.
     * This method actively uses the serviceRegistryTopic field.
     */
    public boolean doesServiceRegistryTopicExist() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ListTopicsResult topics = adminClient.listTopics();
            Set<String> topicNames = topics.names().get(timeout, TimeUnit.MILLISECONDS);
            return topicNames.contains(serviceRegistryTopic);
        } catch (Exception e) {
            return false;
        }
    }

    /**
         * Service instance representation.
         */
        @Getter
        public record ServiceInstance(String id, String serviceName, String url, Map<String, String> metadata) {

    }

    /**
     * Initialize with example services for demonstration.
     * In a real implementation, this would be populated from Kafka.
     */
    private void initializeServices() {
        // API Gateway
        addServiceInstance("api-gateway", "api-gateway-1", "http://api-gateway:8080", Map.of("version", "1.0"));

        // User Service
        addServiceInstance("user-service", "user-service-1", "http://user-service:8081", Map.of("version", "1.0"));

        // Account Service
        addServiceInstance("account-service", "account-service-1", "http://account-service:8082", Map.of("version", "1.0"));

        // Transaction Service
        addServiceInstance("transaction-service", "transaction-service-1", "http://transaction-service:8083", Map.of("version", "1.0"));

        // Notification Service
        addServiceInstance("notification-service", "notification-service-1", "http://notification-service:8084", Map.of("version", "1.0"));
    }

    /**
     * Helper method to add a service instance.
     */
    private void addServiceInstance(String serviceName, String id, String url, Map<String, String> metadata) {
        ServiceInstance instance = new ServiceInstance(id, serviceName, url, metadata);
        serviceInstances.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(instance);
    }

}
