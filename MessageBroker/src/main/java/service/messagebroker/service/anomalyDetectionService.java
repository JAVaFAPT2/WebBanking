package service.messagebroker.service;

import org.springframework.stereotype.Service;
import service.messagebroker.models.AnomalyDetectionResult;
import service.messagebroker.models.UserBehaviorProfile;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public interface anomalyDetectionService {
    AnomalyDetectionResult analyze(UUID accountId, Map<String, Object> transactionContext) ;
    double detectAmountAnomaly(double amount, UserBehaviorProfile profile);
    double detectTimeAnomaly(long timestamp, UserBehaviorProfile profile);
    double detectLocationAnomaly(String location, UserBehaviorProfile profile);
    double detectVelocityAnomaly(UUID accountId, Map<String, Object> transactionContext);
    String determineRecommendedAction(double score);
}
