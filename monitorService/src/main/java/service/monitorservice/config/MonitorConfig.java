package service.monitorservice.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

/**
 * Configuration class for monitoring service settings.
 * Sets up metrics collection, health checks, and monitoring integrations.
 */
@Configuration
@EnableAspectJAutoProxy
public class MonitorConfig {

    /**
     * Customizes the meter registry with application information.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> registry.config()
                .commonTags("application", "web-banking")
                .commonTags("service", "monitor-service")
                .commonTags("environment", environment.getActiveProfiles().length > 0 ?
                        environment.getActiveProfiles()[0] : "default");
    }

    /**
     * Configures the TimedAspect for @Timed annotation support.
     * This allows methods to be timed using the @Timed annotation.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Configures health check settings.
     */
    @Bean
    public HealthCheckConfig healthCheckConfig() {
        return new HealthCheckConfig();
    }

    /**
     * Inner class for health check configuration.
     */
    @Setter
    @Getter
    public static class HealthCheckConfig {
        private long timeout = 5000; // Default timeout in milliseconds
        private boolean detailedOutput = true;

    }

    /**
     * Configures alert thresholds for the monitoring service.
     */
    @Bean
    public AlertConfig alertConfig() {
        AlertConfig config = new AlertConfig();
        config.setCpuThreshold(80.0); // 80% CPU usage threshold
        config.setMemoryThreshold(85.0); // 85% memory usage threshold
        config.setDiskThreshold(90.0); // 90% disk usage threshold
        config.setResponseTimeThreshold(2000); // 2 seconds response time threshold
        return config;
    }

    /**
     * Alert configuration class for setting monitoring thresholds.
     */
    @Setter
    @Getter
    public static class AlertConfig {
        private double cpuThreshold;
        private double memoryThreshold;
        private double diskThreshold;
        private long responseTimeThreshold;
        private boolean alertsEnabled = true;

    }

    /**
     * Configures log aggregation settings.
     */
    @Bean
    public LogAggregationConfig logAggregationConfig() {
        LogAggregationConfig config = new LogAggregationConfig();
        config.setRetentionDays(30);
        config.setIndexPrefix("web-banking-logs");
        return config;
    }

    /**
     * Log aggregation configuration class.
     */
    @Setter
    @Getter
    public static class LogAggregationConfig {
        private int retentionDays;
        private String indexPrefix;
        private boolean enabledStructuredLogging = true;

    }
}
