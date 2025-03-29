package service.monitorservice.controller;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MetricsController {

    private final MeterRegistry meterRegistry;

    @Autowired
    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    @GetMapping("/metrics/custom")
    public Map<String, Object> getCustomMetrics() {
        // Example: retrieve a custom counter metric
        double myCounter = meterRegistry.counter("my.custom.counter").count();
        // You can add more metrics as needed.
        return Map.of("myCustomCounter", myCounter);
    }
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        // Example: retrieve a custom counter metric
        double myCounter = meterRegistry.counter("my.custom.counter").count();
        // You can add more metrics as needed.
        return Map.of("myCustomCounter", myCounter);
    }
    @GetMapping("/metrics/health")
    public Map<String, Object> getHealthMetrics() {
        // Example: retrieve a custom counter metric
        double myCounter = meterRegistry.counter("my.custom.counter").count();
        // You can add more metrics as needed.
        return Map.of("myCustomCounter", myCounter);
    }
}
