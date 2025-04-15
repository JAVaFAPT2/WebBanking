package service.monitorservice.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogAggregationConfig {
    private int retentionDays;
    private String indexPrefix;
    private boolean enabledStructuredLogging = true;

}
