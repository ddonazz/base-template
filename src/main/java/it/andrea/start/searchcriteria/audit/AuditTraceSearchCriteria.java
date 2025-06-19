package it.andrea.start.searchcriteria.audit;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import it.andrea.start.constants.AuditActivity;
import it.andrea.start.constants.AuditTypeOperation;
import lombok.Data;

@Data
public class AuditTraceSearchCriteria implements Serializable {

    @Serial
    private static final long serialVersionUID = 369313118430341308L;

    private Long id;
    private String sessionId;
    private AuditActivity activity;
    private Long userId;
    private String userName;
    private AuditTypeOperation auditType;
    private String textSearch;
    private Instant dateFrom;
    private Instant dateTo;

}
