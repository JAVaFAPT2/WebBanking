package service.monitorservice.config;

import io.micrometer.core.instrument.MeterRegistry;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomPrometheusConfig {

    @Bean
    public MeterRegistry meterRegistry() {
        // Use the default Prometheus configuration
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
}
