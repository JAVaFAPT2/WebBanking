package service.monitorservice.listener;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import service.shared.event.MetricsEvent;


@Component
public class MetricsEventListener {

    private final MeterRegistry meterRegistry;

    public MetricsEventListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Listens for MetricsEvent events and increments a custom metric counter.
     *
     * @param event the metrics event
     */
    @EventListener
    public void onMetricsEvent(MetricsEvent event) {
        // Example: increment a counter using the event's type as a tag
        meterRegistry.counter("app.metrics.events", "eventType", event.getEventType()).increment();

        // You can add additional processing for different event types if needed.
        System.out.println("Processed MetricsEvent: " + event);
    }
}
