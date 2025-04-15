package service.monitorservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.monitorservice.model.Metric;
import service.monitorservice.repository.MetricsRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing metrics data.
 */
@Service
public class MetricsService {

    private final MetricsRepository metricsRepository;

    @Autowired
    public MetricsService(MetricsRepository metricsRepository) {
        this.metricsRepository = metricsRepository;
    }

    /**
     * Save a metric.
     */
    public Metric saveMetric(Metric metric) {
        return metricsRepository.save(metric);
    }

    /**
     * Save multiple metrics.
     */
    public List<Metric> saveMetrics(List<Metric> metrics) {
        return metricsRepository.saveAll(metrics);
    }

    /**
     * Get metrics for a specific service.
     */
    public List<Metric> getMetricsByService(String serviceName) {
        return metricsRepository.findByServiceNameOrderByTimestampDesc(serviceName);
    }

    /**
     * Get metrics for a specific service and metric name.
     */
    public List<Metric> getMetricsByServiceAndName(String serviceName, String metricName) {
        return metricsRepository.findByServiceNameAndNameOrderByTimestampDesc(serviceName, metricName);
    }

    /**
     * Get metrics for a specific service and time range.
     */
    public List<Metric> getMetricsByServiceAndTimeRange(String serviceName, Instant startTime, Instant endTime) {
        return metricsRepository.findByServiceNameAndTimestampBetweenOrderByTimestampDesc(
                serviceName, startTime, endTime);
    }

    /**
     * Get the latest value for a specific metric.
     */
    public Double getLatestMetricValue(String serviceName, String metricName) {
        Metric metric = metricsRepository.findFirstByServiceNameAndNameOrderByTimestampDesc(serviceName, metricName);
        return metric != null ? metric.getValue() : null;
    }

    /**
     * Get aggregated metrics (e.g., average, min, max) for a specific service and metric name.
     */
    public Map<String, Double> getAggregatedMetrics(String serviceName, String metricName,
                                                    Instant startTime, Instant endTime) {
        Map<String, Double> result = new HashMap<>();

        Double avg = metricsRepository.getAverageValue(serviceName, metricName, startTime, endTime);
        Double min = metricsRepository.getMinValue(serviceName, metricName, startTime, endTime);
        Double max = metricsRepository.getMaxValue(serviceName, metricName, startTime, endTime);

        result.put("avg", avg);
        result.put("min", min);
        result.put("max", max);

        return result;
    }

    /**
     * Delete old metrics data to manage database size.
     */
    @Transactional
    public void deleteOldMetrics(Instant cutoffTime) {
        metricsRepository.deleteByTimestampBefore(cutoffTime);
    }
}
