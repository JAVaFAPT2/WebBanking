package service.monitorservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.monitorservice.DTO.MetricDTO;
import service.monitorservice.model.Metric;
import service.monitorservice.repository.MetricsRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public MetricDTO saveMetric(MetricDTO metricDTO) {
        Metric metric = convertToEntity(metricDTO);
        Metric savedMetric = metricsRepository.save(metric);
        return convertToDTO(savedMetric);
    }

    /**
     * Save multiple metrics.
     */
    public List<MetricDTO> saveMetrics(List<MetricDTO> metricDTOs) {
        List<Metric> metrics = metricDTOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        List<Metric> savedMetrics = metricsRepository.saveAll(metrics);

        return savedMetrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get metrics for a specific service.
     */
    public List<MetricDTO> getMetricsByService(String serviceName) {
        List<Metric> metrics = metricsRepository.findByServiceNameOrderByTimestampDesc(serviceName);
        return metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get metrics for a specific service and metric name.
     */
    public List<MetricDTO> getMetricsByServiceAndName(String serviceName, String metricName) {
        List<Metric> metrics = metricsRepository.findByServiceNameAndNameOrderByTimestampDesc(serviceName, metricName);
        return metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get metrics for a specific service and time range.
     */
    public List<MetricDTO> getMetricsByServiceAndTimeRange(String serviceName, Instant startTime, Instant endTime) {
        List<Metric> metrics = metricsRepository.findByServiceNameAndTimestampBetweenOrderByTimestampDesc(
                serviceName, startTime, endTime);
        return metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

    /**
     * Convert DTO to Entity
     */
    private Metric convertToEntity(MetricDTO dto) {
        return new Metric(
                dto.serviceName(),
                dto.name(),
                dto.value(),
                dto.timestamp(),
                dto.unit()
        );
    }

    /**
     * Convert Entity to DTO
     */
    private MetricDTO convertToDTO(Metric entity) {
        return new MetricDTO(
                entity.getServiceName(),
                entity.getName(),
                entity.getValue(),
                entity.getTimestamp(),
                entity.getUnit()
        );
    }
}
