package it.andrea.start.job;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import it.andrea.start.configuration.GlobalConfig;
import it.andrea.start.service.audit.AuditTraceService;
import lombok.AllArgsConstructor;

@Component
@DisallowConcurrentExecution
@AllArgsConstructor
public class AuditDeleteJob extends QuartzJobBean {

    private static final Logger LOG = LoggerFactory.getLogger(AuditDeleteJob.class);

    private final GlobalConfig globalConfig;
    private final AuditTraceService auditTraceService;

    @Override
    public void executeInternal(@NonNull JobExecutionContext context) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        long retentionDays = jobDataMap.getIntValue("retentionDays");
        if (retentionDays <= 0) {
            retentionDays = globalConfig.getAuditSavedDay();
        }

        Instant now = Instant.now();
        LOG.info("Start at : {}", LocalDateTime.from(now));

        Instant deleteBefore = now.minus(retentionDays, ChronoUnit.DAYS);
        LOG.info("Delete audits before of : {}", LocalDateTime.from(now));

        int rowDeleted = auditTraceService.deleteAuditTrace(deleteBefore);
        LOG.info("Deleted audits : {}", rowDeleted);

        LOG.info("Ending at : {}", LocalDateTime.from(Instant.now()));
    }
}
