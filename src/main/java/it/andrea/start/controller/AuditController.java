package it.andrea.start.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.andrea.start.dto.audit.AuditTraceDTO;
import it.andrea.start.error.exception.mapping.MappingToDtoException;
import it.andrea.start.searchcriteria.audit.AuditTraceSearchCriteria;
import it.andrea.start.service.audit.AuditTraceService;

@Tag(name = "Audit API")
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditTraceService auditTraceService;

    public AuditController(AuditTraceService auditTraceService) {
        super();
        this.auditTraceService = auditTraceService;
    }

    // @formatter:off
    @Operation(
        method = "GET",
        description = "List audits by search criteria with timezone date",
        summary = "List audits by search criteria with timezone date"
    )
    // @formatter:on
    @GetMapping("/list")
    public ResponseEntity<Page<AuditTraceDTO>> listAudits(AuditTraceSearchCriteria auditTraceSearchCriteria, Pageable pageable) {
        return ResponseEntity.ok(auditTraceService.searchAuditTrace(auditTraceSearchCriteria, pageable));
    }

    // @formatter:off
    @Operation(
        method = "GET",
        description = "Return information of a specific audit",
        summary = "Return information of a specific audit"
    )
    // @formatter:on
    @GetMapping("/{id}")
    public ResponseEntity<AuditTraceDTO> getAudit(@PathVariable Long id) throws MappingToDtoException {
        AuditTraceDTO audits = auditTraceService.getAuditTrace(id);
        return ResponseEntity.ok(audits);
    }

}
