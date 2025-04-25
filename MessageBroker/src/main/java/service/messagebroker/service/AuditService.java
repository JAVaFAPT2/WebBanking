package service.messagebroker.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

public interface AuditService {
    void recordRiskAssessment(UUID accountId, Map<String, Object> assessment);
}
