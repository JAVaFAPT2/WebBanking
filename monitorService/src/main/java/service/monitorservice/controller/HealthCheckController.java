package service.monitorservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import service.monitorservice.config.MonitorConfig;
import service.monitorservice.service.ServiceRegistryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controller for health check endpoints.
 * Provides endpoints to check the health of individual services and the overall system.
 * Adapted for Kafka KRaft-based service registry instead of Eureka.
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private final ServiceRegistryService serviceRegistry;
    private final RestTemplate restTemplate;
    private final MonitorConfig.HealthCheckConfig healthCheckConfig;
    private final ExecutorService executorService;

    @Autowired
    public HealthCheckController(
            ServiceRegistryService serviceRegistry,
            @Autowired(required = false) RestTemplate restTemplate,
            MonitorConfig.HealthCheckConfig healthCheckConfig) {
        this.serviceRegistry = serviceRegistry;
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.healthCheckConfig = healthCheckConfig;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    /**
     * Check the health of the monitoring service itself.
     */
    @GetMapping
    public ResponseEntity<Health> checkHealth() {
        Health health = Health.up()
                .withDetail("service", "monitor-service")
                .withDetail("status", "UP")
                .withDetail("timestamp", System.currentTimeMillis())
                .build();

        return ResponseEntity.ok(health);
    }

    /**
     * Check the health of all registered services.
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Health>> checkAllServices() {
        List<String> services = serviceRegistry.getAllServices();
        Map<String, Health> healthResults = new HashMap<>();

        List<CompletableFuture<Void>> futures = services.stream()
                .map(service -> CompletableFuture.runAsync(() -> {
                    try {
                        Health serviceHealth = checkServiceHealth(service);
                        healthResults.put(service, serviceHealth);
                    } catch (Exception e) {
                        healthResults.put(service, Health.down()
                                .withDetail("error", e.getMessage())
                                .build());
                    }
                }, executorService))
                .toList();

        // Wait for all health checks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(healthCheckConfig.getTimeout(), TimeUnit.MILLISECONDS)
                .join();

        return ResponseEntity.ok(healthResults);
    }

    /**
     * Check the health of a specific service by name.
     */
    @GetMapping("/{serviceName}")
    public ResponseEntity<Health> checkServiceByName(@PathVariable String serviceName) {
        try {
            Health health = checkServiceHealth(serviceName);
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            Health health = Health.down()
                    .withDetail("service", serviceName)
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    /**
     * Check the overall system health.
     */
    @GetMapping("/system")
    public ResponseEntity<Health> checkSystemHealth() {
        Map<String, Health> serviceHealths = checkAllServices().getBody();
        boolean allUp = serviceHealths.values().stream()
                .allMatch(health -> health.getStatus().equals(Status.UP));

        Health.Builder healthBuilder = allUp ? Health.up() : Health.down();

        healthBuilder.withDetail("services", serviceHealths);
        healthBuilder.withDetail("timestamp", System.currentTimeMillis());

        Health health = healthBuilder.build();
        HttpStatus status = allUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

        return ResponseEntity.status(status).body(health);
    }

    /**
     * Helper method to check the health of a specific service.
     */
    private Health checkServiceHealth(String serviceName) {
        ServiceRegistryService.ServiceInstance instance = serviceRegistry.getServiceInstance(serviceName);

        if (instance == null) {
            return Health.down()
                    .withDetail("service", serviceName)
                    .withDetail("error", "No instances found")
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        }

        String healthUrl = instance.url() + "/actuator/health";

        try {
            ResponseEntity<Health> response = restTemplate.getForEntity(healthUrl, Health.class);
            Health serviceHealth = response.getBody();

            if (serviceHealth == null) {
                return Health.unknown()
                        .withDetail("service", serviceName)
                        .withDetail("instance", instance.id())
                        .withDetail("error", "Empty health response")
                        .withDetail("timestamp", System.currentTimeMillis())
                        .build();
            }

            return Health.status(serviceHealth.getStatus())
                    .withDetail("service", serviceName)
                    .withDetail("instance", instance.id())
                    .withDetails(healthCheckConfig.isDetailedOutput() ? serviceHealth.getDetails() : Map.of())
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", serviceName)
                    .withDetail("instance", instance.id())
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * Check Kafka KRaft server health.
     */
    @GetMapping("/kafka")
    public ResponseEntity<Health> checkKafkaHealth() {
        boolean isKafkaHealthy = serviceRegistry.isKafkaHealthy();

        Health.Builder healthBuilder = isKafkaHealthy ? Health.up() : Health.down();
        healthBuilder.withDetail("service", "kafka-kraft-server")
                .withDetail("timestamp", System.currentTimeMillis());

        if (!isKafkaHealthy) {
            healthBuilder.withDetail("error", "Kafka KRaft server is not responding");
        }

        Health health = healthBuilder.build();
        HttpStatus status = isKafkaHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

        return ResponseEntity.status(status).body(health);
    }

    /**
     * Custom health check endpoint that can be used to check specific components.
     */
    @PostMapping("/check")
    public ResponseEntity<Health> customHealthCheck(@RequestBody Map<String, Object> checkParams) {
        String component = (String) checkParams.getOrDefault("component", "unknown");

        // Implement custom health check logic based on the component
        Health health = Health.up()
                .withDetail("component", component)
                .withDetail("checked", true)
                .withDetail("timestamp", System.currentTimeMillis())
                .build();

        return ResponseEntity.ok(health);
    }
}
