package service.loggingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import service.loggingservice.entity.LogEntity;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, UUID> {
    Optional<LogEntity> findByTimestamp(LocalDateTime timestamp);
}
