package service.monitorservice.service.impl;



import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.shared.models.Metrics;

import service.monitorservice.service.MonitoringService;

@Service
public class MonitoringServiceImpl implements MonitoringService {

    private final MeterRegistry meterRegistry;

    @Autowired
    public MonitoringServiceImpl(MeterRegistry meterRegistry  ) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Metrics collectMetrics() {

        double cpuUsage = meterRegistry.find("system.cpu.usage").gauge() != null
                ? meterRegistry.find("system.cpu.usage").gauge().value()
                : 0.0;
        double memoryUsage = meterRegistry.find("jvm.memory.used").gauge() != null
                ? meterRegistry.find("jvm.memory.used").gauge().value()
                : 0.0;

        double diskUsage = 0.0;

        return new Metrics(cpuUsage, memoryUsage, diskUsage);
    }
}
