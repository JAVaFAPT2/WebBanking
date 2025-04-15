package service.monitorservice.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HealthCheckConfig {
    private long timeout = 5000; // Default timeout in milliseconds
    private boolean detailedOutput = true;

}
