package service.monitorservice.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AlertConfig {
    private double cpuThreshold;
    private double memoryThreshold;
    private double diskThreshold;
    private long responseTimeThreshold;
    private boolean alertsEnabled = true;

}