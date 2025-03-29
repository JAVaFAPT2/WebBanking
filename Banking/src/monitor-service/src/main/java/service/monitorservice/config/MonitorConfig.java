package service.monitorservice.config;

import io.micrometer.core.instrument.MeterRegistry;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitorConfig {

    /**
     * Creates a PrometheusMeterRegistry bean that will be used by Micrometer to collect metrics.
     *
     * @return a PrometheusMeterRegistry instance configured with default settings
     */
    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    /**
     * Exposes a MeterRegistry bean. In this case, we return the PrometheusMeterRegistry.
     *
     * @param prometheusMeterRegistry the Prometheus meter registry
     * @return a MeterRegistry bean
     */
    @Bean
    public MeterRegistry meterRegistry(PrometheusMeterRegistry prometheusMeterRegistry) {
        return prometheusMeterRegistry;
    }
}
