package service.monitorservice.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Meter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import service.monitorservice.DTO.MetricDTO;
import service.monitorservice.service.MetricsService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    private final MeterRegistry meterRegistry;
    private final MetricsService metricsService;

    @Autowired
    public MetricsController(MeterRegistry meterRegistry, MetricsService metricsService) {
        this.meterRegistry = meterRegistry;
        this.metricsService = metricsService;
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

    @GetMapping("/service-info/{serviceName}")
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
    /**
     * Save a single metric
     */
    @PostMapping
    public ResponseEntity<MetricDTO> saveMetric(@RequestBody MetricDTO metric) {
        return ResponseEntity.ok(metricsService.saveMetric(metric));
    }

    /**
     * Save multiple metrics
     */
    @PostMapping("/batch")
    public ResponseEntity<List<MetricDTO>> saveMetrics(@RequestBody List<MetricDTO> metrics) {
        return ResponseEntity.ok(metricsService.saveMetrics(metrics));
    }

    /**
     * Get metrics for a specific service
     */
    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<MetricDTO>> getMetricsByService(@PathVariable String serviceName) {
        return ResponseEntity.ok(metricsService.getMetricsByService(serviceName));
    }

    /**
     * Get metrics for a specific service and metric name
     */
    @GetMapping("/service/{serviceName}/name/{metricName}")
    public ResponseEntity<List<MetricDTO>> getMetricsByServiceAndName(
            @PathVariable String serviceName,
            @PathVariable String metricName) {
        return ResponseEntity.ok(metricsService.getMetricsByServiceAndName(serviceName, metricName));
    }

    /**
     * Get metrics for a specific service and time range
     */
    @GetMapping("/service/{serviceName}/timerange")
    public ResponseEntity<List<MetricDTO>> getMetricsByServiceAndTimeRange(
            @PathVariable String serviceName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        Instant startInstant = startTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endTime.atZone(ZoneId.systemDefault()).toInstant();

        return ResponseEntity.ok(metricsService.getMetricsByServiceAndTimeRange(
                serviceName, startInstant, endInstant));
    }

    /**
     * Get the latest value for a specific metric
     */
    @GetMapping("/service/{serviceName}/name/{metricName}/latest")
    public ResponseEntity<Double> getLatestMetricValue(
            @PathVariable String serviceName,
            @PathVariable String metricName) {

        Double value = metricsService.getLatestMetricValue(serviceName, metricName);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(value);
    }

    /**
     * Get aggregated metrics for a specific service and metric name
     */
    @GetMapping("/service/{serviceName}/name/{metricName}/aggregated")
    public ResponseEntity<Map<String, Double>> getAggregatedMetrics(
            @PathVariable String serviceName,
            @PathVariable String metricName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        Instant startInstant = startTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endTime.atZone(ZoneId.systemDefault()).toInstant();

        return ResponseEntity.ok(metricsService.getAggregatedMetrics(
                serviceName, metricName, startInstant, endInstant));
    }

    /**
     * Delete old metrics data
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> deleteOldMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffTime) {

        Instant cutoffInstant = cutoffTime.atZone(ZoneId.systemDefault()).toInstant();
        metricsService.deleteOldMetrics(cutoffInstant);

        return ResponseEntity.ok("Old metrics deleted successfully");
    }
}
