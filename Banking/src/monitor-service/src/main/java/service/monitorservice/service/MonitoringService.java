package service.monitorservice.service;

import service.shared.models.Metrics;

public interface MonitoringService {
    Metrics collectMetrics();
}
