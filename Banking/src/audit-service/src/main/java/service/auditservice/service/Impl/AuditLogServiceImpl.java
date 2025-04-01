package service.auditservice.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.auditservice.mapper.AuditMapper;
import service.auditservice.model.AuditLogEntity;
import service.auditservice.repository.AuditLogRepository;
import service.auditservice.service.AuditLogService;
import service.shared.models.AuditLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public AuditLogEntity save(AuditLogEntity auditLog) {
        // Save the AuditLogEntity directly
        return auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLogEntity> getAllAuditLogs() {
        List<AuditLogEntity> entities = auditLogRepository.findAll();
        return new ArrayList<>(entities); // No need for mapping here
    }

    @Override
    public AuditLogEntity getAuditLogById(UUID id) {
        Optional<AuditLogEntity> optionalAuditLog = Optional.empty();
        try {
            optionalAuditLog = auditLogRepository.findById(id);

        } catch (IllegalArgumentException e) {
            // Handle the exception as needed
            System.out.println("Error: " + e.getMessage());
        }
        return optionalAuditLog.orElse(null);
    }
}
