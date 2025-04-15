package service.monitorservice.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
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
    public static class HealthCheckConfig {
        private long timeout = 5000; // Default timeout in milliseconds
        private boolean detailedOutput = true;

        // Getters
        public long getTimeout() {
            return timeout;
        }

        public boolean isDetailedOutput() {
            return detailedOutput;
        }

        // Setters
        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public void setDetailedOutput(boolean detailedOutput) {
            this.detailedOutput = detailedOutput;
        }
    }

    /**
     * Configures alert thresholds for the monitoring service.
     */
    @Bean
    public AlertConfig alertConfig() {
        AlertConfig config = new AlertConfig();
        // Using direct field access instead of setters
        config.cpuThreshold = 80.0; // 80% CPU usage threshold
        config.memoryThreshold = 85.0; // 85% memory usage threshold
        config.diskThreshold = 90.0; // 90% disk usage threshold
        config.responseTimeThreshold = 2000; // 2 seconds response time threshold
        return config;
    }

    /**
     * Alert configuration class for setting monitoring thresholds.
     */
    public static class AlertConfig {
        public double cpuThreshold;
        public double memoryThreshold;
        public double diskThreshold;
        public long responseTimeThreshold;
        private boolean alertsEnabled = true;

        // Getters
        public double getCpuThreshold() {
            return cpuThreshold;
        }

        public double getMemoryThreshold() {
            return memoryThreshold;
        }

        public double getDiskThreshold() {
            return diskThreshold;
        }

        public long getResponseTimeThreshold() {
            return responseTimeThreshold;
        }

        public boolean isAlertsEnabled() {
            return alertsEnabled;
        }

        // Setters
        public void setCpuThreshold(double cpuThreshold) {
            this.cpuThreshold = cpuThreshold;
        }

        public void setMemoryThreshold(double memoryThreshold) {
            this.memoryThreshold = memoryThreshold;
        }

        public void setDiskThreshold(double diskThreshold) {
            this.diskThreshold = diskThreshold;
        }

        public void setResponseTimeThreshold(long responseTimeThreshold) {
            this.responseTimeThreshold = responseTimeThreshold;
        }

        public void setAlertsEnabled(boolean alertsEnabled) {
            this.alertsEnabled = alertsEnabled;
        }
    }

    /**
     * Configures log aggregation settings.
     */
    @Bean
    public LogAggregationConfig logAggregationConfig() {
        LogAggregationConfig config = new LogAggregationConfig();
        // Using direct field access instead of setters
        config.retentionDays = 30;
        config.indexPrefix = "web-banking-logs";
        return config;
    }

    /**
     * Log aggregation configuration class.
     */
    public static class LogAggregationConfig {
        public int retentionDays;
        public String indexPrefix;
        private boolean enabledStructuredLogging = true;

        // Getters
        public int getRetentionDays() {
            return retentionDays;
        }

        public String getIndexPrefix() {
            return indexPrefix;
        }

        public boolean isEnabledStructuredLogging() {
            return enabledStructuredLogging;
        }

        // Setters
        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }

        public void setIndexPrefix(String indexPrefix) {
            this.indexPrefix = indexPrefix;
        }

        public void setEnabledStructuredLogging(boolean enabledStructuredLogging) {
            this.enabledStructuredLogging = enabledStructuredLogging;
        }
    }
}
