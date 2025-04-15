package service.monitorservice.DTO;

import java.time.Instant;

public record MetricDTO(
        String serviceName,
        String name,
        Double value,
        Instant timestamp,
        String unit
) {}
