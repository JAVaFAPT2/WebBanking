package service.monitorservice.event;

import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@ToString
@Getter
@Setter
public class MetricsEvent {
    private final String eventType;
    private final Instant timestamp;
    private final double value; // This can represent a metric value if needed
}
