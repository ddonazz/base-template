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
    private AuditActivity activity;
    private String username;
    private AuditTypeOperation auditType;

    private Instant dateEventFrom;
    private Instant dateEventTo;
    private String className;
    private String methodName;
    private String controllerMethod;
    private String httpMethod;
    private String requestUri;
    private String clientIpAddress;
    private Boolean success;
    private Long durationMs;
    private String exceptionType;
    private String exceptionMessage;

}
