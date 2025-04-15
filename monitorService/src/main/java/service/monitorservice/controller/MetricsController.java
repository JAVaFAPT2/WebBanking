package service.monitorservice.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Meter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    private final MeterRegistry meterRegistry;

    @Autowired
    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/summary")
    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("availableMetrics", meterRegistry.getMeters().size());
        summary.put("service", "monitor-service");
        return summary;
    }

    @GetMapping("/all")
    public List<Map<String, Object>> getAllMetrics() {
        return meterRegistry.getMeters().stream()
                .map(meter -> {
                    Map<String, Object> metricData = new HashMap<>();
                    metricData.put("name", meter.getId().getName());
                    metricData.put("tags", meter.getId().getTags());
                    metricData.put("type", meter.getId().getType().name());
                    return metricData;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/service/{serviceName}")
    public Map<String, Object> getServiceMetrics(@PathVariable String serviceName) {
        Map<String, Object> metrics = new HashMap<>();
        List<Meter> serviceMeters = meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith(serviceName))
                .toList();

        metrics.put("service", serviceName);
        metrics.put("metricsCount", serviceMeters.size());
        metrics.put("metrics", serviceMeters.stream()
                .map(meter -> meter.getId().getName())
                .collect(Collectors.toList()));

        return metrics;
    }
}
