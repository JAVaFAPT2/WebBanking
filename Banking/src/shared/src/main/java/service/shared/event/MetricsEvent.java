package service.shared.event;

import lombok.*;

import java.time.Instant;

/**
 * @param value This can represent a metric value if needed
 */
public record MetricsEvent(String eventType, Instant timestamp, double value) {
}
