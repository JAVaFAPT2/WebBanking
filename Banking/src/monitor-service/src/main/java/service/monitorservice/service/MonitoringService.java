package service.monitorservice.service;

import service.monitorservice.model.Metrics;

public interface MonitoringService {
    Metrics collectMetrics();
}
