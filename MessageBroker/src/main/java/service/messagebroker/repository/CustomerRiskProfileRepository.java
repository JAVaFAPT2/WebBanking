package service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import service.messagebroker.models.CustomerRiskProfile;

import java.util.Optional;

@Repository
public interface CustomerRiskProfileRepository extends JpaRepository<CustomerRiskProfile, String> {
    Optional<CustomerRiskProfile> findByUserId(String userId);
}
