package service.auditservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import service.auditservice.event.AuditEvenEx;
import service.shared.event.AuditEvent;


import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<AuditEvenEx, UUID> {

}
