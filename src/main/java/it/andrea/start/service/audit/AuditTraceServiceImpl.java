package it.andrea.start.service.audit;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.andrea.start.dto.audit.AuditTraceDTO;
import it.andrea.start.mappers.audit.AuditMapper;
import it.andrea.start.models.audit.AuditTrace;
import it.andrea.start.repository.audit.AuditTraceRepository;
import it.andrea.start.searchcriteria.audit.AuditTraceSearchCriteria;
import it.andrea.start.searchcriteria.audit.AuditTraceSearchSpecification;

@Service
@Transactional
public class AuditTraceServiceImpl implements AuditTraceService {

    private final AuditTraceRepository auditTraceRepository;

    private final AuditMapper auditMapper;

    public AuditTraceServiceImpl(AuditTraceRepository auditTraceRepository, AuditMapper auditMapper) {
        super();
        this.auditTraceRepository = auditTraceRepository;
        this.auditMapper = auditMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(AuditTrace auditTrace) {
        auditTraceRepository.save(auditTrace);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditTraceDTO> searchAuditTrace(AuditTraceSearchCriteria criteria, Pageable pageable) {
        final Page<AuditTrace> auditPage = auditTraceRepository.findAll(new AuditTraceSearchSpecification(criteria), pageable);
        return auditPage.map(auditMapper::toDto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditTraceDTO getAuditTrace(Long id) {
        AuditTrace auditTrace = auditTraceRepository.findById(id).orElse(null);
        return auditMapper.toDto(auditTrace);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAuditTrace(Instant instant) {
        return auditTraceRepository.deleteRows(instant);
    }

}
