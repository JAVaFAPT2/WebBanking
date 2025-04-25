package service.messagebroker.service;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


public interface UserSettingsService {

    Optional<Double> getSpendingThreshold(UUID userId);
    Optional<Double> getRiskFactor(UUID userId);

}
