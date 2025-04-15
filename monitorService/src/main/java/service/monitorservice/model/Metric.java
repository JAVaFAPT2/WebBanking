package service.monitorservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA Entity representing a metric data point.
 */
@Entity
@Table(name = "metrics", indexes = {
        @Index(name = "idx_service_metric", columnList = "service_name,metric_name"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "metric_name", nullable = false)
    private String name;

    @Column(name = "metric_value", nullable = false)
    private double value;

    @ElementCollection
    @CollectionTable(name = "metric_tags",
            joinColumns = @JoinColumn(name = "metric_id"))
    @MapKeyColumn(name = "tag_key")
    @Column(name = "tag_value")
    private Map<String, String> tags = new HashMap<>();

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    @Override
    public String toString() {
        return "Metric{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                ", tags=" + tags +
                ", timestamp=" + timestamp +
                '}';
    }
}
