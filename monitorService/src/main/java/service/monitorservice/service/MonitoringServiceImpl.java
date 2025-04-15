package service.monitorservice.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.monitorservice.model.AlertSeverity;

@Service
public class MonitoringServiceImpl implements MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringServiceImpl.class);

    private final MeterRegistry meterRegistry;
    private final AlertService alertService;
    private final LogAggregationService logAggregationService;

    @Autowired
    public MonitoringServiceImpl(MeterRegistry meterRegistry,
                                 AlertService alertService,
                                 LogAggregationService logAggregationService) {
        this.meterRegistry = meterRegistry;
        this.alertService = alertService;
        this.logAggregationService = logAggregationService;
    }

    public void recordMetric(String metricName, double value, String... tags) {
        meterRegistry.gauge(metricName, value);
        log.info("Recorded metric: {} with value: {}", metricName, value);
    }

    public Counter createCounter(String name, String... tags) {
        return Counter.builder(name)
                .tags(tags)
                .register(meterRegistry);
    }

    public Timer createTimer(String name, String... tags) {
        return Timer.builder(name)
                .tags(tags)
                .register(meterRegistry);
    }

    public void triggerAlert(String alertName, String message, AlertSeverity severity) {
        alertService.sendAlert(alertName, message, severity);
        log.warn("Alert triggered: {} - {}", alertName, message);
    }

    public void aggregateLogs(String serviceId, String logContent) {
        logAggregationService.aggregateLog(serviceId, logContent);
        log.debug("Aggregated logs for service: {}", serviceId);
    }

    public void monitorHealthCheck(String serviceName, boolean isHealthy) {
        String metricName = "service.health";
        double value = isHealthy ? 1.0 : 0.0;
        recordMetric(metricName, value, "service", serviceName);

        if (!isHealthy) {
            triggerAlert(
                    "ServiceHealthAlert",
                    String.format("Service %s is unhealthy", serviceName),
                    AlertSeverity.CRITICAL
            );
        }
    }
}
