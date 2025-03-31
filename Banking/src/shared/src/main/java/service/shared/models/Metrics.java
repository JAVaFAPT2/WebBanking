package service.shared.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A simple POJO representing application metrics.
 * You can extend this with additional fields as needed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metrics {

    /**
     * The CPU usage in percentage.
     */
    private double cpuUsage;

    /**
     * The memory usage in percentage.
     */
    private double memoryUsage;

    /**
     * The disk usage in percentage.
     */
    private double diskUsage;


}
