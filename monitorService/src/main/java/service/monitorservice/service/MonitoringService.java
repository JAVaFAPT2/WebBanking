package service.monitorservice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import service.monitorservice.model.AlertSeverity;

public interface  MonitoringService {
    void recordMetric(String metricName, double value, String... tags);
    Counter createCounter(String name, String... tags);
    Timer createTimer(String name, String... tags);
    void triggerAlert(String alertName, String message, AlertSeverity severity);
    void aggregateLogs(String serviceId, String logContent);
    void monitorHealthCheck(String serviceName, boolean isHealthy);

}
