package service.monitorservice.repository;




import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import service.monitorservice.model.Metric;

import java.time.Instant;
import java.util.List;

/**
 * Repository for storing and retrieving metrics data using JPA.
 */
@Repository
public interface MetricsRepository extends JpaRepository<Metric, Long> {

    /**
     * Find metrics by service name.
     */
    List<Metric> findByServiceNameOrderByTimestampDesc(String serviceName);

    /**
     * Find metrics by service name and metric name.
     */
    List<Metric> findByServiceNameAndNameOrderByTimestampDesc(String serviceName, String name);

    /**
     * Find metrics by service name within a time range.
     */
    List<Metric> findByServiceNameAndTimestampBetweenOrderByTimestampDesc(
            String serviceName, Instant startTime, Instant endTime);

    /**
     * Find the latest metric by service name and metric name.
     */
    Metric findFirstByServiceNameAndNameOrderByTimestampDesc(String serviceName, String name);

    /**
     * Get average value for a specific metric within a time range.
     */
    @Query("SELECT AVG(m.value) FROM Metric m WHERE m.serviceName = :serviceName " +
            "AND m.name = :metricName AND m.timestamp BETWEEN :startTime AND :endTime")
    Double getAverageValue(
            @Param("serviceName") String serviceName,
            @Param("metricName") String metricName,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * Get minimum value for a specific metric within a time range.
     */
    @Query("SELECT MIN(m.value) FROM Metric m WHERE m.serviceName = :serviceName " +
            "AND m.name = :metricName AND m.timestamp BETWEEN :startTime AND :endTime")
    Double getMinValue(
            @Param("serviceName") String serviceName,
            @Param("metricName") String metricName,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * Get maximum value for a specific metric within a time range.
     */
    @Query("SELECT MAX(m.value) FROM Metric m WHERE m.serviceName = :serviceName " +
            "AND m.name = :metricName AND m.timestamp BETWEEN :startTime AND :endTime")
    Double getMaxValue(
            @Param("serviceName") String serviceName,
            @Param("metricName") String metricName,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * Delete metrics older than a specific timestamp.
     */
    void deleteByTimestampBefore(Instant cutoffTime);
}
