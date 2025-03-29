package service.loggingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import service.shared.models.Log;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LogRepository extends JpaRepository<Log, UUID> {
    Optional<Log> findByTimestamp(LocalDateTime date);
}
