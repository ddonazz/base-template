package it.andrea.start.service.audit;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.andrea.start.dto.audit.AuditTraceDTO;
import it.andrea.start.models.audit.AuditTrace;
import it.andrea.start.searchcriteria.audit.AuditTraceSearchCriteria;

public interface AuditTraceService {

    Page<AuditTraceDTO> searchAuditTrace(AuditTraceSearchCriteria criteria, Pageable pageable);

    void saveLog(AuditTrace auditTrace);

    AuditTraceDTO getAuditTrace(Long id);

    int deleteAuditTrace(Instant instant);

}
