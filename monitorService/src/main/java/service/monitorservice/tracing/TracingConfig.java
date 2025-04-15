package service.monitorservice.tracing;

import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class    TracingConfig {

    @Bean
    public Tracer tracer() {
        io.jaegertracing.Configuration.SamplerConfiguration samplerConfig = io.jaegertracing.Configuration.SamplerConfiguration.fromEnv()
                .withType(ConstSampler.TYPE)
                .withParam(1); // Sample all traces

        io.jaegertracing.Configuration.ReporterConfiguration reporterConfig = io.jaegertracing.Configuration.ReporterConfiguration.fromEnv()
                .withLogSpans(true); // Log spans for debugging

        io.jaegertracing.Configuration config = new io.jaegertracing.Configuration("monitor-service")
                .withSampler(samplerConfig)
                .withReporter(reporterConfig);

        return config.getTracer();
    }}
