package service.messagebroker.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import service.messagebroker.models.CustomerRiskProfile;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRiskProfileRepository extends JpaRepository<CustomerRiskProfile, UUID> {
    Optional<CustomerRiskProfile> findByUserId(UUID userId);
}
